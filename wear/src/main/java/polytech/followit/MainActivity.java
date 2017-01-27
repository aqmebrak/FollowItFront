package polytech.followit;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends Activity {

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "ON CREATE");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG,"ON RESUME");
        GridViewPager pager = (GridViewPager) findViewById(R.id.grid_view_pager);
        pager.setAdapter(new GridPagerAdapter(this, getFragmentManager()));
    }
}
