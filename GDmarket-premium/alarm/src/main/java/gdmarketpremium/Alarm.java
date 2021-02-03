package gdmarketpremium;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.List;

@Entity
@Table(name="Alarm_table")
public class Alarm {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Integer alarmNo;
    private String alarmStatus;
    private Integer itemNo;

    @PrePersist
    public void onPrePersist(){
        Alerted alerted = new Alerted();
        BeanUtils.copyProperties(this, alerted);
        alerted.publishAfterCommit();
    }

    public Integer getAlarmNo() {
        return alarmNo;
    }
    public void setAlarmNo(Integer alarmNo) {
        this.alarmNo = alarmNo;
    }

    public String getAlarmStatus() {
        return alarmStatus;
    }
    public void setAlarmStatus(String alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    public Integer getItemNo() {
        return itemNo;
    }
    public void setItemNo(Integer itemNo) {
        this.itemNo = itemNo;
    }

}
