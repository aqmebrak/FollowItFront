package polytech.followit.model;


public class POI {

    private String name = null;
    private String node = null;
    private boolean selected = false;

    public POI(String name, String node, boolean selected) {
        super();
        this.name = name;
        this.node = node;
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

    public String getNode() {
        return node;
    }
}