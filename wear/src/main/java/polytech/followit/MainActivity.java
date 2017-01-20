package polytech.followit;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final GridViewPager pager = (GridViewPager) findViewById(R.id.grid_view_pager);
        pager.setAdapter(new GridPagerAdapter(this,getFragmentManager()));
    }

}
