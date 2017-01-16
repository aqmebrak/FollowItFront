package com.followit.android.rest;

import java.util.ArrayList;

public interface SocketCallBack {
    void onPathFetched(ArrayList<String> nodes);
    void onBroadcastNotification(String message);
}
