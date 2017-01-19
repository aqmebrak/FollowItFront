package com.followit.android;

/**
 * Created by Akme on 19/01/2017.
 */
public class POI {

    String name = null;
    boolean selected = false;

    public POI(String name, boolean selected) {
        super();
        this.name = name;
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

}