package polytech.followit.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Discount implements Parcelable {

    private String POIname;
    private String discountText;
    private String poiImageB64;

    public Discount(String POIname, String discountText) {
        this.POIname = POIname;
        this.discountText = discountText;
    }

    public Discount(String POIname, String discountText, String poiImageB64) {
        this.POIname = POIname;
        this.discountText = discountText;
        this.poiImageB64 = poiImageB64;
    }

    public String getPOIname() {
        return POIname;
    }

    public String getDiscountText() {
        return discountText;
    }

    public String getPoiImageB64() {
        return poiImageB64;
    }

    //==============================================================================================
    // Parcelable implementation
    //==============================================================================================

    protected Discount(Parcel in) {
        POIname = in.readString();
        discountText = in.readString();
        poiImageB64 = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(POIname);
        dest.writeString(discountText);
        dest.writeString(poiImageB64);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Discount> CREATOR = new Creator<Discount>() {
        @Override
        public Discount createFromParcel(Parcel in) {
            return new Discount(in);
        }

        @Override
        public Discount[] newArray(int size) {
            return new Discount[size];
        }
    };
}
