package gdmarketpremium;

import javax.persistence.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gdmarketpremium.config.kafka.KafkaProcessor;

import gdmarketpremium.external.AlarmService;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeTypeUtils;

@Entity
@Table(name="Item_table")
public class Item {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer itemNo;
    private String itemName;
    private String itemStatus;
    private Integer itemPrice;
    private String rentalStatus;
    private Integer reservationNo;

    @PostPersist
    public void onPostPersist(){
        ItemRegistered itemRegistered = new ItemRegistered();
        itemRegistered.setItemNo(this.getItemNo());
        itemRegistered.setItemName(this.getItemName());
        itemRegistered.setItemPrice(this.getItemPrice());
        itemRegistered.setItemStatus("Rentable");
        itemRegistered.setRentalStatus("NotRenting");
        itemRegistered.setAlarmStatus("Alerted");

        // alarm REQ/RES
        System.out.println("@@@ Alarm @@@");
        System.out.println("@@@ ItemNo : " + getItemNo());

        gdmarketpremium.external.Alarm alarm = new gdmarketpremium.external.Alarm();
        alarm.setAlarmStatus("Alerted");
        alarm.setAlarmNo(getItemNo());
        alarm.setItemNo(getItemNo());

        ItemApplication.applicationContext.getBean(AlarmService.class).alert(alarm);

        // view를 위해 Kafka Send
        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(itemRegistered);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }
        KafkaProcessor processor = ItemApplication.applicationContext.getBean(KafkaProcessor.class);
        MessageChannel outputChannel = processor.outboundTopic();
        outputChannel.send(org.springframework.integration.support.MessageBuilder
                .withPayload(json)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());

        System.out.println("@@@@@@@ ItemRegistered to Json @@@@@@@");
        System.out.println(itemRegistered.toJson());
    }

    @PostUpdate
    public void onPostUpdate() {
        if ("Renting".equals(this.getRentalStatus())) {
            RentedItem rentedItem = new RentedItem();
            rentedItem.setItemNo(this.getItemNo());
            rentedItem.setReservationNo(this.getReservationNo());
            rentedItem.setRentalStatus("Renting");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = null;
            try {
                json = objectMapper.writeValueAsString(rentedItem);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON format exception", e);
            }
            KafkaProcessor processor = ItemApplication.applicationContext.getBean(KafkaProcessor.class);
            MessageChannel outputChannel = processor.outboundTopic();
            outputChannel.send(org.springframework.integration.support.MessageBuilder
                    .withPayload(json)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());
            System.out.println("@@@@@@@ rentedItem to Json @@@@@@@");
            System.out.println(rentedItem.toJson());
        }
        if ("Returned".equals(this.getRentalStatus())) {
            ReturnedItem returnedItem = new ReturnedItem();
            returnedItem.setItemNo(this.getItemNo());
            returnedItem.setReservationNo(this.getReservationNo());
            returnedItem.setRentalStatus("Returned");

            ObjectMapper objectMapper = new ObjectMapper();
            String json = null;
            try {
                json = objectMapper.writeValueAsString(returnedItem);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON format exception", e);
            }
            KafkaProcessor processor = ItemApplication.applicationContext.getBean(KafkaProcessor.class);
            MessageChannel outputChannel = processor.outboundTopic();
            outputChannel.send(org.springframework.integration.support.MessageBuilder
                    .withPayload(json)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());
            System.out.println("@@@@@@@ returnedItem to Json @@@@@@@");
            System.out.println(returnedItem.toJson());
        }
    }

    @PreRemove
    public void onPreRemove() {
        ItemDeleted itemDeleted = new ItemDeleted();
        itemDeleted.setItemNo(this.getItemNo());
        itemDeleted.setAlarmStatus("DeleteAlerted");

        ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(itemDeleted);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON format exception", e);
        }
        KafkaProcessor processor = ItemApplication.applicationContext.getBean(KafkaProcessor.class);
        MessageChannel outputChannel = processor.outboundTopic();
        outputChannel.send(org.springframework.integration.support.MessageBuilder
                .withPayload(json)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build());
        System.out.println("@@@@@@@ itemDeleted to Json @@@@@@@");
        System.out.println(itemDeleted.toJson());
    }


    public Integer getItemNo () {
        return itemNo;
    }
    public void setItemNo (Integer itemNo){
        this.itemNo = itemNo;
    }

    public String getItemName () {
        return itemName;
    }
    public void setItemName (String itemName){
        this.itemName = itemName;
    }

    public String getItemStatus () {
        return itemStatus;
    }
    public void setItemStatus (String itemStatus){
        this.itemStatus = itemStatus;
    }

    public Integer getItemPrice () {
        return itemPrice;
    }
    public void setItemPrice (Integer itemPrice){
        this.itemPrice = itemPrice;
    }

    private String getRentalStatus () {
        return rentalStatus;
    }
    public void setRentalStatus (String rentalStatus){
        this.rentalStatus = rentalStatus;
    }

    private Integer getReservationNo() {
        return reservationNo;
    }
    public void setReservationNo (Integer reservationNo){
        this.reservationNo = reservationNo;
    }
}