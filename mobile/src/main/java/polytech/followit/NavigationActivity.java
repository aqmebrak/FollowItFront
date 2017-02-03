package polytech.followit;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import polytech.followit.model.Instruction;
import polytech.followit.model.Node;
import polytech.followit.model.POI;
import polytech.followit.rest.SocketCallBack;
import polytech.followit.service.BeaconMonitoringService;
import polytech.followit.utility.PathSingleton;


public class NavigationActivity extends FragmentActivity implements
        View.OnClickListener,
        SocketCallBack,
        NavigationFragment.OnFragmentInteractionListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, ServiceConnection {

    private static final String TAG = NavigationActivity.class.getSimpleName();

    //recupere la liste en RAW de la navigation
    ArrayList<Node> listNavigation = null;
    ArrayList<Instruction> navigationSteps;
    //Instruction en cours

    private ViewPager mPager;
    private List<Instruction> mInstructionData = new ArrayList<>();
    private NavigationFragmentAdapter mAdapter;

    private GoogleApiClient googleApiClient;

    // Messenger
    private Messenger messenger = null;
    private boolean isServiceBounded;

    //==============================================================================================
    // Lifecycle
    //==============================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "ON create - Navigation Activity");
        setContentView(R.layout.navigation_view_pager);
        navigationSteps = new ArrayList<>();
        PathSingleton.getInstance().setSocketCallBack(this);

        //Adapter pour créer toutes les vues du Pager
        mAdapter = new NavigationFragmentAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);

        //rempli les instructions sur chaque fragment
        prepareNavigationList();

        //lancement de laffichage en notifiant qu'on a construit les vues
        mAdapter.notifyDataSetChanged();

        // Needed to sync data between the watch and the smartphone
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        // Create and bind the service
        bindService(new Intent(this, BeaconMonitoringService.class), this, Context.BIND_AUTO_CREATE);

        syncDataWithService();
        syncDataWithWatch();
        sendNotification("NEXT_INSTRUCTION");

    }

    //==============================================================================================
    // Listeners implementation
    //==============================================================================================

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }


    @Override
    public void onPathFetched() throws JSONException {
        Log.d(TAG, "ON PATH FETCHED NavigationActivity");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPager.setAdapter(null);
                mAdapter = null;
                getSupportFragmentManager().getFragments().clear();
                mAdapter = new NavigationFragmentAdapter(getSupportFragmentManager());
                mPager.setAdapter(mAdapter);
                mInstructionData.clear();
                prepareNavigationList();
                mAdapter.notifyDataSetChanged();
            }
        });
        //syncDataWithService();
        //syncDataWithWatch();
    }


    @Override
    public void onBroadcastNotification(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                /*NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NavigationActivity.this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("Mise `a jour de la carte")
                        .setContentText("La carte a ete mise a jour, nous avons synchronise votre chemin");
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // notificationID allows you to update the notification later on.
                mNotificationManager.notify(1, mBuilder.build());

                //TODO: ADD action on notification click
                //ON redemande le chemin
                //on cherche l'étape active
                Log.d(TAG, "dEBUT DEMANDE DE CHEMIN");

                //Log.d(TAG, "source: " + ongoingInstruction.nodeToGoTo + " dest " + PathSingleton.getInstance().getPath().getDestination());
                JSONObject o = new JSONObject();
                try {
                    o.put("source", mInstructionData.get(PathSingleton.getInstance().getPath().getIndexOfInstruction()).nodeToGoTo);
                    o.put("destination", PathSingleton.getInstance().getPath().getDestination());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                PathSingleton.getInstance().askForPath(o);*/
            }
        });
    }

    @Override
    public void onPOIListFetched() {
    }

    @Override
    public void onSendNotificationRequest(String action) {
        Log.d(TAG, "onSendNotificationRequest : " + action);
        sendNotification(action);
    }

    /**
     * Fired when the arrival beacon is detected
     */
    @Override
    public void onArrival() {
        Log.d(TAG, "onArrival - Navigation Activity");
        unbindService(this);
        googleApiClient.disconnect();
        stopService(new Intent(this, BeaconMonitoringService.class));

    }


    //==============================================================================================
    // List view implementation
    //==============================================================================================

    private void prepareNavigationList() {
        //ON PREPARE LA LISTE DES ETAPES
        listNavigation = PathSingleton.getInstance().getPath().getListNodes();
        Log.d(TAG, "PREPARENAVIGATIONLIST");
        Log.d(TAG, listNavigation.toString());

        for (int i = 0; i < listNavigation.size(); i++) {

            Node n = listNavigation.get(i);
            String text = "";
            if (i != listNavigation.size() - 1) {
                text = "Instructions\n";
            }
            //si il y a des instructions

            if (!"".equals(n.getInstruction().getInstruction())) {
                text += n.getInstruction().getInstruction() + "\n";
            }
            //si il y a des POI
            if (n.getPoi() != null && !n.getPoi().isEmpty()) {
                text += "Points d'interets à proximité: \n";
                for (POI s : n.getPoi()) {
                    text += "\t- " + s.getName();
                }
            }
            //SI on est pas arrivé a la fin du tableau, on rentre le noeud/beacon ou on va arriver
            if (i < listNavigation.size() - 1) {
                if(i == 0)
                    text += "Déplacez vous vers le magasin le plus proche";
                Log.d(TAG, "if" + n.getName());
                Node nplusun = listNavigation.get(i + 1);
                navigationSteps.add(new Instruction(n.getName(), nplusun.getName(), text, null, n.getInstruction().getOrientationIcon(), null));
                mInstructionData.add(new Instruction(n.getName(), nplusun.getName(), text, null, n.getInstruction().getOrientationIcon(), null));
            } else {
                //sinon juste le noeud/beacon de depart
                text += "\nVous etes arrivé !";
                navigationSteps.add(new Instruction(null, n.getName(), text, null, n.getInstruction().getOrientationIcon(),null));
                //PAGER CONTENU
                mInstructionData.add(new Instruction(null, n.getName(), text, null, n.getInstruction().getOrientationIcon(), null));
            }
            //Log.d(TAG, text);
        }
    }

    //==============================================================================================
    // Utils
    //==============================================================================================

    private void sendNotification(String action) {
        Intent in = new Intent();
        in.setAction(action);
        int instructionIndex = PathSingleton.getInstance().getPath().getIndexOfInstruction();
        if (PathSingleton.getInstance().getPath().getListInstructions().get(instructionIndex) != null) {
            in.putExtra("instruction", PathSingleton.getInstance().getPath().getListInstructions().get(instructionIndex).getInstruction());
            if (PathSingleton.getInstance().getPath().getListInstructions().get(instructionIndex).getOrientationIcon() != -1)
                in.putExtra("icon", PathSingleton.getInstance().getPath().getListInstructions().get(instructionIndex).getOrientationIcon());
        }
        sendBroadcast(in);
    }

    private void syncDataWithWatch() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/instructions");
        ArrayList<String> instructions = PathSingleton.getInstance().getPath().listInstructionsToStringArray();
        int indexOfInstruction = PathSingleton.getInstance().getPath().getIndexOfInstruction();
        String orientation = PathSingleton.getInstance().getPath().getListInstructions().get(indexOfInstruction).getOrientation();
        String timestamp = Long.toString(System.currentTimeMillis());

        putDataMapReq.getDataMap().putStringArrayList("instructions", instructions);
        putDataMapReq.getDataMap().putInt("indexOfInstruction", indexOfInstruction);
        putDataMapReq.getDataMap().putString("orientation", orientation);
        putDataMapReq.getDataMap().putString("timestamp", timestamp);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
    }

    private void syncDataWithService() {
        Intent serviceIntent = new Intent(this, BeaconMonitoringService.class);
        serviceIntent.putExtra("path", PathSingleton.getInstance().getPath());
        startService(serviceIntent);
    }

    //==============================================================================================
    // Fragments implementation
    //==============================================================================================

    @Override
    public void onFragmentInteraction(int currentDataPosition) {

    }

    @Override
    public void onFragmentCreated(NavigationFragment navigationFragment) {
        //Log.d("ViewPagerDemo", "Fragment inflated: " + navigationFragment.getData().instruction);
    }

    @Override
    public void onFragmentResumed(NavigationFragment navigationFragment) {
        //Log.d("ViewPagerDemo", "Fragment resumed: " + navigationFragment.getData().instruction);
    }

    private class NavigationFragmentAdapter extends FragmentPagerAdapter {
        NavigationFragmentAdapter(FragmentManager fm) {
            super(fm); // super tracks this
        }

        @Override
        public Fragment getItem(int position) {
            return NavigationFragment.newInstance(mInstructionData.get(position));
        }

        @Override
        public int getCount() {
            //Log.d(TAG, "get count du fragment " + mInstructionData.size());
            return mInstructionData.size();
        }

        @Override
        public int getItemPosition(Object object) {
            Log.d(TAG, "getItemPosition");
            return POSITION_NONE;
        }
    }

    //==============================================================================================
    // Google Api Client implementation
    //==============================================================================================

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
