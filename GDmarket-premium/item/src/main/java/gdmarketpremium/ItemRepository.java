package gdmarketpremium;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ItemRepository extends PagingAndSortingRepository<Item, Integer>{

    List<Object> findByItemNo(Integer itemNo);
}