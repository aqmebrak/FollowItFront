package polytech.followit.utility;

/**
 * Created by Akme on 23/01/2017.
 */

///CLASS qui permet de remplacer un element par un autre (utile pour placer des progressbar)

import android.view.View;
import android.view.ViewGroup;

public class ViewGroupUtils {

    public static ViewGroup getParent(View view) {
        return (ViewGroup)view.getParent();
    }

    public static void removeView(View view) {
        ViewGroup parent = getParent(view);
        if(parent != null) {
            parent.removeView(view);
        }
    }

    public static void replaceView(View currentView, View newView) {
        ViewGroup parent = getParent(currentView);
        if(parent == null) {
            return;
        }
        final int index = parent.indexOfChild(currentView);
        removeView(currentView);
        removeView(newView);
        parent.addView(newView, index);
    }
}