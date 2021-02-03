package gdmarketpremium;

import gdmarketpremium.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired
    AlarmRepository alarmManagementRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void onStringEventListener(@Payload String eventString){

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverItemDeleted_(@Payload ItemDeleted itemDeleted){
        if(itemDeleted.isMe()){
            System.out.println("##### listener  : " + itemDeleted.toJson());
            if("DeleteAlerted".equals(itemDeleted.getAlarmStatus())){
                Alarm alarm = (Alarm) alarmManagementRepository.findByItemNo(itemDeleted.getItemNo()).get(0);
                alarm.setAlarmStatus("DeleteAlerted");
                alarmManagementRepository.save(alarm);
            }
        }
    }
}
