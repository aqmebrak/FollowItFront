package polytech.followit;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
                if (item.getUri().getPath().compareTo("/startActivity") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    instructions = dataMap.getStringArrayList("instructions");

                    /*if (instructions != null) {
                        text.setVisibility(View.INVISIBLE);
                        GridViewPager pager = (GridViewPager) findViewById(R.id.grid_view_pager);
                        pager.setAdapter(new GridPagerAdapter(this,getFragmentManager()));
                    }*/
                    //Intent main = new Intent(this, MainActivity.class);
                    //main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //startActivity(main);

                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }
}

