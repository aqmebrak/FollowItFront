package polytech.followit.model;


import android.os.Parcel;
import android.os.Parcelable;

public class POI implements Parcelable {

    private String name = null;
    private String node = null;
    private boolean selected = false;

    public POI(String name, String node, boolean selected) {
        super();
        this.name = name;
        this.node = node;
        this.selected = selected;
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

    //==============================================================================================
    // Parcelable implementation
    //==============================================================================================

    protected POI(Parcel in) {
        name = in.readString();
        node = in.readString();
        selected = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(node);
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
                '}';
    }
}