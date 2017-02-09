package polytech.followit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.estimote.sdk.SystemRequirementsChecker;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

import polytech.followit.adapter.MyCustomPOIListAdapter;
import polytech.followit.model.POI;
import polytech.followit.rest.SocketCallBack;
import polytech.followit.utility.PathSingleton;

/**
 * Created by Akme on 05/02/2017.
 */

public class SelectDepartureActivity extends AppCompatActivity implements
        SocketCallBack,
        View.OnClickListener {

    private static final String TAG = SelectDepartureActivity.class.getSimpleName();

    MyCustomPOIListAdapter dataAdapter = null;
    ProgressDialog progressDialog;

    //==============================================================================================
    // Lifecycle
    //==============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.departure_activity);

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        PathSingleton.getInstance().setSocketCallBack(this);

        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setOnClickListener(this);

        Log.d(TAG, "GET POI LIST");
        //GET POI LIST
        PathSingleton.getInstance().askPOIList();
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
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
    }

    @Override
    protected  void onStop(){
        super.onStop();
        progressDialog.dismiss();
    }

    //==============================================================================================
    // Listeners implementations
    //==============================================================================================

    @Override
    public void onPathFetched() throws JSONException {

    }

    @Override
    public void onBroadcastNotification(String message) {

    }

    @Override
    public void onInvalidPath(){

    }

    @Override
    public void onPOIListFetched() {
        Log.d(TAG, "NOTIF: POI LIST" + PathSingleton.getInstance().getListAllPoi().toString());

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Generate list View from ArrayList
                displayListView(PathSingleton.getInstance().getListAllPoi());
                progressDialog.hide();

            }
        });
    }

    @Override
    public void onSendNotificationRequest(String action) {
        //NOT USED
    }

    @Override
    public void onArrival() {
        //NOT USED
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nextButton:
                Log.d(TAG, "click " + getSelectedRadioButton());
                progressDialog.dismiss();
                if(!Objects.equals(getSelectedRadioButton(), "")){
                    Intent mainIntent = new Intent(SelectDepartureActivity.this, SelectArrivalActivity.class);
                    mainIntent.putExtra("source", getSelectedRadioButton());
                    startActivity(mainIntent);
                    finish();
                }else{
                    Toast.makeText(this,"Veuillez choisir un point de départ", Toast.LENGTH_SHORT).show();
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
        ListView listView = (ListView) findViewById(R.id.poi_list_departure);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);
    }

    private String getSelectedRadioButton() {
        String selected = "";

        ArrayList<POI> POIList = dataAdapter.getPOIList();
        for (int i = 0; i < POIList.size(); i++) {

            POI p = POIList.get(i);
            if (p.isSelected()) {
                //Log.d(TAG, "SELECTEDCHECKBOX :" + p.getName());
                selected = p.getName();
                break;
            }
        }
        return selected;
    }

}
