package polytech.followit.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
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

public class BeaconMonitoringService extends Service implements
        BeaconManager.MonitoringListener,
        BeaconManager.ServiceReadyCallback {

    private static final String TAG = BeaconMonitoringService.class.getName();
    public static final String BEACON_DETECTED = "beacon detected";
    public static final String FIRST_INSTRUCTION = "first instruction";
    public static final int MSG_TEST = 1;

    private BeaconManager beaconManager;
    private Region lastDetectedRegion = new Region("", null, null, null);

    private Socket socket;
    private ArrayList<polytech.followit.model.Beacon> listAllBeacons;
    private ArrayList<Node> listAllNodes;
    private Path path;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"ON CREATE SERVICE");
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setBackgroundScanPeriod(1000, 5000);
        beaconManager.setRegionExitExpiration(5000);

        beaconManager.connect(this);
        beaconManager.setMonitoringListener(this);

        try {
            socket = IO.socket("https://followit-backend.herokuapp.com/");
        } catch (URISyntaxException e) {
            Log.d(TAG, "Couldn't socket" + e);
        }

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }

        }).on("beaconArray", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                buildAllBeacons(args);
            }
        }).on("allNodes", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                buildAllNodes(args);
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        });
        socket.connect();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        path = (Path) intent.getExtras().get("path");
        return START_STICKY;
    }

    @Override
    public void onEnteredRegion(Region region, List<Beacon> list) {
        Log.d(TAG, "ON ENTERED REGION");
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
            // beacon non prévu dans le chemin
            /*if (!PathSingleton.getInstance().getPath().getListBeacons().contains(beacon)) {
                JSONObject o = new JSONObject();
                try {
                    o.put("source", getNodeNameFromBeacon(beacon));
                    o.put("destination", PathSingleton.getInstance().getPath().getDestination());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                PathSingleton.getInstance().askForPath(o);
            } else*/ if (!lastDetectedRegion.equals(region)) {
                Log.d(TAG, "BEACON DETECTED");
                Intent in = new Intent(BEACON_DETECTED);
                in.putExtra("region", region);
                LocalBroadcastManager.getInstance(this).sendBroadcast(in);
                lastDetectedRegion = region;
            }
        }
    }

    @Override
    public void onExitedRegion(Region region) {

    }

    @Override
    public void onServiceReady() {
        socket.emit("getAllNodes");
        socket.emit("getBeaconArray");
    }

    private String getNodeNameFromBeacon(polytech.followit.model.Beacon beacon) {
        for (Node node : listAllNodes) {
            if (node.getBeacon().equals(beacon))
                return node.getName();
        }
        //// TODO: 26/01/2017 handle exception
        return null;
    }

    //==============================================================================================
    // Utils
    //==============================================================================================

    private void buildAllNodes(Object... args) {
        JSONObject response = (JSONObject) args[0];
        listAllNodes = new ArrayList<>();
        Log.d(TAG,"RESPONSE GET ALL NODES" + response);
        try {
            JSONArray nodesArray = response.getJSONArray("nodes");
            for (int i = 0; i < nodesArray.length(); i++) {
                JSONObject node = nodesArray.getJSONObject(i);
                JSONObject node_value = node.getJSONObject("value");

                String name = node.getString("v");

                JSONArray poi = node_value.getJSONArray("POI");
                ArrayList<POI> listPoi = new ArrayList<>();
                for (int j = 0; j < poi.length(); j++) {
                    POI node_poi = new POI(poi.getString(j),null,false);
                    listPoi.add(node_poi);
                }

                JSONObject node_value_coord = (JSONObject) node_value.get("coord");
                double xCoord = node_value_coord.getDouble("x");
                double yCoord = node_value_coord.getDouble("y");

                polytech.followit.model.Beacon beacon = null;
                if (node_value.has("beacon")) {
                    JSONObject node_value_beacon = node_value.getJSONObject("beacon");
                    beacon = new polytech.followit.model.Beacon(
                            node_value_beacon.getString("name"),
                            node_value_beacon.getString("UUID"),
                            node_value_beacon.getInt("major"),
                            node_value_beacon.getInt("minor")
                    );
                }

                Node newNode = new Node(name, listPoi, null, xCoord, yCoord, beacon);
                listAllNodes.add(newNode);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void buildAllBeacons(Object... args) {
        JSONObject response = (JSONObject) args[0];
        listAllBeacons = new ArrayList<>();
        Log.d(TAG,"RESPONSE GETBEACONARRAY : "+response);
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
            Log.d(TAG,"LIST ALL BEACONS : "+listAllBeacons.toString());
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

    //==============================================================================================
    // Messaging implementation
    //==============================================================================================

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TEST:
                    Toast.makeText(getApplicationContext(), "hello!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        return mMessenger.getBinder();
    }
}
