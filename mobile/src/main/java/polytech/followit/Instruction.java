package polytech.followit;

/**
 * Created by Akme on 20/01/2017.
 */

public class Instruction {

    public String nodeToGoTo;
    public String nodeFrom;
    public String instruction;

    public Instruction(String nodeFrom, String instruction) {
        this.nodeFrom = nodeFrom;
        this.instruction = instruction;
    }

    public Instruction(String nodeToGoTo, String nodeFrom, String instruction) {
        this.nodeToGoTo = nodeToGoTo;
        this.nodeFrom = nodeFrom;
        this.instruction = instruction;
    }

    public void setNodeToGoTo(String nodeToGoTo) {
        this.nodeToGoTo = nodeToGoTo;
    }
}
