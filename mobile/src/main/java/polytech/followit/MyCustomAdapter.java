package polytech.followit;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import java.util.ArrayList;

import polytech.followit.model.POI;

/**
 * Created by Akme on 22/01/2017.
 */

class MyCustomAdapter extends ArrayAdapter<POI> {



    private ArrayList<POI> POIList;

    public MyCustomAdapter(Context context, int textViewResourceId,ArrayList<POI> POIList) {
        super(context, textViewResourceId, POIList);
        this.POIList = new ArrayList<POI>();
        this.POIList.addAll(POIList);
    }

    private class ViewHolder {
        CheckBox name;
    }

    public ArrayList<POI> getPOIList() {
        return POIList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.poi_info, null);

            holder = new ViewHolder();
            holder.name = (CheckBox) convertView.findViewById(R.id.checkBox1);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        POI POI = POIList.get(position);
        holder.name.setText(POI.getName());
        holder.name.setChecked(POI.isSelected());
        holder.name.setTag(POI);
        holder.name.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                POI p = (POI) cb.getTag();
                p.setSelected(cb.isChecked());
            }
        });
        return convertView;

    }

}
