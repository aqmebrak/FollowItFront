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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        SocketCallBack,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Path path;
    private Button getPathButton;

    private GoogleApiClient googleApiClient;
    private PutDataMapRequest mapRequest;
    private PutDataRequest request;
    private static ArrayList<String> instructions;

    // Service variables
    private IntentFilter filter;
    private BroadcastReceiver broadcastReceiver = new BroadcastResponseReceiver();


    MyCustomAdapter dataAdapter = null;

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

        //GET POI LIST
        path.getPOIList();
        // Set listeners
        getPathButton.setOnClickListener(this);

        // Service part
        //startService(new Intent(this, BeaconMonitoringService.class));
        //filter = new IntentFilter(BeaconMonitoringService.BEACON_DETECTED);
        //LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);

        //POURQUOI ? SINON CA AFFICHE QUAND MEME....
        ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
        pb.setVisibility(View.GONE);


        /***
         *
         * LIST VIEW CHECKBOX
         *
         */

        //Generate list View from ArrayList
        displayListView();

        checkButtonClick();
    }

    private void displayListView() {

        //Array list of countries
        ArrayList<POI> POIList = new ArrayList<POI>();


        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this,
                R.layout.country_info, POIList);
        ListView listView = (ListView) findViewById(R.id.poi_list);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                POI POI = (POI) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(),
                        "Clicked on Row: " + POI.getName(),
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    private class MyCustomAdapter extends ArrayAdapter<POI> {

        private ArrayList<POI> POIList;

        public MyCustomAdapter(Context context, int textViewResourceId,
                               ArrayList<POI> POIList) {
            super(context, textViewResourceId, POIList);
            this.POIList = new ArrayList<POI>();
            this.POIList.addAll(POIList);
        }

        private class ViewHolder {
            CheckBox name;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.country_info, null);

                holder = new ViewHolder();
                holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            POI POI = POIList.get(position);
            holder.name.setText(POI.getName());
            holder.name.setChecked(POI.isSelected());
            holder.name.setTag(POI);

            return convertView;

        }

    }

    private void checkButtonClick() {

        Button myButton = (Button) findViewById(R.id.findSelected);
        myButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                StringBuffer responseText = new StringBuffer();
                responseText.append("The following were selected...\n");

                ArrayList<POI> POIList = dataAdapter.POIList;
                for (int i = 0; i < POIList.size(); i++) {
                    POI POI = POIList.get(i);
                    if (POI.isSelected()) {
                        responseText.append("\n" + POI.getName());
                    }
                }

                Toast.makeText(getApplicationContext(),
                        responseText, Toast.LENGTH_LONG).show();

            }
        });

    }

    /**
     *
     *
     *
     *
     *
     *
     */

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
    public void onPathFetched(final ArrayList<Node> path) throws JSONException {
        instructions = new ArrayList<>();

        for (int i=0; i<path.size(); i++) {
            instructions.add(path.get(i).getInstruction());
        }

        // Todo: Tableau d'indications à mettre
        //PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/indications");
        //putDataMapReq.getDataMap().putString("indications", "TABLEAU");
        //PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        //PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(googleApiClient, putDataReq);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView t = (TextView) findViewById(R.id.result_tv);
                t.setText(path.toString());
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
    public void POIListNotification(final ArrayList<String> list) {
        Log.d(TAG, "NOTIF: POI LIST" + list.toString());

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

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
