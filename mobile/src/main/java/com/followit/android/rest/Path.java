package com.followit.android.rest;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import org.json.*;
import com.loopj.android.http.*;
import java.util.ArrayList;
import cz.msebera.android.httpclient.Header;

/**
 * Created by mperrin on 23/12/2016.
 */

public class Path extends AsyncTask<Void, Void, String> {

    public interface RestCallback {
        void onFinished(String result);
    }

    // This is the reference to the associated listener
    private final RestCallback restCallback;

    private static final String TAG = Path.class.getSimpleName();
    private ArrayList<String> nodes;
    private String source;
    private String destination;
    private Context context;

    public Path(Context c,RestCallback restCallback)
    {
        this.restCallback = restCallback;
        context = c;
    }

    public ArrayList<String> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<String> nodes) {
        this.nodes = nodes;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    protected String doInBackground(Void... params) {
        return getPath((RequestParams)params);
    }

    public void getPath(RequestParams params) throws JSONException {
        RestClient.post("path", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // If the response is JSONObject instead of expected JSONArray
                Log.d(TAG, "Object: " + response.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // Pull out the first event on the public timeline
                nodes = new ArrayList<String>();
                Log.d(TAG, "length" + response.length());
                for (int i = 0; i < response.length(); i++) {
                    try {
                        nodes.add(response.get(i).toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.d(TAG, "Array: " + response.toString());
                if (nodes != null)
                    Toast.makeText(context, nodes.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d(TAG, "ERROR: " + errorResponse.toString());
            }

        });
    }
}
