package rbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


 @RestController
 public class BookController {

        @Autowired
        BookRepository bookRepository;

        @RequestMapping(value = "/books/chkAndUpdateStock", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
        public boolean chkAndUpdateStock(HttpServletRequest request, HttpServletResponse response) throws Exception {
        
                 /* 서킷브레이커 시간지연
                 Thread.currentThread().sleep((long) (400 + Math.random() * 220));
                 */
                
                Integer stock;
                boolean result = false;  
                Long bookid = Long.valueOf(request.getParameter("bookid"));
                Book book = bookRepository.findByBookid(bookid);

                stock = book.getStock();
                System.out.println("### Stock......." + stock);

                if (stock > 0) {
                        result = true;
                        System.out.println("### result......." + result);
                
                        book.setStock(stock-1);
                        bookRepository.save(book);
                }
                return result;
        }


}
