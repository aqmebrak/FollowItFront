package polytech.followit;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;

import java.util.ArrayList;

public class GridPagerAdapter extends FragmentGridPagerAdapter {

    private final String TAG = GridPagerAdapter.class.getSimpleName();
    private final Context context;
    private final ArrayList<GridPagerRow> rowList = new ArrayList<>();

    public GridPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        Log.d(TAG, "ON GRID PAGER ADAPTER CONSTRUCTOR");
        this.context = context;
        GridPagerRow pagerRow = new GridPagerRow();

        if (ApplicationListener.instructions != null) {
            for (int i = 0; i < ApplicationListener.instructions.size(); i++) {
                CardFragment fragment;
                int icon = ApplicationListener.getIconByOrientation(ApplicationListener.listOrientation.get(i));
                fragment = CardFragment.create("Instruction", ApplicationListener.instructions.get(i), icon);
                pagerRow.addColumn(fragment);
            }
            rowList.add(pagerRow);
        } else
            rowList.add(new GridPagerRow(CardFragment.create("Oups !", "Veuillez définir votre destination")));

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

    @Override
    public Drawable getBackgroundForPage(int row, int column) {
        return context.getResources().getDrawable(R.drawable.background,null);
    }
}
