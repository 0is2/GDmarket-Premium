package gdmarketpremium;

public class ReturnedItem extends AbstractEvent {

    private Integer reservationNo;
    private String customerName;
    private Integer customerId;
    private String rentalStatus;
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
    public String getRentalStatus() {
        return rentalStatus;
    }

    public void setRentalStatus(String rentalStatus) {
        this.rentalStatus = rentalStatus;
    }
    public Integer getItemNo() {
        return itemNo;
    }

    public void setItemNo(Integer itemNo) {
        this.itemNo = itemNo;
    }
}