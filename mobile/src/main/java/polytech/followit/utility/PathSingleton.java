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
import polytech.followit.rest.GetPath;
import polytech.followit.rest.SocketCallBack;

public class PathSingleton {

    private final String TAG = GetPath.class.getSimpleName();
    private static PathSingleton ourInstance = new PathSingleton();

    private Path path;
    private ArrayList<POI> listPOI;

    private SocketCallBack socketCallBack;
    private Socket socket;

    private PathSingleton() {
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

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ArrayList<POI> getListPOI() {
        return listPOI;
    }

    public void setListPOI(ArrayList<POI> listPOI) {
        this.listPOI = listPOI;
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
        String node_name;
        ArrayList<POI> node_poi;
        Instruction node_instruction;
        double node_xCoord, node_yCoord;
        Beacon node_beacon = null;

        ArrayList<Instruction> path_listInstructions;

        Path path = new Path();

        try {
            JSONObject response = (JSONObject) args[0];
            JSONArray arrayPath = response.getJSONArray("map");
            JSONObject currentNode;

            // Create nodes
            for (int i = 0; i < arrayPath.length(); i++) {
                currentNode = arrayPath.getJSONObject(i);
                node_poi = new ArrayList<>();
                path_listInstructions = new ArrayList<>();

                // name
                node_name = currentNode.getString("currentNode");

                // poi
                for (int j = 0; j < currentNode.getJSONArray("POIList").length(); j++) {
                    String poi_name = (String) currentNode.getJSONArray("POIList").get(j);
                    POI poi = new POI(poi_name, node_name, false);
                    node_poi.add(poi);
                }

                // instruction
                Instruction instruction = new Instruction(null, null, currentNode.getString("instruction"));
                node_instruction = instruction;
                path_listInstructions.add(instruction);

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

    private void buildPOIList(Object... args) {
        JSONObject response = (JSONObject) args[0];

        listPOI = new ArrayList<>();
        Log.d(TAG, "call: POI LIST" + response.toString());
        try {
            JSONArray arrayPOI = (JSONArray) response.get("poi");
            for (int i = 0; i < arrayPOI.length(); i++) {
                JSONObject poi = (JSONObject) arrayPOI.get(i);
                listPOI.add(new POI((String) poi.get("poi"), (String) poi.get("node"), false));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getInstance().socketCallBack.onPOIListFetched();
    }
}
