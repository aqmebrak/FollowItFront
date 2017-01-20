package polytech.followit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import polytech.followit.Node;
import polytech.followit.R;

/**
 * Created by Akme on 19/01/2017.
 */

public class NavigationActivity extends AppCompatActivity {

    ArrayList<Node> listNavigation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigation_activity);

        listNavigation = (ArrayList<Node>) getIntent().getSerializableExtra("nodeList");
        Log.d("NAVIGATION:", listNavigation.toString());

        TextView t = (TextView) findViewById(R.id.instructions_textView);
        t.setText(listNavigation.toString());

    }
}
