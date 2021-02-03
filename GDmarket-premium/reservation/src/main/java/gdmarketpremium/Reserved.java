package gdmarketpremium;

public class Reserved extends AbstractEvent {

    private Integer reservationNo;
    private String customerName;
    private Integer customerId;
    private Integer ItemNo;
    private String ItemName;
    private String ItemStatus;
    private Integer ItemPrice;

    public Integer getReservationNo() {
        return reservationNo;
    }
    public void setReservationNo(Integer reservationNo) {
        this.reservationNo = reservationNo;
    }

    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public Integer getItemNo() {
        return ItemNo;
    }
    public void setItemNo(Integer ItemNo) {
        this.ItemNo = ItemNo;
    }

    public String getItemName() {
        return ItemName;
    }
    public void setItemName(String ItemName) {
        this.ItemName = ItemName;
    }

    public String getItemStatus() {
        return ItemStatus;
    }
    public void setItemStatus(String ItemStatus) {
        this.ItemStatus = ItemStatus;
    }

    public Integer getItemPrice() {
        return ItemPrice;
    }
    public void setItemPrice(Integer ItemPrice) {
        this.ItemPrice = ItemPrice;
    }
}