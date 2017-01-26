package polytech.followit.model;

import android.support.annotation.Nullable;

import java.io.Serializable;

public class Instruction implements Serializable {

    public String nodeToGoTo;
    public String nodeFrom;
    public String instruction;

    public Instruction(@Nullable String nodeToGoTo, @Nullable String nodeFrom, String instruction) {
        this.nodeToGoTo = nodeToGoTo;
        this.nodeFrom = nodeFrom;
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }
}
