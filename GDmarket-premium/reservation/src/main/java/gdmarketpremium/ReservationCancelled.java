package gdmarketpremium;

public class ReservationCancelled extends AbstractEvent {

    private Integer reservationNo;
    private String customerName;
    private Integer customerId;
    private String itemStatus;
    private Integer itemNo;

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
    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }
    public Integer getItemNo() {
        return itemNo;
    }

    public void setItemNo(Integer itemNo) {
        this.itemNo = itemNo;
    }
}