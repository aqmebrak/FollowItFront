package polytech.followit.service;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import polytech.followit.utility.PathSingleton;


public class MessageHandler extends Handler {

    private final String TAG = MessageHandler.class.getName();
    static final int MSG_ASK_NEW_PATH = 1;
    static final int MSG_NEXT_INSTRUCTION = 2;

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_ASK_NEW_PATH:
                Log.d(TAG,"DANS LE MSG_ASK_NEW_PATH !");
                askNewPath(msg);
                break;
            case MSG_NEXT_INSTRUCTION:
                //// TODO: 01/02/2017 Changer le fragment d'affichage des instructions + notifier la montre
                Log.d(TAG,"IN MSG_NEXT_INSTRUCTION");
                break;
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


