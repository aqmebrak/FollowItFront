package com.followit.android;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.R;
import com.followit.android.rest.Path;
import com.followit.android.rest.RestClient;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button getPathButton;
    private Path path = new Path(MainActivity.this);
    private Socket mSocket;

    {
        try {
            mSocket = IO.socket("https://followit-backend.herokuapp.com/");
        } catch (URISyntaxException e) {
            Log.d(TAG, "Couldn't socket" + e);
        }
    }

    private EditText mInputMessageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mSocket.connect();
        //mSocket.on("new message", onNewMessage);

        getPathButton = (Button) findViewById(R.id.getPathButton);

        getPathButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                TextView tv = (TextView) findViewById(R.id.pathResultTextField);

                RequestParams params = new RequestParams();
                params.put("source", "a");
                params.put("destination", "f");
                try {
                    path.getPath(params);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                while (!path.isResponded()) {
                }
                Log.d(TAG, "usResonded : " + path.isResponded());
                tv.setText("nooooodes");
                tv.setVisibility(View.VISIBLE);

                //attemptSend();
            }
        });
    }

    private void attemptSend() {
        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        mInputMessageView.setText("HELLO");
        mSocket.emit("new message", message);
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent();
            // intent.putExtra("username", mUsername);
            intent.putExtra("numUsers", numUsers);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("new message", onNewMessage);
    }
}
