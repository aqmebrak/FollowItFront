package polytech.followit;


import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
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
import polytech.followit.service.NotificationBroadcast;
import polytech.followit.utility.PathSingleton;

public class MainActivity extends AppCompatActivity implements
        SocketCallBack,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String FIRST_INSTRUCTION = "first instruction";

    private GoogleApiClient googleApiClient;
    private PutDataMapRequest mapRequest;
    private PutDataRequest request;

    private LocationManager locationManager;
    private Location location;

    // Service variables
    private IntentFilter filter;
    private BroadcastReceiver broadcastReceiver = new NotificationBroadcast();

    //POI DE DEPART ET DESTINATION
    public String depart;
    public String arrivee;
    ProgressDialog progressDialog;

    MyCustomAdapter dataAdapter = null;

    //==============================================================================================
    // Lifecycle
    //==============================================================================================

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

    }


    @Override
    protected void onResume() {
        super.onResume();
        //On réaffiche le bouton au lieu du progressbar
        ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
        pb.setVisibility(View.GONE);
        Button getPathButton = (Button) findViewById(R.id.getPathButton);
        getPathButton.setVisibility(View.VISIBLE);

        //truc
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, PackageManager.PERMISSION_GRANTED);
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        //SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //googleApiClient.disconnect();
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
                /*Timer timer = new Timer();
                TimerTask myTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (location != null) {
                            onPathClicked();
                            this.cancel();
                        }
                    }
                };
                timer.schedule(myTask, 2000, 2000);
                break;*/
        }
    }

    @Override
    public void onPathFetched() throws JSONException {
        Log.d(TAG, "ONPATHFETCHED");
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/startActivity");
        ArrayList<String> instructions = PathSingleton.getInstance().getPath().listInstructionsToStringArray();
        String timestamp = Long.toString(System.currentTimeMillis());
        putDataMapReq.getDataMap().putStringArrayList("instructions", instructions);
        putDataMapReq.getDataMap().putString("timestamp", timestamp);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(googleApiClient, putDataReq);

        // Start service TODO: Change with parcelable ++fast
        Intent serviceIntent = new Intent(this, BeaconMonitoringService.class);
        serviceIntent.putExtra("path", PathSingleton.getInstance().getPath());
        startService(serviceIntent);
        filter = new IntentFilter(BeaconMonitoringService.BEACON_DETECTED);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);

        sendNotification("polytech.followit.FIRST_INSTRUCTION");

        // Set direction for first time
        /*for (Node node : PathSingleton.getInstance().getPath().getListNodes()) {
            if (node.hasBeacon()) {
                Node departure = PathSingleton.getInstance().getPath().getListNodes().get(0);
                Node arrival = node;
                double angle;
                if (arrival.getxCoord() >= departure.getxCoord()) {
                    angle = DirectionFinder.angleBetweenTwoNode(departure, arrival);
                }
                else {
                    angle = -1 * DirectionFinder.angleBetweenTwoNode(departure, arrival);
                }
                PathSingleton.getInstance().setAngleDeviationToNextBeacon(angle);
            }
        }*/

        Intent intent = new Intent(this, NavigationActivity.class);
        intent.putExtra("location", location);
        startActivity(intent);
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
    // Location implementation
    //==============================================================================================

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
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
    // Utils implementation
    //==============================================================================================

    public void sendNotification(String action) {
        Intent in = new Intent();
        in.setAction("polytech.followit.FIRST_INSTRUCTION");
        if (PathSingleton.getInstance().getPath().getListInstructions().get(1) != null)
            in.putExtra("firstInstruction", PathSingleton.getInstance().getPath().getListInstructions().get(1).getInstruction());
        sendBroadcast(in);
    }
}
