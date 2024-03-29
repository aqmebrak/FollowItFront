package polytech.followit.model;


import android.os.Parcel;
import android.os.Parcelable;

public class POI implements Parcelable {

    private String name = null;
    private String node = null;
    private String discount = null;
    private String imageB64 = null;
    private boolean selected = false;

    public POI(String name, String node, String discount, String imageB64 , boolean selected) {
        super();
        this.name = name;
        this.node = node;
        this.discount = discount;
        this.selected = selected;
        this.imageB64 = imageB64;
    }

    public POI(String name, String node,  String imageB64 ,boolean selected) {
        super();
        this.name = name;
        this.node = node;
        this.selected = selected;
        this.imageB64 = imageB64;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getNode() {
        return node;
    }

    public String getDiscount() {
        return discount;
    }

    public String getImageB64() {
        return imageB64;
    }

    //==============================================================================================
    // Parcelable implementation
    //==============================================================================================

    protected POI(Parcel in) {
        name = in.readString();
        node = in.readString();
        discount = in.readString();
        selected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(node);
        dest.writeString(discount);
        dest.writeByte((byte) (selected ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<POI> CREATOR = new Creator<POI>() {
        @Override
        public POI createFromParcel(Parcel in) {
            return new POI(in);
        }

        @Override
        public POI[] newArray(int size) {
            return new POI[size];
        }
    };

    @Override
    public String toString() {
        return "POI{" +
                "name='" + name + '\'' +
                ", node='" + node + '\'' +
                ", selected='" + selected + '\'' +
                '}';
    }
}