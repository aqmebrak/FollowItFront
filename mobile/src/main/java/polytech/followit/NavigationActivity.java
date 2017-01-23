package polytech.followit;

import android.app.NotificationManager;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import polytech.followit.model.Instruction;
import polytech.followit.model.Node;
import polytech.followit.model.POI;
import polytech.followit.rest.GetPath;
import polytech.followit.rest.SocketCallBack;

public class NavigationActivity extends AppCompatActivity implements
        SensorEventListener,
        View.OnClickListener,
        SocketCallBack {

    private static final String TAG = NavigationActivity.class.getSimpleName();

    /**
     * variables for sensor
     **/
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorMagneticField;

    private float[] valuesAccelerometer;
    private float[] valuesMagneticField;
    private float[] matrixR;
    private float[] matrixI;
    private float[] matrixValues;

    private ImageView navigation;
    private final int NUMBER_OF_MEASUREMENT = 5;
    private final double MIN_DIFF_FOR_EVENT = 5;
    private ArrayList<Double> valuesAzimuth;
    private double lastAzimuth;

    private Location location;

    //recupere la liste en RAW de la navigation
    ArrayList<Node> listNavigation = null;

    //liste des etapes en Instruction
    ArrayList<Instruction> navigationSteps = null;

    //index position sur quelle étape on se trouve
    int index = 0;
    //Instruction en cours
    Instruction ongoingInstruction;

    GetPath p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_activity);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        valuesAccelerometer = new float[3];
        valuesMagneticField = new float[3];
        matrixR = new float[9];
        matrixI = new float[9];
        matrixValues = new float[3];

        valuesAzimuth = new ArrayList<>();
        lastAzimuth = 360;

        navigation = (ImageView) findViewById(R.id.navigation);
        navigation.setImageResource(R.drawable.ic_navigation_black_24dp);

        p = new GetPath(this);

        //RECUPERE LES DONNES
        Bundle bundle = getIntent().getExtras();
        p.source = bundle.getString("source");
        p.destination = bundle.getString("destination");
        listNavigation = (ArrayList<Node>) getIntent().getSerializableExtra("nodeList");
        location = (Location) bundle.getParcelable("location");

        Log.d("NAVIGATION:", listNavigation.toString());

        prepareNavigationList();

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
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_UI);

        super.onResume();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this, sensorAccelerometer);
        sensorManager.unregisterListener(this, sensorMagneticField);
        super.onPause();
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
    public void onPOIListFetched(ArrayList<POI> list) {
        //NOT USED
    }

    //==============================================================================================
    // Sensors implementation
    //==============================================================================================

    @Override
    public void onSensorChanged(SensorEvent event) {

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, valuesAccelerometer, 0, 3);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, valuesMagneticField, 0, 3);
                break;
        }

        boolean success = SensorManager.getRotationMatrix(matrixR, matrixI, valuesAccelerometer, valuesMagneticField);

        if (success) {
            SensorManager.getOrientation(matrixR, matrixValues);
            double azimuth = matrixValues[0];

            if (Math.abs(lastAzimuth - Math.toDegrees(azimuth)) > MIN_DIFF_FOR_EVENT) {

                if (valuesAzimuth.size() == NUMBER_OF_MEASUREMENT) {
                    double sumSin = 0;
                    double sumCos = 0;
                    for (double value : valuesAzimuth) {
                        sumSin += Math.sin(value);
                        sumCos += Math.cos(value);
                    }
                    azimuth = Math.toDegrees(Math.atan2(sumSin, sumCos)) +
                            Math.toRadians(getGeomagneticField(location).getDeclination());
                    valuesAzimuth.clear();
                    navigation.setRotation((float) (-azimuth));


                    lastAzimuth = azimuth;

                    /*PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/startActivity");
                    putDataMapReq.getDataMap().putStringArrayList("instructions", azimuth);
                    PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(googleApiClient, putDataReq);*/
                } else valuesAzimuth.add(azimuth);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //==============================================================================================
    // Functions utils
    //==============================================================================================

    private GeomagneticField getGeomagneticField(Location location) {
        return new GeomagneticField(
                (float) location.getLatitude(),
                (float) location.getLongitude(),
                (float) location.getAltitude(),
                System.currentTimeMillis());
    }
}
