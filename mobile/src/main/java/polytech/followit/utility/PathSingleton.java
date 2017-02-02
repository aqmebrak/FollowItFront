package polytech.followit.utility;

import android.support.annotation.NonNull;
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

    private double angleDeviationToNextBeacon;

    private PathSingleton() {
        path = new Path();
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

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ArrayList<POI> getListAllPoi() {
        return listAllPoi;
    }

    public void setListAllPoi(ArrayList<POI> listAllPoi) {
        this.listAllPoi = listAllPoi;
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

        //on clean d'abord les ARRAY de Path
        getInstance().getPath().getListInstructions().clear();
        getInstance().getPath().getListNodes().clear();
        getInstance().getPath().getListNodes().clear();

        String node_name;
        ArrayList<POI> node_poi;
        Instruction node_instruction;
        double node_xCoord, node_yCoord;
        Beacon node_beacon = null;

        try {
            JSONObject response = (JSONObject) args[0];
            Log.d(getInstance().TAG,"BUILD PATH WITH NODES RESPONSE : "+response);
            JSONArray arrayPath = response.getJSONArray("map");
            JSONObject currentNode;

            // Create nodes
            for (int i = 0; i < arrayPath.length(); i++) {
                currentNode = arrayPath.getJSONObject(i);
                node_poi = new ArrayList<>();

                // name
                node_name = currentNode.getString("node");

                // poi
                for (int j = 0; j < currentNode.getJSONArray("POIList").length(); j++) {
                    String poi_name = (String) currentNode.getJSONArray("POIList").get(j);
                    POI poi = new POI(poi_name, node_name, false);
                    node_poi.add(poi);
                }

                // instruction
                int orientation_icon = currentNode.has("orientation") ? determineOrientationIcon(currentNode.getString("orientation")) : -1;
                Instruction instruction = new Instruction(null, null, currentNode.getString("instruction"),null, orientation_icon);
                node_instruction = instruction;
                getInstance().getPath().getListInstructions().add(instruction);

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
                    getInstance().getPath().getListBeacons().add(node_beacon);
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

                getInstance().getPath().getListNodes().add(node);
            }
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
                listAllPoi.add(new POI((String) poi.get("poi"), (String) poi.get("node"), false));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getInstance().socketCallBack.onPOIListFetched();
    }

    private int determineOrientationIcon(String orientation){
        switch (orientation) {
            case "NORTH": return R.drawable.ic_north;
            case "NORTH_EAST": return R.drawable.ic_north_east;
            case "NORTH_WEST": return R.drawable.ic_north_west;
            case "EAST": return R.drawable.ic_east;
            case "WEST": return R.drawable.ic_west;
            case "SOUTH_EAST": return R.drawable.ic_south_east;
            case "SOUTH_WEST": return R.drawable.ic_south_west;
        }
        return -1;
    }
}
