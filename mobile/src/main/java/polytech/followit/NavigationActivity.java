package polytech.followit;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import polytech.followit.rest.Path;
import polytech.followit.rest.SocketCallBack;

/**
 * Created by Akme on 19/01/2017.
 */

public class NavigationActivity extends AppCompatActivity
        implements SensorEventListener,
        View.OnClickListener,
        SocketCallBack {

    private static final String TAG = NavigationActivity.class.getSimpleName();

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;

    //Pour DEBUG le compass
    TextView tvHeading;

    //recupere la liste en RAW de la navigation
    ArrayList<Node> listNavigation = null;

    //liste des etapes en Instruction
    ArrayList<Instruction> navigationSteps = null;

    //index position sur quelle étape on se trouve
    int index = 0;
    //Instruction en cours
    Instruction ongoingInstruction;

    Path p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_activity);

        p = new Path(this);

        //RECUPERE LES DONNES
        Bundle bundle = getIntent().getExtras();
        p.source = bundle.getString("source");
        p.destination = bundle.getString("destination");
        listNavigation = (ArrayList<Node>) getIntent().getSerializableExtra("nodeList");


        Log.d("NAVIGATION:", listNavigation.toString());

        prepareNavigationList();

        //on chage l'image du compass
        image = (ImageView) findViewById(R.id.imageViewCompass);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.tvHeading);

        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //button listeners
        Button b = (Button) findViewById(R.id.previous_button);
        b.setOnClickListener(this);
        b = (Button) findViewById(R.id.next_button);
        b.setOnClickListener(this);

    }

    private void prepareNavigationList() {
        //ON PREPARE LA LISTE DES ETAPES
        navigationSteps = new ArrayList<>();
        for (int i = 0; i < listNavigation.size(); i++) {

            Node n = listNavigation.get(i);
            String text = "Instructions\n";
            //si il y a des instructions
            if (n.getInstruction() != null && n.getInstruction() != "") {
                text += n.getInstruction() + "\n";
            }
            //si il y a des POI
            if (n.getPoi() != null && !n.getPoi().isEmpty()) {
                text += "Points d'interets à proximité: \n";
                for (String s : n.getPoi()) {
                    text += "\t- " + s;
                }
            }
            //SI on est pas arrivé a la fin du tableau, on rentre le noeud/beacon ou on va arriver
            if (i < listNavigation.size() - 1) {
                Node nplusun = listNavigation.get(i + 1);
                navigationSteps.add(new Instruction(n.getName(), nplusun.getName(), text));
            } else {
                //sinon juste le noeud/beacon de depart
                navigationSteps.add(new Instruction(n.getName(), text));
            }
        }
        //On affiche la 1ere etape
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView t = (TextView) findViewById(R.id.instructions_textView);
                t.setText(navigationSteps.get(index).instruction);
                ongoingInstruction = navigationSteps.get(index);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.previous_button:
                if (index > 0) {
                    index--;
                    TextView t = (TextView) findViewById(R.id.instructions_textView);
                    t.setText(navigationSteps.get(index).instruction);
                    ongoingInstruction = navigationSteps.get(index);
                }
                break;
            case R.id.next_button:
                if (index < navigationSteps.size() - 1) {
                    index++;
                    TextView t = (TextView) findViewById(R.id.instructions_textView);
                    t.setText(navigationSteps.get(index).instruction);
                    ongoingInstruction = navigationSteps.get(index);
                }
                break;
        }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }

    @Override
    public void onPathFetched(ArrayList<Node> path) throws JSONException {
        //NOT USED
        listNavigation.clear();
        listNavigation = path;
        prepareNavigationList();
    }

    @Override
    public void onBroadcastNotification(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NavigationActivity.this)
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentTitle("Map Update")
                        .setContentText("Map has been updated, synchronize your navigation steps");
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // notificationID allows you to update the notification later on.
                mNotificationManager.notify(1, mBuilder.build());

                //TODO: ADD action on notification click
                //ON redemande le chemin
                //on cherche l'étape active
                Log.d(TAG, "dEBUT DEMANDE DE CHEMIN");

                Log.d(TAG, "source: " + ongoingInstruction.nodeToGoTo + " dest " + p.destination);
                JSONObject o = new JSONObject();
                try {
                    o.put("source", ongoingInstruction.nodeToGoTo);
                    o.put("destination", p.destination);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                p.askForPath(o);
            }
        });
    }

    @Override
    public void POIListNotification(ArrayList<POI> list) {
        //NOT USED
    }
}
