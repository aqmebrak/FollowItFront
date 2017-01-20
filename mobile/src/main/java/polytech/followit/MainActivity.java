package polytech.followit;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.SystemRequirementsChecker;
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

import polytech.followit.rest.Path;
import polytech.followit.rest.SocketCallBack;
import polytech.followit.service.BroadcastResponseReceiver;

public class MainActivity extends AppCompatActivity implements
        SocketCallBack,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener {

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

        Log.d(TAG, "GET POI LIST");
        //GET POI LIST
        path.askPOIList();
        // Set listeners
        getPathButton.setOnClickListener(this);
        Spinner listSpinner = (Spinner) findViewById(R.id.select_shops_spinner);
        listSpinner.setOnItemSelectedListener(this);

        // Service part
        //startService(new Intent(this, BeaconMonitoringService.class));
        //filter = new IntentFilter(BeaconMonitoringService.BEACON_DETECTED);
        //LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);

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
                String destination = null;

                //on recupere le val du dropdown
                if (path.source != null) {
                    path.destination = getSelectedCheckbox();
                    if (path.destination != null) {
                        Log.d(TAG, "BUTTON" + path.source + path.destination);
                        getPath(path.source, path.destination);
                    }
                }

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

        for (int i = 0; i < path.size(); i++) {
            instructions.add(path.get(i).getInstruction());
        }

        // Todo: Tableau d'indications à mettre
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/startActivity");
        putDataMapReq.getDataMap().putStringArrayList("instructions", instructions);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(googleApiClient, putDataReq);

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

        /*
         * SWITCHER VUE
         */
        Intent intent = new Intent(getBaseContext(), NavigationActivity.class);
        intent.putExtra("nodeList", path);
        startActivity(intent);
    }

    @Override
    public void POIListNotification(final ArrayList<POI> list) {
        Log.d(TAG, "NOTIF: POI LIST" + list.toString());

        final ArrayList<String> POIonly = new ArrayList<>();

        for (POI p : list) {
            POIonly.add(p.getName());
        }
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Liste Dropdown pour le départ
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, POIonly);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                Spinner listSpinner = (Spinner) findViewById(R.id.select_shops_spinner);
                listSpinner.setAdapter(adapter);

                //Generate list View from ArrayList
                displayListView(list);
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = (String) parent.getItemAtPosition(position);
        Log.d(TAG, "ITEM SPINNER SELECTED" + item);
        path.source = item;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    /***********************************/
    /**          FUNCTIONS            **/
    /***********************************/

    private void getPath(String POIsource, String POIdestination) {
        String source = "";
        String destination = "";

        for (POI p : path.getPOIList()) {
            Log.d(TAG, p.toString());
            if (p.getName() == POIsource) {
                source = p.getNode();
            } else if (p.getName() == POIdestination) {
                destination = p.getNode();
            }

        }
        if (source != "" && destination != "") {
            Log.d(TAG, "source et destination OK\n");
            JSONObject itinerary = new JSONObject();
            try {
                itinerary.put("source", source);
                itinerary.put("destination", destination);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            path.askForPath(itinerary);
        }
    }


    /***********************************/
    /**          LIST CHECKBOX        **/
    /***********************************/

    private void displayListView(ArrayList<POI> POIList) {

        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this,
                R.layout.country_info, POIList);
        ListView listView = (ListView) findViewById(R.id.poi_list);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
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
            holder.name.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    POI p = (POI) cb.getTag();
                    Toast.makeText(getApplicationContext(),
                            "Clicked on Checkbox: " + cb.getText() +
                                    " is " + cb.isChecked(),
                            Toast.LENGTH_LONG).show();
                    p.setSelected(cb.isChecked());
                }
            });
            return convertView;

        }

    }

    //TOAST CE QUON A COCHE
    private String getSelectedCheckbox() {
        String selected = "";

        ArrayList<POI> POIList = dataAdapter.POIList;
        for (int i = 0; i < POIList.size(); i++) {

            POI p = POIList.get(i);
            Log.d(TAG, p.toString());
            if (p.isSelected()) {
                Log.d(TAG, "SELECTED");
                selected = p.getName();
                break;
            }
        }
        return selected;
    }

    /**
     * FIN LISTE
     */
}
