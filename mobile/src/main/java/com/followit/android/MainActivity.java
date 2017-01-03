package com.followit.android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.R;
import com.followit.android.rest.Path;
import com.followit.android.rest.SocketCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Path path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        path = new Path(MainActivity.this, new SocketCallBack() {
            @Override
            public void onPushNotification(ArrayList<String> nodes) {
                if (nodes != null)
                    Toast.makeText(MainActivity.this, nodes.toString(), Toast.LENGTH_SHORT).show();
            }
        });

        Button getPathButton = (Button) findViewById(R.id.getPathButton);

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
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
