package gdmarketpremium;

import javax.persistence.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gdmarketpremium.config.kafka.KafkaProcessor;
import gdmarketpremium.external.Payment;
import gdmarketpremium.external.PaymentService;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeTypeUtils;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer reservationNo;
    private String customerName;
    private Integer customerId;
    private Integer itemNo;
    private String itemName;
    private Integer itemPrice;
    private String rentalStatus;
    private String paymentStatus;

    @PostPersist
    public void onPostPersist(){
        Reserved reserved = new Reserved();
        reserved.setReservationNo(this.getReservationNo());
        reserved.setCustomerName(this.getCustomerName());
        reserved.setCustomerId(this.getCustomerId());
        reserved.setItemStatus("NotRentable");
        reserved.setItemNo(this.getItemNo());
        reserved.setItemName(this.getItemName());
        reserved.setItemPrice(this.getItemPrice());

        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(reserved);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }
        KafkaProcessor processor = ReservationApplication.applicationContext.getBean(KafkaProcessor.class);
        MessageChannel outputChannel = processor.outboundTopic();
        outputChannel.send(MessageBuilder
                .withPayload(json)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());
        System.out.println("@@@@@@@ reserved to Json @@@@@@@");
        System.out.println(reserved.toJson());
    }


    @PreUpdate
    public void onPreUpdate(){

        if(("Paid".equals(this.getPaymentStatus()) || this.getPaymentStatus() == null) && "NotRenting".equals(this.getRentalStatus())) {
            Payment payment = new Payment();
            System.out.println("@@@ ReservationNo : " + getReservationNo());
            System.out.println("@@@ ItemNo : " + getItemNo());
            System.out.println("@@@ ItemPrice : " + getItemPrice());

            payment.setPaymentStatus("Paid");
            payment.setReservationNo(getReservationNo());
            payment.setItemNo(getItemNo());
            payment.setItemPrice(getItemPrice());

            ReservationApplication.applicationContext.getBean(PaymentService.class).approvePayment(payment);
            System.out.println("@@@ PaymentCompleted ReservationNumber= " + getReservationNo());
        }

        if("NotPaid".equals(this.getPaymentStatus())) {
            Payment payment = new Payment();
            System.out.println("### ReservationNo : " + getReservationNo());

            payment.setPaymentStatus("NotPaid");

            ReservationApplication.applicationContext.getBean(PaymentService.class).cancelPayment(getReservationNo());
            System.out.println("### PaymentCanceled ReservationNumber= " + getReservationNo());
        }
    }


    @PreRemove
    public void onPreRemove(){
        ReservationCancelled reservationCancelled = new ReservationCancelled();
        reservationCancelled.setReservationNo(this.getReservationNo());
        reservationCancelled.setCustomerName(this.getCustomerName());
        reservationCancelled.setCustomerId(this.getCustomerId());
        reservationCancelled.setItemStatus("Rentable");
        reservationCancelled.setItemNo(this.getItemNo());

        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(reservationCancelled);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }
        KafkaProcessor processor = ReservationApplication.applicationContext.getBean(KafkaProcessor.class);
        MessageChannel outputChannel = processor.outboundTopic();
        outputChannel.send(MessageBuilder
                .withPayload(json)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());
        System.out.println("@@@@@@@ reservationCancelled to Json @@@@@@@");
        System.out.println(reservationCancelled.toJson());
    }

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

    public Integer getItemNo() {
        return itemNo;
    }
    public void setItemNo(Integer itemNo) {
        this.itemNo = itemNo;
    }

    public String getItemName() {
        return itemName;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getRentalStatus() {
        return rentalStatus;
    }
    public void setRentalStatus(String rentalStatus) {
        this.rentalStatus = rentalStatus;
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
}
