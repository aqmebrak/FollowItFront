package polytech.followit;


import android.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GridPagerRow {

    private final List<Fragment> columns = new ArrayList<>();

    public GridPagerRow(Fragment... fragments) {
        Collections.addAll(columns, fragments);
    }

    Fragment getColumn(int i) {
        return columns.get(i);
    }

    public int getColumnCount() {
        return columns.size();
    }

}
