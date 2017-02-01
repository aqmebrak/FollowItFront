package polytech.followit.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

public class Instruction implements Parcelable {

    public String nodeToGoTo;
    public String nodeFrom;
    public String instruction;
    public ArrayList<Discount> discountList;

    private int indexOfInstruction = 1;

    public Instruction(@Nullable String nodeToGoTo, @Nullable String nodeFrom, String instruction, @Nullable ArrayList<Discount> discountList) {
        this.nodeToGoTo = nodeToGoTo;
        this.nodeFrom = nodeFrom;
        this.instruction = instruction;
        this.discountList = new ArrayList<>();
        this.discountList.add(new Discount("cafet", "20% de remise immédiateblablabla"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
        this.discountList.add(new Discount("LEARNING", "OUVERT TOUS LES JOURS JUSQUA 21h"));
    }

    protected Instruction(Parcel in) {
        nodeToGoTo = in.readString();
        nodeFrom = in.readString();
        instruction = in.readString();
        indexOfInstruction = in.readInt();
    }

    public static final Creator<Instruction> CREATOR = new Creator<Instruction>() {
        @Override
        public Instruction createFromParcel(Parcel in) {
            return new Instruction(in);
        }

        @Override
        public Instruction[] newArray(int size) {
            Log.d("INSTRUCTION CLASS", "NEW ARRAY" + size);
            return new Instruction[size];
        }
    };

    public String getInstruction() {
        return instruction;
    }

    public int getIndexOfInstruction() {
        return indexOfInstruction;
    }

    public void setIndexOfInstruction(int indexOfInstruction) {
        this.indexOfInstruction = indexOfInstruction;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nodeToGoTo);
        dest.writeString(nodeFrom);
        dest.writeString(instruction);
        dest.writeInt(indexOfInstruction);
    }
}
