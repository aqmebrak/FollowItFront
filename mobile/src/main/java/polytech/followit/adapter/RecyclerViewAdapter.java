package polytech.followit.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import polytech.followit.R;
import polytech.followit.model.Discount;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private ArrayList<Discount> discountList;

    public RecyclerViewAdapter(ArrayList<Discount> myValues) {
        this.discountList = myValues;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.discount_info, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.poi_name.setText(discountList.get(position).getPOIname());
        holder.discount_text.setText(discountList.get(position).getDiscountText());
        //construction du bitmap pour l'image
        //Log.d("RECYCLER" , "test image");

        if (discountList.get(position).getPoiImageB64() != null && !discountList.get(position).getPoiImageB64().isEmpty()) {
            //Log.d("RECYCLER" , "ON a une image");
            String decodeString = discountList.get(position).getPoiImageB64();
            decodeString =decodeString.substring(decodeString.indexOf(","));
            byte[] data = Base64.decode(decodeString, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(data, 0, data.length);
            holder.poi_image.setImageBitmap(decodedByte);
        }
    }


    @Override
    public int getItemCount() {
        return discountList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView poi_name;
        private TextView discount_text;
        private ImageView poi_image;

        public MyViewHolder(View itemView) {
            super(itemView);
            poi_name = (TextView) itemView.findViewById(R.id.poi_name);
            discount_text = (TextView) itemView.findViewById(R.id.discount_text);
            poi_image = (ImageView) itemView.findViewById(R.id.poi_image);
        }
    }
}
