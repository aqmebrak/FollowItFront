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

public class Path {

    // This is the reference to the associated listener
    private SocketCallBack socketCallBack;

    private static final String TAG = Path.class.getSimpleName();
    private ArrayList<String> nodes;
    private String source;
    private String destination;
    private Context context;
    private Socket socket;


    public Path(Context c, final SocketCallBack socketCallBack) {
        this.socketCallBack = socketCallBack;
        context = c;
        try {
            socket = IO.socket("https://followit-backend.herokuapp.com/");
        } catch (URISyntaxException e) {
            Log.d(TAG, "Couldn't socket" + e);
        }

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }

        }).on("path", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                ArrayList<String> nodes = new ArrayList<String>();
                Log.d(TAG, "call: PATH" + response.toString());
                try {
                    JSONArray a = (JSONArray) response.get("map");
                    for (int i = 0; i < a.length(); i++) {
                        JSONObject node = (JSONObject) a.get(i);
                        nodes.add(node.get("node").toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socketCallBack.onPathFetched(nodes);
            }

        }).on("POIList", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                ArrayList<String> list = new ArrayList<String>();
                Log.d(TAG, "call: POI LIST" + response.toString());
                try {
                    JSONArray a = (JSONArray) response.get("poi");
                    for (int i = 0; i < a.length(); i++) {
                        list.add((String) a.get(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socketCallBack.POIListNotification(list);
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
            }

        }).on("notif", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d(TAG, "NOTIFICATION");
                JSONObject response = (JSONObject) args[0];
                socketCallBack.onBroadcastNotification(response.toString());
            }

        });
        socket.connect();
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
        socket.emit("askPath", param);
    }

    public void getPOIList() {
        socket.emit("getPOI");
    }
}
