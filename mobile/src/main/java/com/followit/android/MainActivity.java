package com.followit.android;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.R;
import com.followit.android.rest.Path;
import com.followit.android.rest.SocketCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SocketCallBack {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Path path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        path = new Path(MainActivity.this, this);

        final Button getPathButton = (Button) findViewById(R.id.getPathButton);

        getPathButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click

                JSONObject params = new JSONObject();
                try {
                    params.put("source", "a");
                    params.put("destination", "f");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                path.askForPath(params);

                //display progressbar
                getPathButton.setVisibility(View.GONE);
                ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
                pb.setVisibility(View.VISIBLE);
            }
        });
        //POURQUOI ? SINON CA AFFICHE QUAND MEME....
        ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
        pb.setVisibility(View.GONE);
    }

    @Override
    public void onPushNotification(final ArrayList<String> nodes) {
        Log.d(TAG, "NOTIF: JSONOBJECT" + nodes.toString());

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView t = (TextView) findViewById(R.id.result_tv);
                t.setText(nodes.toString());
                t.setFontFeatureSettings();
                t.setVisibility(View.VISIBLE);

                //display button again
                Button getPathButton = (Button) findViewById(R.id.getPathButton);
                getPathButton.setVisibility(View.VISIBLE);
                ProgressBar pb = (ProgressBar) findViewById(R.id.pb);
                pb.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onBroadcastNotification(final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                NotificationCompat.Builder mBuilder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(MainActivity.this)
                                .setSmallIcon(R.drawable.ic_stat_name)
                                .setContentTitle("Map Update")
                                .setContentText("Map has been updated, synchronize your navigation steps");
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                // notificationID allows you to update the notification later on.
                mNotificationManager.notify(1, mBuilder.build());
                //TODO: ADD action on notification click

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
