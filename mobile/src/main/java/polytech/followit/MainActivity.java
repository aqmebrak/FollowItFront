package polytech.followit;


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
import android.support.v4.content.ContextCompat;
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
import java.util.Timer;
import java.util.TimerTask;

import polytech.followit.model.Node;
import polytech.followit.model.POI;
import polytech.followit.rest.Path;
import polytech.followit.rest.SocketCallBack;
import polytech.followit.service.BroadcastResponseReceiver;

public class MainActivity extends AppCompatActivity implements
        SocketCallBack,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener,
        LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient googleApiClient;
    private PutDataMapRequest mapRequest;
    private PutDataRequest request;
    private static ArrayList<String> instructions;

    private LocationManager locationManager;
    private Location location;

    // Service variables
    private IntentFilter filter;
    private BroadcastReceiver broadcastReceiver = new BroadcastResponseReceiver();

    //POI DE DEPART ET DESTINATION
    public String depart;
    public String arrivee;

    //SOCKET CLASS
    Path path;

    MyCustomAdapter dataAdapter = null;

    //==============================================================================================
    // Lifecycle
    //==============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Build a new GoogleApiClient for the Wearable API
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        path = new Path(this);

        Button getPathButton = (Button) findViewById(R.id.getPathButton);

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
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, PackageManager.PERMISSION_GRANTED);
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        //googleApiClient.connect();
        //SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //googleApiClient.disconnect();
    }

    /***********************************/
    /**          LISTENERS            **/
    /***********************************/

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.getPathButton:

                Button getPathButton = (Button) findViewById(R.id.getPathButton);
                getPathButton.setVisibility(View.GONE);

                Timer timer = new Timer();
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

        /*
         * SWITCHER VUE
         */
        Intent intent = new Intent(getBaseContext(), NavigationActivity.class);
        intent.putExtra("nodeList", path);
        intent.putExtra("source", this.path.source);
        intent.putExtra("destination", this.path.destination);
        intent.putExtra("location", location);
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
        //NOT USED
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        depart = (String) parent.getItemAtPosition(position);
        Log.d(TAG, "ITEM SPINNER SELECTED" + depart);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    /***********************************/
    /**          FUNCTIONS            **/
    /***********************************/

    private void getPath() {
        //NOEUD DEPART ET ARRIVEE
        String nodeSource = "";
        String NodeDestination = "";

        //Pour chaque POI cliqué par l'user, on cherche son noeud correspondant
        for (POI p : path.getPOIList()) {
            if (Objects.equals(p.getName(), depart)) {
                nodeSource = p.getNode();
            } else if (Objects.equals(p.getName(), arrivee)) {
                NodeDestination = p.getNode();
            }
        }
        Log.d(TAG, "GETPATH: " + nodeSource + " " + NodeDestination);
        //si nos deux noeuds ont été bien récupérés, on créé le JSON
        if (!Objects.equals(nodeSource, "") && !Objects.equals(NodeDestination, "")) {
            //on en profite pour enregistrer les noeuds dans PATH
            path.source = nodeSource;
            path.destination = NodeDestination;
            JSONObject itinerary = new JSONObject();
            try {
                itinerary.put("source", nodeSource);
                itinerary.put("destination", NodeDestination);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //On appelle le socket.emit demandant le chemin
            Log.d(TAG,"GETPATH JSON : " + itinerary.toString());
            path.askForPath(itinerary);
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


    /***********************************/
    /**          LIST CHECKBOX        **/
    /***********************************/

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
                if (poi.getName() != path.source) {
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }
}
