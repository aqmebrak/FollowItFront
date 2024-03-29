package polytech.followit.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public class Node implements Parcelable {

    private String name;
    private ArrayList<POI> poi;
    private Instruction instruction;
    private Beacon beacon;

    public Node(String name, ArrayList<POI> poi, @Nullable Instruction instruction, @Nullable Beacon beacon) {
        this.name = name;
        this.poi = poi;
        this.instruction = instruction;
        this.beacon = beacon;
    }

    public String getName() {
        return name;
    }

    public ArrayList<POI> getPoi() {
        return poi;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", poi=" + poi +
                ", instruction=" + instruction +
                ", beacon=" + beacon +
                '}';
    }

    //==============================================================================================
    // Parcelable implementation
    //==============================================================================================

    protected Node(Parcel in) {
        name = in.readString();
        poi = in.createTypedArrayList(POI.CREATOR);
        instruction = in.readParcelable(Instruction.class.getClassLoader());
        beacon = in.readParcelable(Beacon.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeTypedList(poi);
        dest.writeParcelable(instruction, flags);
        dest.writeParcelable(beacon, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Node> CREATOR = new Creator<Node>() {
        @Override
        public Node createFromParcel(Parcel in) {
            return new Node(in);
        }

        @Override
        public Node[] newArray(int size) {
            return new Node[size];
        }
    };

}
