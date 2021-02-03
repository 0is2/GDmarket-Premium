package gdmarketpremium;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface AlarmRepository extends PagingAndSortingRepository<Alarm, Integer>{
    List<Object> findByItemNo(Integer itemNo);
}