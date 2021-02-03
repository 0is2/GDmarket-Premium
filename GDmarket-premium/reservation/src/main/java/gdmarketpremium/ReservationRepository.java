package gdmarketpremium;

import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ReservationRepository extends PagingAndSortingRepository<Reservation, Integer>{
    List<Object> findByReservationNo(Integer reservationNo);
}