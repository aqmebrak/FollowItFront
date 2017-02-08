package polytech.followit;

import android.app.NotificationManager;
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
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import polytech.followit.model.Discount;
import polytech.followit.model.Instruction;
import polytech.followit.model.Node;
import polytech.followit.model.POI;
import polytech.followit.rest.SocketCallBack;
import polytech.followit.service.BeaconMonitoringService;
import polytech.followit.utility.PathSingleton;


public class NavigationActivity extends FragmentActivity implements
        SocketCallBack,
        NavigationFragment.OnFragmentInteractionListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ServiceConnection,
        ViewPager.OnPageChangeListener {

    private static final String TAG = NavigationActivity.class.getSimpleName();


    private ViewPager mPager;
    private List<Instruction> mInstructionData = new ArrayList<>();
    private Instruction currentInstruction;

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
        PathSingleton.getInstance().setSocketCallBack(this);


        //Adapter pour créer toutes les vues du Pager
        NavigationFragmentAdapter mAdapter = new NavigationFragmentAdapter(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(this);
        //rempli les instructions sur chaque fragment
        prepareNavigationList();
        currentInstruction = mInstructionData.get(0);


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
        sendNotificationOnPhone("NEXT_INSTRUCTION");

    }

    //==============================================================================================
    // Listeners implementation
    //==============================================================================================


    @Override
    public void onPathFetched() throws JSONException {
        Log.d(TAG, "ON PATH FETCHED NavigationActivity");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mInstructionData.clear();
                mPager.setAdapter(null);
                NavigationFragmentAdapter mAdapter = new NavigationFragmentAdapter(getSupportFragmentManager());
                mPager.setAdapter(mAdapter);
                prepareNavigationList();
                mAdapter.notifyDataSetChanged();
            }
        });
        syncDataWithService();
        syncDataWithWatch();
    }


    @Override
    public void onBroadcastNotification(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NavigationActivity.this)
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

                //SI l'user n'a pas selectionné l'arrivee, dans ce cas on demande le chemin a partir de l'inscrution actuellement affichee
                if (currentInstruction.nodeFrom != null) {
                    Log.d(TAG, "source: " + currentInstruction.nodeFrom + " dest " + PathSingleton.getInstance().getPath().getDestination());
                    JSONObject o = new JSONObject();
                    try {
                        o.put("source", currentInstruction.nodeFrom);
                        o.put("destination", PathSingleton.getInstance().getPath().getDestination());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    PathSingleton.getInstance().askForPath(o);
                }
            }
        });
    }

    @Override
    public void onPOIListFetched() {
    }

    @Override
    public void onInvalidPath() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(NavigationActivity.this);
                builder.setMessage("Aucun chemin n'a pu etre trouvé  !")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
        Intent mainIntent = new Intent(NavigationActivity.this, SelectDepartureActivity.class);
        startActivity(mainIntent);
        finish();

    }

    /**
     * Fired when the service needs to fire a notification
     *
     * @param action Defining the type of notification
     */
    @Override
    public void onSendNotificationRequest(String action) {
        //Log.d(TAG, "onSendNotificationRequest : " + action);
        sendNotificationOnPhone(action);
        syncDataWithWatch();
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
        ArrayList<Node> listNavigation = PathSingleton.getInstance().getPath().getListNodes();
        Log.d(TAG, "PREPARENAVIGATIONLIST");
        Log.d(TAG, listNavigation.toString());

        for (int i = 0; i < listNavigation.size(); i++) {
            ArrayList<Discount> listDiscounts = new ArrayList<>();

            Node n = listNavigation.get(i);
            String text = "";

            //si il y a des instructions
            if (!"".equals(n.getInstruction().getInstruction())) {
                text += n.getInstruction().getInstruction() + "\n";
            }
            if (i != 0) {

                //si il y a des POI
                if (n.getPoi() != null && !n.getPoi().isEmpty()) {
                    for (POI s : n.getPoi()) {
                        listDiscounts.add(new Discount(s.getName(), s.getDiscount(), s.getImageB64()));
                    }
                }
            } else {
                //CAS DU NOEUD DE DEPART
                Node nplusun = listNavigation.get(i + 1);
                //si il y a des POI
                if (nplusun.getPoi() != null && !nplusun.getPoi().isEmpty()) {
                    for (POI s : nplusun.getPoi()) {
                        listDiscounts.add(new Discount(s.getName(), s.getDiscount(), s.getImageB64()));
                    }
                }
            }

            //SI on est pas arrivé a la fin du tableau, on rentre le noeud/beacon ou on va arriver
            if (i < listNavigation.size() - 1) {
                mInstructionData.add(new Instruction(n.getName(), text, listDiscounts, PathSingleton.getInstance().getPath().getListNodes().get(i).getInstruction().getOrientation()));
            } else {
                //sinon juste le noeud/beacon de ic_depart
                mInstructionData.add(new Instruction(n.getName(), text, listDiscounts, PathSingleton.getInstance().getPath().getListNodes().get(i).getInstruction().getOrientation()));
            }
        }
    }

    //==============================================================================================
    // Utils
    //==============================================================================================

    /**
     * Send a broadcast to the NotificationBroadcast class
     *
     * @param action Defining the type of notification
     */
    private void sendNotificationOnPhone(String action) {
        Intent in = new Intent();
        in.setAction(action);

        int instructionIndex = PathSingleton.getInstance().getPath().getIndexOfInstruction();
        String orientation = PathSingleton.getInstance().getPath().getListInstructions().get(instructionIndex).getOrientation();
        in.putExtra("instruction", PathSingleton.getInstance().getPath().getListInstructions().get(instructionIndex).getInstruction());
        in.putExtra("icon", PathSingleton.determineOrientationIcon(orientation));

        sendBroadcast(in);
    }


    private void syncDataWithWatch() {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/instructions");

        ArrayList<String> instructions = PathSingleton.getInstance().getPath().listInstructionsToStringArray();
        ArrayList<String> listOrientation = PathSingleton.getInstance().getPath().getListOrientationInstructions();
        Log.d(TAG, "list all orientations : " + PathSingleton.getInstance().getPath().getListOrientationInstructions());
        String timestamp = Long.toString(System.currentTimeMillis()); // To force the data to be updated
        int indexOfInstruction = PathSingleton.getInstance().getPath().getIndexOfInstruction();

        putDataMapReq.getDataMap().putStringArrayList("instructions", instructions);
        putDataMapReq.getDataMap().putStringArrayList("listOrientation", listOrientation);
        putDataMapReq.getDataMap().putString("timestamp", timestamp);
        putDataMapReq.getDataMap().putInt("indexOfInstruction", indexOfInstruction);
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
    public void onStop() {
        super.onStop();
        if (isServiceBounded)
            unbindService(this);
    }

    @Override
    public void onFragmentInteraction(int currentDataPosition) {
    }

    @Override
    public void onFragmentCreated(NavigationFragment navigationFragment) {

    }

    @Override
    public void onFragmentResumed(NavigationFragment navigationFragment) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "PAGE SELECTED:" + position);
        currentInstruction = mInstructionData.get(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class NavigationFragmentAdapter extends FragmentStatePagerAdapter {


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

        //this is called when notifyDataSetChanged() is called
        @Override
        public int getItemPosition(Object object) {
            //Log.d(TAG, "GETITEMPOSITION APPELAY");
            // refresh all fragments when data set changed
            return PagerAdapter.POSITION_NONE;
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
