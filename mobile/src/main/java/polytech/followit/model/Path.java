package polytech.followit.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Path implements Serializable {

    private ArrayList<Node> listNodes = new ArrayList<>();
    private ArrayList<Instruction> listInstructions = new ArrayList<>();
    private String source = null, destination = null;
    private ArrayList<Beacon> listBeacons = new ArrayList<>();

    public Path() {
    }

    public ArrayList<Node> getListNodes() {
        return listNodes;
    }

    public void setListNodes(ArrayList<Node> listNodes) {
        this.listNodes = listNodes;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ArrayList<Instruction> getListInstructions() {
        return listInstructions;
    }

    public void setListInstructions(ArrayList<Instruction> listInstructions) {
        this.listInstructions = listInstructions;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public ArrayList<Beacon> getListBeacons() {
        return listBeacons;
    }

    public void setListBeacons(ArrayList<Beacon> listBeacons) {
        this.listBeacons = listBeacons;
    }

    public ArrayList<String> listInstructionsToStringArray() {
        ArrayList<String> result = new ArrayList<>();
        for (Instruction instruction : listInstructions) {
            result.add(instruction.getInstruction());
        }
        return result;
    }
}
