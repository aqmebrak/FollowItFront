package polytech.followit.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;

public class Instruction implements Parcelable {

    public String nodeToGoTo;
    public String nodeFrom;
    public String instruction;
    private int orientationIcon;
    private String orientation;
    public ArrayList<Discount> discountList;

    public Instruction(@Nullable String nodeToGoTo,
                       @Nullable String nodeFrom, String instruction,
                       @Nullable ArrayList<Discount> discountList,
                       int orientationIcon,
                       @Nullable String orientation) {
        this.nodeToGoTo = nodeToGoTo;
        this.nodeFrom = nodeFrom;
        this.instruction = instruction;
        this.orientationIcon = orientationIcon;
        this.orientation = orientation;
        this.discountList = new ArrayList<>();
        this.discountList.add(new Discount("cafet", "20% de remise immédiateblablabla"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
    }

    public String getInstruction() {
        return instruction;
    }

    public int getOrientationIcon() {
        return orientationIcon;
    }

    public String getOrientation() {
        return orientation;
    }

    //==============================================================================================
    // Parcelable implementation
    //==============================================================================================

    protected Instruction(Parcel in) {
        nodeToGoTo = in.readString();
        nodeFrom = in.readString();
        instruction = in.readString();
        orientationIcon = in.readInt();
        discountList = in.createTypedArrayList(Discount.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nodeToGoTo);
        dest.writeString(nodeFrom);
        dest.writeString(instruction);
        dest.writeInt(orientationIcon);
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
                "nodeToGoTo='" + nodeToGoTo + '\'' +
                ", nodeFrom='" + nodeFrom + '\'' +
                ", instruction='" + instruction + '\'' +
                ", discountList=" + discountList +
                '}';
    }
}
