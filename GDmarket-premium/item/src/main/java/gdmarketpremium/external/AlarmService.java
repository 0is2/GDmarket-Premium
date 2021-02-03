
package gdmarketpremium.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="alarm", url="${api.alarm.url}")
public interface AlarmService {

    @RequestMapping(method= RequestMethod.POST, path="/alarms")
    public void alert(@RequestBody Alarm alarm);

}