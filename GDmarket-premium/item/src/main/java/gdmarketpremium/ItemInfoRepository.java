package gdmarketpremium;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ItemInfoRepository extends CrudRepository<ItemInfo, Long> {
    List<ItemInfo> findByItemNo(Integer itemNo);
    void deleteByItemNo(Integer itemNo);
}