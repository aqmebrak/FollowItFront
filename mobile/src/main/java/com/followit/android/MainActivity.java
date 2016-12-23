package com.followit.android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.android.R;
import com.followit.android.rest.Path;
import com.followit.android.rest.RestClient;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;

public class MainActivity extends AppCompatActivity {

    private Button getPathButton;
    private Path path = new Path(MainActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getPathButton = (Button) findViewById(R.id.getPathButton);

        getPathButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                RequestParams params = new RequestParams();
                params.put("source", "a");
                params.put("destination", "f");
                try {
                    path.getPath(params);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
