package com.followit.android;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.estimote.sdk.SystemRequirementsChecker;
import com.followit.android.rest.Path;
import com.followit.android.rest.SocketCallBack;
import com.followit.android.service.BeaconMonitoringService;
import com.followit.android.service.BroadcastResponseReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        SocketCallBack,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Path path;
    private ArrayList<String> shopList;
    private Button getPathButton;

    private GoogleApiClient googleApiClient;
    private PutDataMapRequest mapRequest;
    private PutDataRequest request;

    // Service variables
    private IntentFilter filter;
    private BroadcastReceiver broadcastReceiver = new BroadcastResponseReceiver();

    /***********************************/
    /**          LIFECYCLES           **/
    /***********************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // hide progressbar so it doesn't appear at first
        findViewById(R.id.pb).setVisibility(View.GONE);

        // Build a new GoogleApiClient for the Wearable API
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        path = new Path(MainActivity.this, this);
        getPathButton = (Button) findViewById(R.id.getPathButton);

        //GET SHOP LIST
        path.getShopList();
        // Set listeners
        getPathButton.setOnClickListener(this);

        // Service part
        startService(new Intent(this, BeaconMonitoringService.class));
        filter = new IntentFilter(BeaconMonitoringService.BEACON_DETECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);


        //POURQUOI ? SINON CA AFFICHE QUAND MEME....
        ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
        pb.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        googleApiClient.disconnect();
    }

    /***********************************/
    /**          LISTENERS            **/
    /***********************************/

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.getPathButton:
                getPath();
                //display progressbar
                getPathButton.setVisibility(View.GONE);
                ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
                pb.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onPathFetched(final ArrayList<String> nodes) {
        Log.d(TAG, "NOTIF: JSONOBJECT" + nodes.toString());

        // Todo: Tableau d'indications à mettre
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/indications");
        putDataMapReq.getDataMap().putString("indications", "TABLEAU");
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(googleApiClient, putDataReq);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView t = (TextView) findViewById(R.id.result_tv);
                t.setText(nodes.toString());
                //t.setFontFeatureSettings();
                t.setVisibility(View.VISIBLE);

                //display button again
                Button getPathButton = (Button) findViewById(R.id.getPathButton);
                getPathButton.setVisibility(View.VISIBLE);
                ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
                pb.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void shopListNotification(final ArrayList<String> list) {
        Log.d(TAG, "NOTIF: JSONOBJECT" + list.toString());

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, list);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                Spinner listSpinner = (Spinner) findViewById(R.id.select_shops_spinner);
                listSpinner.setAdapter(adapter);
            }
        });
    }

    @Override
    public void onBroadcastNotification(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("Map Update")
                        .setContentText("Map has been updated, synchronize your navigation steps");
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // notificationID allows you to update the notification later on.
                mNotificationManager.notify(1, mBuilder.build());
                //TODO: ADD action on notification click

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: " + bundle);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    /***********************************/
    /**          FUNCTIONS            **/
    /***********************************/

    private void getPath() {
        JSONObject itinerary = new JSONObject();
        try {
            itinerary.put("source", "a");
            itinerary.put("destination", "f");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        path.askForPath(itinerary);
    }
}
