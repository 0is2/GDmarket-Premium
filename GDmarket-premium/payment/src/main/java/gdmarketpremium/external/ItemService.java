
package gdmarketpremium.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name="item", url="http://item:8080")
public interface ItemService {

    @RequestMapping(method= RequestMethod.PATCH, path="/items")
    public void lendItem(@RequestBody Item item);

}