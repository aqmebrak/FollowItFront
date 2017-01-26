package polytech.followit.rest;

import polytech.followit.model.Node;
import polytech.followit.model.POI;

import org.json.JSONException;

import java.util.ArrayList;

public interface SocketCallBack {
    void onPathFetched() throws JSONException;
    void onBroadcastNotification(String message);
    void onPOIListFetched();
    void onBeaconsFetched();
    void onNodesFetched();
}
