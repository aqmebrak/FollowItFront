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
        GridPagerAdapter view2D = new GridPagerAdapter(this, getFragmentManager());
        pager.setAdapter(view2D);
        view2D.getFragment(0,ApplicationListener.indexOfInstruction);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG,"ON RESUME");
    }
}
