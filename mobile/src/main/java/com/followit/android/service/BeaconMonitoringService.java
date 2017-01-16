package com.followit.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.UUID;

public class BeaconMonitoringService extends Service implements
        BeaconManager.MonitoringListener,
        BeaconManager.ServiceReadyCallback {

    private static final String TAG = BeaconMonitoringService.class.getName();
    public static final String BEACON_DETECTED = "beacon detected";
    private static final String UUID_STRING = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private BeaconManager beaconManager;
    private Region beaconIce, beaconMint;
    private Region lastDetectedRegion = new Region("",null,null,null);

    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setBackgroundScanPeriod(1000,5000);
        beaconManager.setRegionExitExpiration(5000);

        beaconIce = new Region("ice", UUID.fromString(UUID_STRING), 31250, 14836);
        beaconMint = new Region("mint", UUID.fromString(UUID_STRING), 14878, 34184);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        beaconManager.connect(this);
        beaconManager.setMonitoringListener(this);

        return START_STICKY;
    }

    @Override
    public void onEnteredRegion(Region region, List<Beacon> list) {
        //Log.d(TAG,"region : "+region);
        //Log.d(TAG,"list beacon : "+list);
        if (!lastDetectedRegion.equals(region)) {
            Log.d(TAG, "BEACON DETECTED");
            Intent in = new Intent(BEACON_DETECTED);
            in.putExtra("region", region);
            LocalBroadcastManager.getInstance(this).sendBroadcast(in);
            lastDetectedRegion = region;
        }
    }

    @Override
    public void onExitedRegion(Region region) {

    }

    @Override
    public void onServiceReady() {
        beaconManager.startMonitoring(beaconIce);
        beaconManager.startMonitoring(beaconMint);
    }
}
