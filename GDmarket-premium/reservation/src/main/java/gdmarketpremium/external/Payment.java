package gdmarketpremium.external;

public class Payment {

    private Integer paymentNo;
    private Integer reservationNo;
    private Integer itemPrice;
    private String paymentStatus;
    private Integer itemNo;

    public Integer getPaymentNo() {
        return paymentNo;
    }
    public void setPaymentNo(Integer paymentNo) {
        this.paymentNo = paymentNo;
    }
    public Integer getReservationNo() {
        return reservationNo;
    }
    public void setReservationNo(Integer reservationNo) {
        this.reservationNo = reservationNo;
    }
    public Integer getItemPrice() {
        return itemPrice;
    }
    public void setItemPrice(Integer itemPrice) {
        this.itemPrice = itemPrice;
    }
    public String getPaymentStatus() {
        return paymentStatus;
    }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    public Integer getItemNo() {
        return itemNo;
    }
    public void setItemNo(Integer itemNo) {
        this.itemNo = itemNo;
    }

}
