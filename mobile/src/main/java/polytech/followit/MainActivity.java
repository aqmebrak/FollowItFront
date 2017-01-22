package polytech.followit;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import polytech.followit.model.Node;
import polytech.followit.model.POI;
import polytech.followit.rest.Path;
import polytech.followit.rest.SocketCallBack;
import polytech.followit.service.BroadcastResponseReceiver;

public class MainActivity extends AppCompatActivity implements SocketCallBack,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleApiClient googleApiClient;
    private PutDataMapRequest mapRequest;
    private PutDataRequest request;
    private static ArrayList<String> instructions;

    // Service variables
    private IntentFilter filter;
    private BroadcastReceiver broadcastReceiver = new BroadcastResponseReceiver();


    //SOCKET CLASS
    Path path;

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
                Button getPathButton = (Button) findViewById(R.id.getPathButton);
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
                ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
                pb.setVisibility(View.GONE);
                Button getPathButton = (Button) findViewById(R.id.getPathButton);
                getPathButton.setVisibility(View.VISIBLE);
            }
        });

        /*
         * SWITCHER VUE
         */
        Intent intent = new Intent(getBaseContext(), NavigationActivity.class);
        intent.putExtra("nodeList", path);
        Log.d(TAG, this.path.source + this.path.destination);
        intent.putExtra("source", this.path.source.toString());
        intent.putExtra("destination", this.path.destination.toString());
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

        //Pour chaque POI cliqué par l'user, on cherche son noeud correspondant
        for (POI p : path.getPOIList()) {
            Log.d(TAG, p.toString());
            if (p.getName() == POIsource) {
                source = p.getNode();
            } else if (p.getName() == POIdestination) {
                destination = p.getNode();
            }
        }

        //si nos deux noeuds ont été bien récupérés, on créé le JSON
        if (source != "" && destination != "") {
            //on en profite pour enregistrer les noeuds dans SINGLETON PATH
            path.source = source;
            path.destination = destination;
            JSONObject itinerary = new JSONObject();
            try {
                itinerary.put("source", source);
                itinerary.put("destination", destination);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //On appelle le socket.emit demandant le chemin
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



    //TOAST CE QUON A COCHE
    private String getSelectedCheckbox() {
        String selected = "";

        ArrayList<POI> POIList = dataAdapter.getPOIList();
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
