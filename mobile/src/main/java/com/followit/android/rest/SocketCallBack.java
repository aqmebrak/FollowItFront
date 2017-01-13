package com.followit.android.rest;

import java.util.ArrayList;

/**
 * Created by mperrin on 03/01/2017.
 */

public interface SocketCallBack {
    void onPushNotification( ArrayList<String> nodes);
    void onBroadcastNotification(String message);
}
