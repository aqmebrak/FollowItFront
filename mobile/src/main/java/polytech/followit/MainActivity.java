package polytech.followit;


import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import polytech.followit.adapter.MyCustomPOIListAdapter;
import polytech.followit.model.POI;
import polytech.followit.rest.SocketCallBack;
import polytech.followit.service.BeaconMonitoringService;
import polytech.followit.utility.PathSingleton;

public class MainActivity extends AppCompatActivity implements
        SocketCallBack,
        View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    //POI DE DEPART ET DESTINATION
    public String depart;
    public String arrivee;
    ProgressDialog progressDialog;

    MyCustomPOIListAdapter dataAdapter = null;

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

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
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
        // Dismiss progressDialog hence avoid leak windows
        progressDialog.dismiss();
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
    public void onSendNotificationRequest(String action) {

    }

    @Override
    public void onArrival() {

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
        dataAdapter = new MyCustomPOIListAdapter(this,
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
}
