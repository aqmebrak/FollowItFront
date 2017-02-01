package polytech.followit;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import polytech.followit.model.Discount;

/**
 * Created by Akme on 01/02/2017.
 */

public class MyCustomDiscountListAdapter extends ArrayAdapter<Discount> {

    private ArrayList<Discount> discountList;

    public MyCustomDiscountListAdapter(Context context, int textViewResourceId, ArrayList<Discount> discountList) {
        super(context, textViewResourceId, discountList);
        this.discountList = new ArrayList<>();
        this.discountList.addAll(discountList);
    }

    private class ViewHolder {
        TextView poiName;
        TextView discountText;
        ImageButton poiImage;
        Button toggleNotif;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        //Log.v("ConvertView", String.valueOf(position));

        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.discount_info, null);

            holder = new ViewHolder();
            holder.poiName = (TextView) convertView.findViewById(R.id.poi_name);
            holder.discountText = (TextView) convertView.findViewById(R.id.discount_text);
            holder.poiImage = (ImageButton) convertView.findViewById(R.id.poi_image);
            holder.poiImage.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_navigation_black_24dp));
            holder.toggleNotif = (Button) convertView.findViewById(R.id.toggle_notification_button);
            holder.toggleNotif.setBackground(convertView.getResources().getDrawable(R.drawable.ic_notifications_black_24dp));
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Discount discount = discountList.get(position);
        holder.poiName.setText(discount.getPOIname());
        holder.discountText.setText(discount.getDiscountText());

        return convertView;

    }
}
