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
    private ArrayList<Beacon> listAllBeacons;
    private ArrayList<Node> listAllNodes;

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

        }).on("beaconArray", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];
                listAllBeacons = new ArrayList<>();
                Log.d(TAG,"RESPONSE GETBEACONARRAY : "+response);
                try {
                    JSONArray beaconsArray = response.getJSONArray("beaconArray");
                    for (int i = 0; i < beaconsArray.length(); i++) {
                        JSONObject beacon = (JSONObject) beaconsArray.get(i);
                        String name = beacon.getString("name");
                        String UUID = beacon.getString("UUID");
                        int major = beacon.getInt("major");
                        int minor = beacon.getInt("minor");
                        listAllBeacons.add(new Beacon(name, UUID, major, minor));
                    }
                    Log.d(TAG,"LIST ALL BEACONS : "+listAllBeacons.toString());
                    getInstance().getSocketCallBack().onBeaconsFetched();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).on("allNodes", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject response = (JSONObject) args[0];
                listAllNodes = new ArrayList<>();
                Log.d(TAG,"RESPONSE GET ALL NODES" + response);
                try {
                    JSONArray nodesArray = response.getJSONArray("nodes");
                    for (int i = 0; i < nodesArray.length(); i++) {
                        JSONObject node = nodesArray.getJSONObject(i);
                        JSONObject node_value = node.getJSONObject("value");

                        String name = node.getString("v");

                        JSONArray poi = node_value.getJSONArray("POI");
                        ArrayList<POI> listPoi = new ArrayList<>();
                        for (int j = 0; j < poi.length(); j++) {
                            POI node_poi = new POI(poi.getString(j),null,false);
                            listPoi.add(node_poi);
                        }

                        JSONObject node_value_coord = (JSONObject) node_value.get("coord");
                        double xCoord = node_value_coord.getDouble("x");
                        double yCoord = node_value_coord.getDouble("y");

                        Beacon beacon = null;
                        if (node_value.has("beacon")) {
                            JSONObject node_value_beacon = node_value.getJSONObject("beacon");
                            beacon = new Beacon(
                                    node_value_beacon.getString("name"),
                                    node_value_beacon.getString("UUID"),
                                    node_value_beacon.getInt("major"),
                                    node_value_beacon.getInt("minor")
                            );
                        }

                        Node newNode = new Node(name, listPoi, null, xCoord, yCoord, beacon);
                        listAllNodes.add(newNode);
                        getInstance().socketCallBack.onNodesFetched();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    public double getAngleDeviationToNextBeacon() {
        return angleDeviationToNextBeacon;
    }

    public void setAngleDeviationToNextBeacon(double angleDeviationToNextBeacon) {
        this.angleDeviationToNextBeacon = angleDeviationToNextBeacon;
    }

    public ArrayList<Beacon> getListAllBeacons() {
        return listAllBeacons;
    }

    public void setListAllBeacons(ArrayList<Beacon> listAllBeacons) {
        this.listAllBeacons = listAllBeacons;
    }

    public ArrayList<Node> getListAllNodes() {
        return listAllNodes;
    }

    public void setListAllNodes(ArrayList<Node> listAllNodes) {
        this.listAllNodes = listAllNodes;
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

    private static void buildPathWithNodes(Object... args) {
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
                Instruction instruction = new Instruction(null, null, currentNode.getString("instruction"));
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
}
