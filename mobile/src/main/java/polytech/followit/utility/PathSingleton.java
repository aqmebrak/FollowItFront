package polytech.followit.utility;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import polytech.followit.R;
import polytech.followit.model.Beacon;
import polytech.followit.model.Instruction;
import polytech.followit.model.Node;
import polytech.followit.model.POI;
import polytech.followit.model.Path;
import polytech.followit.rest.SocketCallBack;

public class PathSingleton {

    private final String TAG = PathSingleton.class.getSimpleName();
    private static PathSingleton ourInstance = new PathSingleton();

    private Path path;
    private ArrayList<POI> listAllPoi;

    private SocketCallBack socketCallBack;
    private Socket socket;

    private PathSingleton() {
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
                buildPOIList(args);
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
                getInstance().socketCallBack.onBroadcastNotification(response.toString());
            }

        });
        socket.connect();
    }

    //==============================================================================================
    // getter and setter
    //==============================================================================================

    public static PathSingleton getInstance() {
        return ourInstance;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public SocketCallBack getSocketCallBack() {
        return socketCallBack;
    }

    public void setSocketCallBack(final SocketCallBack socketCallBack) {
        this.socketCallBack = socketCallBack;
    }

    public ArrayList<POI> getListAllPoi() {
        return listAllPoi;
    }

    //==============================================================================================
    // socket functions
    //==============================================================================================

    public void askForPath(JSONObject param) {
        Log.d(getInstance().TAG, "ASKING PATH SOCKET");
        getInstance().socket.emit("askPath", param);
    }

    public void askPOIList() {
        getInstance().socket.emit("getPOI");
    }

    private void buildPathWithNodes(Object... args) {
        Log.d(getInstance().TAG, "BUILDPATHWITHNODES");

        //Path attributes
        ArrayList<Node> listNodes = new ArrayList<>();
        ArrayList<Instruction> listInstructions = new ArrayList<>();
        ArrayList<String> listOrientationInstructions = new ArrayList<>();
        ArrayList<Beacon> listBeacons = new ArrayList<>();
        String source = null, destination = null;

        // Node attributes
        String node_name;
        ArrayList<POI> node_poi;
        Instruction node_instruction;
        Beacon node_beacon;

        try {
            JSONObject response = (JSONObject) args[0];
            Log.d(getInstance().TAG, "BUILD PATH WITH NODES RESPONSE : " + response);
            JSONArray arrayPath = response.getJSONArray("map");
            JSONObject currentNode;

            // Create nodes
            for (int i = 0; i < arrayPath.length(); i++) {
                currentNode = arrayPath.getJSONObject(i);

                // name
                node_name = currentNode.getString("node");
                if (i == 0) source = node_name;
                if (i == arrayPath.length() - 1) destination = node_name;

                // poi
                node_poi = new ArrayList<>();
                for (int j = 0; j < currentNode.getJSONArray("POIList").length(); j++) {
                    JSONObject currentPOI = (JSONObject) currentNode.getJSONArray("POIList").get(j);
                    String poi_name = currentPOI.getString("poi");
                    String poi_discount = currentPOI.has("discount") ? (String) currentPOI.get("discount") : "Pas de promotion pour ce magasin";
                    String poi_imageB64 = currentPOI.has("image") ? (String) currentPOI.get("image") : null;
                    POI poi = new POI(poi_name, node_name, poi_discount, poi_imageB64, false);
                    node_poi.add(poi);
                }

                // instruction
                String orientation = null;
                if (i == 0) orientation = "DEPARTURE";
                else if (i == arrayPath.length() - 1) orientation = "ARRIVAL";
                else if (currentNode.has("orientation"))
                    orientation = currentNode.getString("orientation");
                node_instruction = new Instruction(null, null, currentNode.getString("instruction"), null, orientation);
                listInstructions.add(node_instruction);
                listOrientationInstructions.add(orientation);

                // beacon
                if (currentNode.has("beacon")) {
                    node_beacon = new Beacon(
                            currentNode.getJSONObject("beacon").getString("name"),
                            currentNode.getJSONObject("beacon").getString("UUID"),
                            currentNode.getJSONObject("beacon").getInt("major"),
                            currentNode.getJSONObject("beacon").getInt("minor")
                    );
                    listBeacons.add(node_beacon);
                } else node_beacon = null;

                // Node final object
                Node node = new Node(
                        node_name,
                        node_poi,
                        node_instruction,
                        node_beacon
                );

                listNodes.add(node);
            }
            path = new Path(listNodes, listInstructions, listBeacons, listOrientationInstructions, source, destination);
            getInstance().getSocketCallBack().onPathFetched();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void buildPOIList(Object... args) {
        JSONObject response = (JSONObject) args[0];

        listAllPoi = new ArrayList<>();
        Log.d(TAG, "call: POI LIST" + response.toString());
        try {
            JSONArray arrayPOI = (JSONArray) response.get("poi");
            for (int i = 0; i < arrayPOI.length(); i++) {
                JSONObject poi = (JSONObject) arrayPOI.get(i);
                listAllPoi.add(new POI((String) poi.get("poi"), (String) poi.get("node"), poi.has("image") ? (String) poi.get("image") : null, false));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getInstance().socketCallBack.onPOIListFetched();
    }

    public static int determineOrientationIcon(String orientation) {
        switch (orientation) {
            case "NORTH":
                return R.drawable.ic_north;
            case "NORTH_EAST":
                return R.drawable.ic_north_east;
            case "NORTH_WEST":
                return R.drawable.ic_north_west;
            case "EAST":
                return R.drawable.ic_east;
            case "WEST":
                return R.drawable.ic_west;
            case "SOUTH_EAST":
                return R.drawable.ic_south_east;
            case "SOUTH_WEST":
                return R.drawable.ic_south_west;
            case "DEPARTURE":
                return R.drawable.ic_departure;
            case "ARRIVAL":
                return R.drawable.ic_arrival;
            default:
                return R.drawable.ic_default;
        }
    }
}
