package com.followit.android.rest;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


/**
 * Created by mperrin on 23/12/2016.
 */

public class Path {

    // This is the reference to the associated listener
    private SocketCallBack socketCallBack;

    private static final String TAG = Path.class.getSimpleName();
    private ArrayList<String> nodes;
    private String source;
    private String destination;
    private Context context;
    private Socket mSocket;


    public Path(Context c, final SocketCallBack socketCallBack) {
        this.socketCallBack = socketCallBack;
        context = c;
        {
            try {
                mSocket = IO.socket("https://followit-backend.herokuapp.com/");
            } catch (URISyntaxException e) {
                Log.d(TAG, "Couldn't socket" + e);
            }
        }

        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        }).on("path", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                ArrayList<String> nodes = new ArrayList<String>();
                Log.d(TAG, "call: JSONOBJECT" + response.toString());
                try {
                    JSONArray a = (JSONArray) response.get("map");
                    for (int i = 0; i < a.length(); i++) {
                        nodes.add((String) a.get(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socketCallBack.onPushNotification(nodes);
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        });
        mSocket.connect();
    }

    public ArrayList<String> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<String> nodes) {
        this.nodes = nodes;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void askForPath(JSONObject param) {
        mSocket.emit("askPath", param);
    }
}
