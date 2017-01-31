package polytech.followit.model;

import android.support.annotation.Nullable;

import java.util.ArrayList;

public class Node {

    private static final long serialVersionUID = 1L;

    private String name;
    private ArrayList<POI> poi;
    private Instruction instruction;
    private double xCoord, yCoord;
    private Beacon beacon;

    public Node(String name, ArrayList<POI> poi, @Nullable Instruction instruction, double xCoord, double yCoord, @Nullable Beacon beacon) {
        this.name = name;
        this.poi = poi;
        this.instruction = instruction;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
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

    public double getxCoord() {
        return xCoord;
    }

    public double getyCoord() {
        return yCoord;
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public boolean hasBeacon() {
        return this.beacon != null;
    }

    @Override
    public String toString() {
        return "Node{" +
                "name='" + name + '\'' +
                ", poi=" + poi +
                ", instruction='" + instruction + '\'' +
                ", xCoord=" + xCoord +
                ", yCoord=" + yCoord +
                '}';
    }
}
