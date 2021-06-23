package rbook;

import rbook.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired BookRepository bookRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReturned_UpdateStock(@Payload Returned returned){

        Integer stock;
        if(!returned.validate()) return;
                    
        Book book = bookRepository.findByBookid(Long.valueOf(returned.getBookid()));
        
        // 재고 증가
        stock = book.getStock();
        book.setStock(stock+1);
           
        bookRepository.save(book);
                    
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
