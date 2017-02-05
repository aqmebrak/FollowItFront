package polytech.followit.service;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import polytech.followit.NavigationActivity;
import polytech.followit.utility.PathSingleton;


class MessageHandler extends Handler {

    private final String TAG = MessageHandler.class.getName();
    static final int MSG_ASK_NEW_PATH = 1;
    static final int MSG_NEXT_INSTRUCTION = 2;
    static final int MSG_ARRIVED_TO_DESTINATION = 3;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_ASK_NEW_PATH:
                Log.d(TAG,"DANS LE MSG_ASK_NEW_PATH !");
                askNewPath(msg);
                break;
            case MSG_NEXT_INSTRUCTION:
                Log.d(TAG,"IN MSG_NEXT_INSTRUCTION");
                PathSingleton.getInstance().getPath().incrementIndexOfInstruction();
                PathSingleton.getInstance().getSocketCallBack().onSendNotificationRequest("NEXT_INSTRUCTION");
                break;
            case MSG_ARRIVED_TO_DESTINATION:
                Log.d(TAG,"IN MSG_ARRIVED_TO_DESTINATION");
                PathSingleton.getInstance().getPath().incrementIndexOfInstruction();
                PathSingleton.getInstance().getSocketCallBack().onSendNotificationRequest("ARRIVED_TO_DESTINATION");
                PathSingleton.getInstance().getSocketCallBack().onArrival();
            default:
                super.handleMessage(msg);
        }
    }

    private void askNewPath(Message message) {
        Bundle msg_data = message.getData();
        JSONObject itinerary = new JSONObject();
        try {
            itinerary.put("source", msg_data.get("source"));
            itinerary.put("destination", msg_data.get("destination"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PathSingleton.getInstance().askForPath(itinerary);
    }

}


