package gdmarketpremium;

public class ItemDeleted extends AbstractEvent {

    private Integer itemNo;
    private String itemName;
    private String itemStatus;
    private Integer itemPrice;
    private String alarmStatus;

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
    public String getItemStatus() {
        return itemStatus;
    }
    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }
    public Integer getItemPrice() {
        return itemPrice;
    }
    public void setItemPrice(Integer itemPrice) {
        this.itemPrice = itemPrice;
    }
    public String getAlarmStatus() {
        return alarmStatus;
    }
    public void setAlarmStatus(String alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

}
