package polytech.followit.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import polytech.followit.model.Node;
import polytech.followit.model.Path;
import polytech.followit.rest.SocketCallBack;
import polytech.followit.utility.PathSingleton;

public class BeaconMonitoringService extends Service implements
        BeaconManager.MonitoringListener,
        BeaconManager.ServiceReadyCallback,
        SocketCallBack {

    private static final String TAG = BeaconMonitoringService.class.getName();
    public static final String BEACON_DETECTED = "beacon detected";
    private BeaconManager beaconManager;
    private Region lastDetectedRegion = new Region("", null, null, null);

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"ON CREATE SERVICE");
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setBackgroundScanPeriod(1000, 5000);
        beaconManager.setRegionExitExpiration(5000);

        PathSingleton.getInstance().setSocketCallBack(this);
        PathSingleton.getInstance().getSocket().emit("getBeaconArray");
        PathSingleton.getInstance().getSocket().emit("getAllNodes");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
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
            if (!PathSingleton.getInstance().getPath().getListBeacons().contains(beacon)) {
                JSONObject o = new JSONObject();
                try {
                    o.put("source", getNodeNameFromBeacon(beacon));
                    o.put("destination", PathSingleton.getInstance().getPath().getDestination());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                PathSingleton.getInstance().askForPath(o);
            } else if (!lastDetectedRegion.equals(region)) {
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
    }

    private String getNodeNameFromBeacon(polytech.followit.model.Beacon beacon) {
        for (Node node : PathSingleton.getInstance().getListAllNodes()) {
            if (node.getBeacon().equals(beacon))
                return node.getName();
        }
        //// TODO: 26/01/2017 handle exception
        return null;
    }

    //==============================================================================================
    // Socket callbacks implementation
    //==============================================================================================

    @Override
    public void onPathFetched() throws JSONException {

    }

    @Override
    public void onBroadcastNotification(String message) {

    }

    @Override
    public void onPOIListFetched() {

    }

    @Override
    public void onBeaconsFetched() {
        Log.d(TAG, "ON BEACONS FETCHED");
        ArrayList<polytech.followit.model.Beacon> listBeacon = PathSingleton.getInstance().getListAllBeacons();
        for (polytech.followit.model.Beacon beacon : listBeacon) {
            Region region = new Region(
                    beacon.getName(),
                    UUID.fromString(beacon.getUUID()),
                    beacon.getMajor(),
                    beacon.getMinor()
            );
            beaconManager.startMonitoring(region);
        }
        Log.d(TAG,"ON BEACONS FETCHED" + listBeacon.toString());
    }

    @Override
    public void onNodesFetched() {
        Log.d(TAG, "ON NODES FETCHED");
        beaconManager.connect(this);
        beaconManager.setMonitoringListener(this);
    }
}
