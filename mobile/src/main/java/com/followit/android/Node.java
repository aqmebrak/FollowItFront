package com.followit.android;

import java.io.Serializable;
import java.util.ArrayList;

public class Node implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private ArrayList<String> poi;
    private String instruction;

    public Node(String name, ArrayList<String> poi, String instruction) {
        this.name = name;
        this.poi = poi;
        this.instruction = instruction;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getPoi() {
        return poi;
    }

    public String getInstruction() {
        return instruction;
    }

    @Override
    public String toString() {
        return name + " " + poi.toString() + " " + instruction;
    }
}
