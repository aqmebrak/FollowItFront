package polytech.followit.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class WearableCommunication extends AsyncTask<Void,Void,Void> {

    private String path;
    private String message;
    private GoogleApiClient googleClient;

    // Constructor to send a message to the data layer
    public WearableCommunication(String path, String message, GoogleApiClient googleClient) {
        this.path = path;
        this.message = message;
        this.googleClient = googleClient;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleClient).await();
        for (Node node : nodes.getNodes()) {
            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, message.getBytes()).await();
            if (result.getStatus().isSuccess()) {
                Log.v("myTag", "Message: {" + message + "} sent to: " + node.getDisplayName());
            } else {
                // Log an error
                Log.v("myTag", "ERROR: failed to send Message");
            }
        }
        return null;
    }
}