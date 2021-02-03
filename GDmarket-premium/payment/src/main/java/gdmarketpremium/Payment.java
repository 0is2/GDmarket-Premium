package gdmarketpremium;

import javax.persistence.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Payment_table")
public class Payment {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer paymentNo;
    private Integer reservationNo;
    private Integer itemPrice;
    private String paymentStatus;
    private Integer itemNo;

    @PostPersist
    public void onPostPersist(){

        if ("Paid".equals(paymentStatus) ) {
            System.out.println("=============결제 승인 처리중=============");
            PaymentApproved paymentCompleted = new PaymentApproved();

            paymentCompleted.setPaymentStatus("Paid");
            paymentCompleted.setReservationNo(reservationNo);
            paymentCompleted.setItemNo(itemNo);
            paymentCompleted.setItemPrice(itemPrice);

            BeanUtils.copyProperties(this, paymentCompleted);
            paymentCompleted.publishAfterCommit();

            try {
                Thread.currentThread().sleep((long) (400 + Math.random() * 220));
                System.out.println("=============결제 승인 완료=============");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @PreRemove
    public void onPreRemove(){
        PaymentCanceled paymentCanceled = new PaymentCanceled();
        paymentCanceled.setPaymentStatus("NotPaid");
        paymentCanceled.setReservationNo(reservationNo);
        paymentCanceled.setItemNo(itemNo);
        paymentCanceled.setItemPrice(itemPrice);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(paymentCanceled);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }

        System.out.println("### paymentCanceled Info ###");
        System.out.println(json);
    }

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
