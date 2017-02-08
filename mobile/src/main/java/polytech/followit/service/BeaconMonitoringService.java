package polytech.followit.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import polytech.followit.model.Node;
import polytech.followit.model.POI;
import polytech.followit.model.Path;
import polytech.followit.utility.PathSingleton;

public class BeaconMonitoringService extends Service implements
        BeaconManager.MonitoringListener,
        BeaconManager.ServiceReadyCallback {

    private static final String TAG = BeaconMonitoringService.class.getName();
    private Messenger messenger;

    private BeaconManager beaconManager;

    private Socket socket;
    public static Path path;
    private boolean isStarted = false;
    private polytech.followit.model.Beacon lastBeaconDetected = new polytech.followit.model.Beacon(null, null, 0, 0);


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ON CREATE SERVICE");
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setBackgroundScanPeriod(1000, 2000);
        beaconManager.setRegionExitExpiration(5000);

        beaconManager.connect(this);
        beaconManager.setMonitoringListener(this);

        messenger = new Messenger(new MessageHandler());

        try {
            socket = IO.socket("https://followit-backend.herokuapp.com/");
        } catch (URISyntaxException e) {
            Log.d(TAG, "Couldn't socket" + e);
        }

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }

        }).on("beaconList", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                buildAllBeacons(args);
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        });
        socket.connect();
    }

    /**
     * Called when we use start(@Intent service)
     */
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.d(TAG, "on start command");
        if (!isStarted) {
            socket.emit("getAllBeacons");
            isStarted = true;
        }
        path = intent.getExtras().getParcelable("path");
        return START_NOT_STICKY;
    }

    /**
     * We enter a region we're monitoring
     *
     * @param region Region we just entered
     * @param list   List of beacon detected
     */
    @Override
    public void onEnteredRegion(Region region, List<Beacon> list) {
        Log.d(TAG, "ON ENTERED REGION" + path.toString());

        // We need to convert all @Estimote.Beacon objects to our @Followit.Beacon objects
        ArrayList<polytech.followit.model.Beacon> listDetectedBeacon = new ArrayList<>();
        for (Beacon beacon : list) {
            listDetectedBeacon.add(new polytech.followit.model.Beacon(
                    "beacon",
                    beacon.getProximityUUID().toString(),
                    beacon.getMajor(),
                    beacon.getMinor()
            ));
        }

        for (polytech.followit.model.Beacon beacon : listDetectedBeacon) {
            if (!lastBeaconDetected.equals(beacon)) {
                lastBeaconDetected = beacon;
                // Beacon not in our path
                if (!path.getListBeacons().contains(beacon)) {
                    Log.d(TAG, "detected beacon :" + beacon + " not in our path. Source node : " + path.getSource());
                    Bundle msg_data = new Bundle();
                    String newSource = getNodeofBeacon(beacon);
                    msg_data.putString("source", newSource);
                    msg_data.putString("destination", path.getDestination());
                    sendMessage(MessageHandler.MSG_ASK_NEW_PATH, msg_data);
                }
                // Beacon detected in our path
                else {
                    Log.d(TAG, "We detected a beacon in our path");
                    // If the detected beacon is the arrival one
                    if (isArrivalBeacon(beacon))
                        sendMessage(MessageHandler.MSG_ARRIVED_TO_DESTINATION, null);
                    else sendMessage(MessageHandler.MSG_NEXT_INSTRUCTION, null);
                }
            }
        }
    }

    private String getNodeofBeacon(polytech.followit.model.Beacon beacon) {
        for (Node n : PathSingleton.getInstance().getPath().getListNodes()) {
            if (n.getBeacon().equals(beacon)) {
                return n.getName();
            }
        }
        return "error";
    }

    @Override
    public void onExitedRegion(Region region) {

    }

    @Override
    public void onServiceReady() {
        Log.d(TAG, "on service Ready");
    }

    /**
     * When binding to the service, we return the binder so we can send messages
     */
    @Override
    public IBinder onBind(Intent intent) {
        // Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return messenger.getBinder();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service stopped and destroyed");
        socket.disconnect();
        beaconManager.disconnect();
        //super.onDestroy();
    }


    //==============================================================================================
    // Utils
    //==============================================================================================

    /**
     * Build a list of all beacons fetched by the socket a
     * and monitor regions based on the beacons fetched
     *
     * @param args response from socket
     */
    private void buildAllBeacons(Object... args) {
        JSONObject response = (JSONObject) args[0];
        ArrayList<polytech.followit.model.Beacon> listAllBeacons = new ArrayList<>();
        Log.d(TAG, "RESPONSE GETBEACONARRAY : " + response);
        try {
            JSONArray beaconsArray = response.getJSONArray("beaconArray");
            for (int i = 0; i < beaconsArray.length(); i++) {
                JSONObject beacon = (JSONObject) beaconsArray.get(i);
                String name = beacon.getString("name");
                String UUID = beacon.getString("UUID");
                int major = beacon.getInt("major");
                int minor = beacon.getInt("minor");
                listAllBeacons.add(new polytech.followit.model.Beacon(name, UUID, major, minor));
            }
            Log.d(TAG, "LIST ALL BEACONS : " + listAllBeacons.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        for (polytech.followit.model.Beacon beacon : listAllBeacons) {
            Region region = new Region(
                    beacon.getName(),
                    UUID.fromString(beacon.getUUID()),
                    beacon.getMajor(),
                    beacon.getMinor()
            );
            beaconManager.startMonitoring(region);
        }
    }

    private boolean isArrivalBeacon(polytech.followit.model.Beacon beacon) {
        if (path.getListNodes().get(path.getListNodes().size() - 1).getBeacon() != null) {
            return path.getListNodes().get(path.getListNodes().size() - 1).getBeacon().equals(beacon);
        } else return false;
    }

    private void sendMessage(int what, @Nullable Bundle data) {
        try {
            Message msg = Message.obtain(null, what);
            if (data != null) msg.setData(data);
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
