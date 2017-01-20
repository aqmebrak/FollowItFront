package polytech.followit;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;

import java.util.ArrayList;

public class GridPagerAdapter extends FragmentGridPagerAdapter {

    private final Context context;

    private final ArrayList<GridPagerRow> rowList = new ArrayList<>();

    public GridPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.context = context;

        if (ApplicationListener.instructions != null) {
            GridPagerRow pagerRow = new GridPagerRow();
            for (int i=1; i<ApplicationListener.instructions.size() -1; i++) {
                pagerRow.addColumn(CardFragment.create("Instruction", ApplicationListener.instructions.get(i)));
            }
            rowList.add(pagerRow);
        }

        /*rowList.add(new GridPagerRow(CardFragment.create("titre 1","contenu 1"),
                CardFragment.create("HAHA","YOLO")));
        rowList.add(new GridPagerRow(CardFragment.create("titre 2","contenu 2")));*/
    }

    @Override
    public Fragment getFragment(int row, int col) {
        return rowList.get(row).getColumn(col);
    }

    @Override
    public int getRowCount() {
        return rowList.size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        return rowList.get(rowNum).getColumnCount();
    }

}
