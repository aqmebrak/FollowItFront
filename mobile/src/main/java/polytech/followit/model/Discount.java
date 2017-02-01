package polytech.followit.model;

/**
 * Created by Akme on 01/02/2017.
 */
public class Discount {

    private String POIname;
    private String discountText;

    public Discount(String POIname, String discountText) {
        this.POIname = POIname;
        this.discountText = discountText;
    }

    public String getPOIname() {
        return POIname;
    }

    public void setPOIname(String POIname) {
        this.POIname = POIname;
    }

    public String getDiscountText() {
        return discountText;
    }

    public void setDiscountText(String discountText) {
        this.discountText = discountText;
    }
}
