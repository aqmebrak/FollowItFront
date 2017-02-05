package polytech.followit.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import java.util.ArrayList;

import polytech.followit.R;
import polytech.followit.model.POI;

/**
 * Created by Akme on 22/01/2017.
 */

public class MyCustomPOIListAdapter extends ArrayAdapter<POI> {

    private ArrayList<POI> POIList;
    int checkedPosition = -1;
    RadioButton selectedRb;

    public MyCustomPOIListAdapter(Context context, int textViewResourceId, ArrayList<POI> POIList) {
        super(context, textViewResourceId, POIList);
        this.POIList = new ArrayList<POI>();
        this.POIList.addAll(POIList);
    }

    private class ViewHolder {
        RadioButton name;
    }

    public ArrayList<POI> getPOIList() {
        return POIList;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        //Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.poi_info, null);

            holder = new ViewHolder();
            holder.name = (RadioButton) convertView.findViewById(R.id.radioButton);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        POI poi = POIList.get(position);
        holder.name.setText(poi.getName());
        holder.name.setChecked(poi.isSelected());
        holder.name.setTag(poi);
        holder.name.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("ADAPTER: ", "ONCLICK " + position + "" + checkedPosition);
                //premier clic: on met la checkbox de la position a true, on garde en memoire la position Et la checkbox

                //second click:
                //si ce n'est pas la meme checkbox, alors on coche la checkbox position et on decoche la checkbox checkedposition
                //on garde en memoire la checkbox et la position ==> checkedposition

                if(checkedPosition < 0 && selectedRb == null){
                    RadioButton rb = (RadioButton) v;
                    rb.setChecked(true);
                    POI p = POIList.get(position);
                    p.setSelected(true);
                    checkedPosition = position;
                    selectedRb = rb;
                }

                if(position != checkedPosition){
                    //uncheck
                    selectedRb.setChecked(false);
                    POIList.get(checkedPosition).setSelected(false);
                    //check new position
                    RadioButton rb = (RadioButton) v;
                    rb.setChecked(true);
                    POIList.get(position).setSelected(true);
                    checkedPosition = position;
                    selectedRb = rb;
                }

            }
        });

        return convertView;

    }

}
