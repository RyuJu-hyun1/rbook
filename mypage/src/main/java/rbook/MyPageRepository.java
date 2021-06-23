package rbook;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyPageRepository extends CrudRepository<MyPage, Long> {

    List<MyPage> findByRentid(Long rentid);
    //List<MyPage> findByRentid(Long rentid);
    List<MyPage> findByBillid(Long billid);

}