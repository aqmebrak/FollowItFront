package polytech.followit.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Beacon implements Parcelable {

    private String name, UUID;
    private int major, minor;

    public Beacon(String name, String UUID, int major, int minor) {
        this.name = name;
        this.UUID = UUID;
        this.major = major;
        this.minor = minor;
    }

    protected Beacon(Parcel in) {
        name = in.readString();
        UUID = in.readString();
        major = in.readInt();
        minor = in.readInt();
    }

    public String getName() {
        return name;
    }

    public String getUUID() {
        return UUID;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    @Override
    public String toString() {
        return "Beacon{" +
                "name='" + name + '\'' +
                ", UUID='" + UUID + '\'' +
                ", major=" + major +
                ", minor=" + minor +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Beacon beacon = (Beacon) o;

        if (major != beacon.major) return false;
        if (minor != beacon.minor) return false;
        return UUID != null ? UUID.equals(beacon.UUID) : beacon.UUID == null;

    }

    @Override
    public int hashCode() {
        int result = UUID != null ? UUID.hashCode() : 0;
        result = 31 * result + major;
        result = 31 * result + minor;
        return result;
    }

    //==============================================================================================
    // Parcelable implementation
    //==============================================================================================

    public static final Creator<Beacon> CREATOR = new Creator<Beacon>() {
        @Override
        public Beacon createFromParcel(Parcel in) {
            return new Beacon(in);
        }

        @Override
        public Beacon[] newArray(int size) {
            return new Beacon[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(UUID);
        dest.writeInt(major);
        dest.writeInt(minor);
    }
}
