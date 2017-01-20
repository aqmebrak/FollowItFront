package polytech.followit.rest;

import android.content.Context;
import android.util.Log;

import polytech.followit.Node;

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
                buildPathWithNodes(args);
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


    private void buildPathWithNodes(Object... args) {
        String node_name, node_instruction;
        ArrayList<String> node_poi;
        ArrayList<Node> result = new ArrayList<>();

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

            Log.d(TAG,"result : "+result);
            socketCallBack.onPathFetched(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}