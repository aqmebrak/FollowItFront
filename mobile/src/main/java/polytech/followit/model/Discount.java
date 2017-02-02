package polytech.followit.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Discount implements Parcelable {

    private String POIname;
    private String discountText;

    public Discount(String POIname, String discountText) {
        this.POIname = POIname;
        this.discountText = discountText;
    }

    public String getPOIname() {
        return POIname;
    }

    public String getDiscountText() {
        return discountText;
    }

    //==============================================================================================
    // Parcelable implementation
    //==============================================================================================

    protected Discount(Parcel in) {
        POIname = in.readString();
        discountText = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(POIname);
        dest.writeString(discountText);
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
