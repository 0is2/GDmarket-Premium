package gdmarketpremium;

public class PaymentRequested extends AbstractEvent {

    private Integer reservationNo;
    private Integer itemPrice;
    private Integer itemNo;
    private String paymentStatus;

    public PaymentRequested(){
        super();
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

    public Integer getItemNo() {
        return itemNo;
    }
    public void setItemNo(Integer itemNo) {
        this.itemNo = itemNo;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}