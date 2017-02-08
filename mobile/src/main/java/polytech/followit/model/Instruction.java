package polytech.followit.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public class Instruction implements Parcelable {

    public String nodeFrom;
    public String instruction;
    private String orientation;
    public ArrayList<Discount> discountList;

    public Instruction(@Nullable String nodeFrom, String instruction,
                       @Nullable ArrayList<Discount> discountList,
                       @Nullable String orientation) {
        this.nodeFrom = nodeFrom;
        this.instruction = instruction;
        this.orientation = orientation;
        this.discountList = discountList;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getOrientation() {
        return orientation;
    }

    //==============================================================================================
    // Parcelable implementation
    //==============================================================================================

    protected Instruction(Parcel in) {
        nodeFrom = in.readString();
        instruction = in.readString();
        discountList = in.createTypedArrayList(Discount.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nodeFrom);
        dest.writeString(instruction);
        dest.writeTypedList(discountList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Instruction> CREATOR = new Creator<Instruction>() {
        @Override
        public Instruction createFromParcel(Parcel in) {
            return new Instruction(in);
        }

        @Override
        public Instruction[] newArray(int size) {
            return new Instruction[size];
        }
    };

    @Override
    public String toString() {
        return "Instruction{" +
                ", nodeFrom='" + nodeFrom + '\'' +
                ", instruction='" + instruction + '\'' +
                ", discountList=" + discountList +
                '}';
    }
}
