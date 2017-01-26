package polytech.followit.model;

import java.io.Serializable;

public class Beacon implements Serializable{

    private String name, UUID;
    private int major, minor;

    public Beacon(String name, String UUID, int major, int minor) {
        this.name = name;
        this.UUID = UUID;
        this.major = major;
        this.minor = minor;
    }

    public String getName() {
        return name;
    }

    public String getUUID() {
        return UUID;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    @Override
    public String toString() {
        return "Beacon{" +
                "name='" + name + '\'' +
                ", UUID='" + UUID + '\'' +
                ", major=" + major +
                ", minor=" + minor +
                '}';
    }
}
