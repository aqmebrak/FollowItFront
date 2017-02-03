package polytech.followit;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;


public class ApplicationListener extends Application implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = ApplicationListener.class.getName();
    public static ArrayList<String> instructions;
    public static int indexOfInstruction;
    public static String orientation;
    private GoogleApiClient apiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        apiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected: " + bundle);
        Wearable.DataApi.addListener(apiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: " + i);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG,"ON DATA CHANGED");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                Log.d(TAG,"data item : "+event.getDataItem());
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().equals("/instructions")) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    instructions = dataMap.getStringArrayList("instructions");
                    indexOfInstruction = dataMap.getInt("indexOfInstruction");
                    orientation = dataMap.getString("orientation");
                    sendNotification();
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    private void sendNotification() {

        Intent viewIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, viewIntent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(getIconByOrientation())
                .setContentTitle("Instruction")
                .setContentText(instructions.get(indexOfInstruction))
                .setContentIntent(viewPendingIntent)
                .setPriority(1000)
                .setVibrate(new long[]{1000, 300});

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, mBuilder.build());
    }

    private int getIconByOrientation() {
        switch (orientation) {
            case "NORTH": return R.drawable.ic_north;
            case "NORTH_EAST": return R.drawable.ic_north_east;
            case "NORTH_WEST": return R.drawable.ic_north_west;
            case "EAST": return R.drawable.ic_east;
            case "WEST": return R.drawable.ic_west;
            case "SOUTH_EAST": return R.drawable.ic_south_east;
            case "SOUTH_WEST": return R.drawable.ic_south_west;
            default: return -1;
        }
    }
}

