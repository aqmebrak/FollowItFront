package polytech.followit.rest;

import android.content.Context;
import android.util.Log;

import polytech.followit.Node;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import polytech.followit.POI;

public class Path {

    // This is the reference to the associated listener
    public SocketCallBack socketCallBack;

    public final String TAG = Path.class.getSimpleName();
    public Socket socket;
    //Liste navigation
    public ArrayList<Node> result;
    //List POI avec leur node associé
    public ArrayList<POI> POIList;
    public String source;
    public String destination;

    public Path(final SocketCallBack socketCallBack) {
        this.socketCallBack = socketCallBack;
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
                Log.d(TAG, "PATH SOCKET CALLBACK");

                buildPathWithNodes(args);
            }

        }).on("POIList", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                POIList = new ArrayList<POI>();
                Log.d(TAG, "call: POI LIST" + response.toString());
                try {
                    JSONArray a = (JSONArray) response.get("poi");
                    for (int i = 0; i < a.length(); i++) {
                        JSONObject el = (JSONObject) a.get(i);
                        POIList.add(new POI((String) el.get("poi"), (String) el.get("node"), false));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socketCallBack.POIListNotification(POIList);
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

    public ArrayList<POI> getPOIList() {
        return POIList;
    }

    public void askForPath(JSONObject param) {
        Log.d(TAG, "ASKING PATH SOCKET");
        socket.emit("askPath", param);
    }

    public void askPOIList() {
        socket.emit("getPOI");
    }

    private void buildPathWithNodes(Object... args) {
        String node_name, node_instruction;
        ArrayList<String> node_poi;
        result = new ArrayList<>();

        try {
            JSONObject response = (JSONObject) args[0];
            JSONArray path = response.getJSONArray("map");

            // create Node objects
            for (int i = 0; i < path.length(); i++) {
                node_name = path.getJSONObject(i).getString("node");

                // create Node's node_poi
                node_poi = new ArrayList<>();
                for (int j = 0; j < path.getJSONObject(i).getJSONArray("POIList").length(); j++)
                    node_poi.add((String) path.getJSONObject(i).getJSONArray("POIList").get(j));
                node_instruction = path.getJSONObject(i).getString("instruction");
                result.add(new Node(node_name, node_poi, node_instruction));
            }

            Log.d(TAG, "result : " + result);
            socketCallBack.onPathFetched(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
