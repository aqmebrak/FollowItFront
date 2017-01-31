package polytech.followit;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

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
import java.util.Objects;

import polytech.followit.model.POI;
import polytech.followit.rest.SocketCallBack;
import polytech.followit.service.BeaconMonitoringService;
import polytech.followit.service.MessageHandler;
import polytech.followit.service.NotificationBroadcast;
import polytech.followit.utility.PathSingleton;

public class MainActivity extends AppCompatActivity implements
        SocketCallBack,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        ServiceConnection {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient googleApiClient;

    //POI DE DEPART ET DESTINATION
    public String depart;
    public String arrivee;
    ProgressDialog progressDialog;

    MyCustomAdapter dataAdapter = null;

    // Messenger
    private Messenger messenger = null;
    private boolean isServiceBounded;


    //==============================================================================================
    // Lifecycle
    //==============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        // hide progressbar so it doesn't appear at first
        findViewById(R.id.pb).setVisibility(View.GONE);

        // Build a new GoogleApiClient for the Wearable API
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        PathSingleton.getInstance().setSocketCallBack(this);

        Button getPathButton = (Button) findViewById(R.id.getPathButton);

        Log.d(TAG, "GET POI LIST");
        //GET POI LIST
        PathSingleton.getInstance().askPOIList();

        // Set listeners
        getPathButton.setOnClickListener(this);
        Spinner listSpinner = (Spinner) findViewById(R.id.select_shops_spinner);
        listSpinner.setOnItemSelectedListener(this);

        //POURQUOI ? SINON CA AFFICHE QUAND MEME....
        ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
        pb.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
        progressDialog.show();

        // Create and bind the service
        bindService(new Intent(this, BeaconMonitoringService.class), this, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //On réaffiche le bouton au lieu du progressbar
        ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
        pb.setVisibility(View.GONE);
        Button getPathButton = (Button) findViewById(R.id.getPathButton);
        getPathButton.setVisibility(View.VISIBLE);

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"On destroy");
        if (isServiceBounded) {
            unbindService(this);
            isServiceBounded = false;
        }
    }

    //==============================================================================================
    // Listeners implementations
    //==============================================================================================

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.getPathButton:
                Log.d(TAG, "click");
                Button getPathButton = (Button) findViewById(R.id.getPathButton);
                getPathButton.setVisibility(View.GONE);
                ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
                pb.setVisibility(View.VISIBLE);

                onPathClicked();
        }
    }

    /**
     * Fired when we received the path
     * @throws JSONException
     */
    @Override
    public void onPathFetched() throws JSONException {
        Log.d(TAG, "ONPATHFETCHED");

        syncDataWithService();
        syncDataWithWatch();
        sendNotification("FIRST_INSTRUCTION");
        startActivity(new Intent(this, NavigationActivity.class));
    }

    @Override
    public void onPOIListFetched() {
        Log.d(TAG, "NOTIF: POI LIST" + PathSingleton.getInstance().getListAllPoi().toString());

        final ArrayList<String> POInameSpinner = new ArrayList<>();

        for (POI p : PathSingleton.getInstance().getListAllPoi())
            POInameSpinner.add(p.getName());

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Liste Dropdown pour le départ
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, POInameSpinner);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                Spinner listSpinner = (Spinner) findViewById(R.id.select_shops_spinner);
                listSpinner.setAdapter(adapter);

                //Generate list View from ArrayList
                displayListView(PathSingleton.getInstance().getListAllPoi());
                progressDialog.hide();
            }
        });
    }

    @Override
    public void onBroadcastNotification(final String message) {
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        depart = (String) parent.getItemAtPosition(position);
        Log.d(TAG, "ITEM SPINNER SELECTED" + depart);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    //==============================================================================================
    // Private utils functions
    //==============================================================================================

    private void getPath() {
        //NOEUD DEPART ET ARRIVEE
        String nodeSource = "";
        String nodeDestination = "";

        //Pour chaque POI cliqué par l'user, on cherche son noeud correspondant
        for (POI p : PathSingleton.getInstance().getListAllPoi()) {
            if (Objects.equals(p.getName(), depart)) {
                nodeSource = p.getNode();
            } else if (Objects.equals(p.getName(), arrivee)) {
                nodeDestination = p.getNode();
            }
        }
        Log.d(TAG, "GETPATH: " + nodeSource + " " + nodeDestination);
        //si nos deux noeuds ont été bien récupérés, on créé le JSON
        if (!Objects.equals(nodeSource, "") && !Objects.equals(nodeDestination, "")) {
            //on en profite pour enregistrer les noeuds dans PATH
            PathSingleton.getInstance().getPath().setSource(nodeSource);
            PathSingleton.getInstance().getPath().setDestination(nodeDestination);
            JSONObject itinerary = new JSONObject();
            try {
                itinerary.put("source", nodeSource);
                itinerary.put("destination", nodeDestination);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //On appelle le socket.emit demandant le chemin
            Log.d(TAG, "GETPATH JSON : " + itinerary.toString());
            PathSingleton.getInstance().askForPath(itinerary);
        }
    }

    private void onPathClicked() {
        //on recupere le val du checkbox list
        if (depart != null) {
            arrivee = getSelectedCheckbox();
            if (arrivee != null) {
                getPath();
            }
        }
    }

    private void sendNotification(String action) {
        Intent in = new Intent();
        in.setAction("polytech.followit.FIRST_INSTRUCTION");
        if (PathSingleton.getInstance().getPath().getListInstructions().get(1) != null)
            in.putExtra("firstInstruction", PathSingleton.getInstance().getPath().getListInstructions().get(1).getInstruction());
        sendBroadcast(in);
    }

    private void syncDataWithWatch() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/instructions");
        ArrayList<String> instructions = PathSingleton.getInstance().getPath().listInstructionsToStringArray();
        String timestamp = Long.toString(System.currentTimeMillis());
        putDataMapReq.getDataMap().putStringArrayList("instructions", instructions);
        putDataMapReq.getDataMap().putString("timestamp", timestamp);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
    }

    private void  syncDataWithService() {
        Intent serviceIntent = new Intent(this,BeaconMonitoringService.class);
        serviceIntent.putExtra("path", PathSingleton.getInstance().getPath());
        startService(serviceIntent);
    }

    //==============================================================================================
    // List view implementation
    //==============================================================================================

    private void displayListView(ArrayList<POI> POIList) {
        //create an ArrayAdaptar from the String Array
        dataAdapter = new MyCustomAdapter(this,
                R.layout.poi_info, POIList);
        ListView listView = (ListView) findViewById(R.id.poi_list);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                POI poi = (POI) parent.getItemAtPosition(position);
                CheckBox cb = (CheckBox) view.findViewById(R.id.checkBox1);
                if (!Objects.equals(poi.getName(), PathSingleton.getInstance().getPath().getSource())) {
                    if (poi.isSelected()) {
                        poi.setSelected(false);
                        cb.setChecked(false);
                    } else {
                        poi.setSelected(true);
                        cb.setChecked(true);
                    }
                }
            }
        });
    }

    private String getSelectedCheckbox() {
        String selected = "";

        ArrayList<POI> POIList = dataAdapter.getPOIList();
        for (int i = 0; i < POIList.size(); i++) {

            POI p = POIList.get(i);
            if (p.isSelected()) {
                Log.d(TAG, "SELECTEDCHECKBOX :" + p.getName());
                selected = p.getName();
                break;
            }
        }
        return selected;
    }

    //==============================================================================================
    // GoogleApiClient service implementation
    //==============================================================================================

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: " + bundle);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed: " + result);
    }

    //==============================================================================================
    // Service Messaging implementation
    //==============================================================================================

    // This is called when the connection with the service has been
    // established, giving us the object we can use to
    // interact with the service.  We are communicating with the
    // service using a Messenger, so here we get a client-side
    // representation of that from the raw IBinder object.
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.d(TAG, "Service connected, ready to be bind");
        messenger = new Messenger(iBinder);
        isServiceBounded = true;
    }

    // This is called when the connection with the service has been
    // unexpectedly disconnected -- that is, its process crashed.
    @Override
    public void onServiceDisconnected(ComponentName className) {
        Log.e(TAG, "Service disconnected");
        messenger = null;
        isServiceBounded = false;
    }
}
