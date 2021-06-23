
package rbook.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

//@FeignClient(name="book", url="http://book:8080")
@FeignClient(name="book", url="http://localhost:8082")
public interface BookService {

    @RequestMapping(method= RequestMethod.GET, path="/books/chkAndUpdateStock")
    public boolean chkAndUpdateStock(@RequestParam("bookid") Long bookid);

}