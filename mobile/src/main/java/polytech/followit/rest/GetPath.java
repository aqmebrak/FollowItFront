package polytech.followit.rest;

import android.util.Log;

import polytech.followit.model.Beacon;
import polytech.followit.model.Instruction;
import polytech.followit.model.Node;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import polytech.followit.model.POI;
import polytech.followit.model.Path;

public class GetPath {

    // This is the reference to the associated listener
    public SocketCallBack socketCallBack;

    public final String TAG = GetPath.class.getSimpleName();
    public Socket socket;
    //Liste navigation
    public ArrayList<Node> result;
    //List POI avec leur node associé

    public GetPath(final SocketCallBack socketCallBack) {
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
                buildPathWithNodes(args);
            }

        }).on("POIList", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];

                ArrayList<POI> POIList;

                POIList = new ArrayList<>();
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
                socketCallBack.onPOIListFetched(POIList);
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

    public void askForPath(JSONObject param) {
        Log.d(TAG, "ASKING PATH SOCKET");
        socket.emit("askPath", param);
    }

    public void askPOIList() {
        socket.emit("getPOI");
    }

    private void buildPathWithNodes(Object... args) {
        String node_name;
        ArrayList<POI> node_poi;
        Instruction node_instruction;
        double node_xCoord, node_yCoord;
        Beacon node_beacon = null;

        Path path = new Path();

        try {
            JSONObject response = (JSONObject) args[0];
            JSONArray arrayPath = response.getJSONArray("map");
            JSONObject currentNode;

            // Create nodes
            for (int i = 0; i < arrayPath.length(); i++) {
                currentNode = arrayPath.getJSONObject(i);
                node_poi = new ArrayList<>();

                // name
                node_name = currentNode.getString("currentNode");

                // poi
                for (int j = 0; j < currentNode.getJSONArray("POIList").length(); j++) {
                    String poi_name = (String) currentNode.getJSONArray("POIList").get(j);
                    POI poi = new POI(poi_name, node_name, false);
                    node_poi.add(poi);
                }

                // instruction
                node_instruction = new Instruction(null, null, currentNode.getString("instruction"));

                // coordinate
                node_xCoord = currentNode.getJSONObject("coord").getDouble("x");
                node_yCoord = currentNode.getJSONObject("coord").getDouble("y");

                // beacon
                if (currentNode.has("beacon")) {
                    node_beacon = new Beacon(
                            currentNode.getJSONObject("beacon").getString("name"),
                            currentNode.getJSONObject("beacon").getString("UUID"),
                            currentNode.getJSONObject("beacon").getInt("major"),
                            currentNode.getJSONObject("beacon").getInt("minor")
                    );
                }

                // Node final object
                Node node = new Node(
                        node_name,
                        node_poi,
                        node_instruction,
                        node_xCoord,
                        node_yCoord,
                        node_beacon
                );

                path.getListNodes().add(node);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
