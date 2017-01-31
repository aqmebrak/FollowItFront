package polytech.followit;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "ON CREATE");

        GridViewPager pager = (GridViewPager) findViewById(R.id.grid_view_pager);
        pager.setAdapter(new GridPagerAdapter(this, getFragmentManager()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG,"ON RESUME");
    }
}
