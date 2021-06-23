package rbook;

import rbook.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class MyPageViewHandler {


    @Autowired
    private MyPageRepository myPageRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenRented_then_CREATE_1 (@Payload Rented rented) {
        try {

            if (!rented.validate()) return;

            // view 객체 생성
            MyPage myPage = new MyPage();
            // view 객체에 이벤트의 Value 를 set 함
            myPage.setRentid(rented.getRentid());
            myPage.setUserid(rented.getUserid());
            myPage.setBookid(rented.getBookid());
            myPage.setRentstatus(rented.getStatus());
            // view 레파지 토리에 save
            myPageRepository.save(myPage);
        
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenRegisteredBill_then_UPDATE_1(@Payload RegisteredBill registeredBill) {
        try {
            if (!registeredBill.validate()) return;
                // view 객체 조회
            List<MyPage> myPageList = myPageRepository.findByRentid(registeredBill.getRentid());
            for(MyPage myPage : myPageList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                myPage.setBillid(registeredBill.getBillid());
                myPage.setFee(registeredBill.getFee());
                myPage.setBillstatus(registeredBill.getStatus());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReturned_then_UPDATE_2(@Payload Returned returned) {
        try {
            if (!returned.validate()) return;
                // view 객체 조회
            List<MyPage> myPageList = myPageRepository.findByRentid(returned.getRentid());
            for(MyPage myPage : myPageList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                myPage.setRentstatus(returned.getStatus());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenPaid_then_UPDATE_3(@Payload Paid paid) {
        try {
            if (!paid.validate()) return;
                // view 객체 조회
            List<MyPage> myPageList = myPageRepository.findByBillid(paid.getBillid());
            for(MyPage myPage : myPageList){
                // view 객체에 이벤트의 eventDirectValue 를 set 함
                myPage.setBillstatus(paid.getStatus());
                // view 레파지 토리에 save
                myPageRepository.save(myPage);
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}