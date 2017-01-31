package polytech.followit.model;

import android.support.annotation.Nullable;

public class Instruction {

    public String nodeToGoTo;
    public String nodeFrom;
    public String instruction;
    private int indexOfInstruction = 1;

    public Instruction(@Nullable String nodeToGoTo, @Nullable String nodeFrom, String instruction) {
        this.nodeToGoTo = nodeToGoTo;
        this.nodeFrom = nodeFrom;
        this.instruction = instruction;
    }

    public String getInstruction() {
        return instruction;
    }

    public int getIndexOfInstruction() {
        return indexOfInstruction;
    }

    public void setIndexOfInstruction(int indexOfInstruction) {
        this.indexOfInstruction = indexOfInstruction;
    }
}
