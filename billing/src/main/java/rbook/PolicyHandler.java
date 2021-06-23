package rbook;

import rbook.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class PolicyHandler{
    @Autowired BillingRepository billingRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRented_RegisterBill(@Payload Rented rented){

        if(!rented.validate()) return;

        Integer fee=1000;

        System.out.println("\n\n##### listener RegisterBill : " + rented.toJson() + "\n\n");

        // Bill 값 setting
        Billing billing = new Billing();
        billing.setRentid(rented.getRentid());
        billing.setFee(fee);
        billing.setStatus("Billing");
        billingRepository.save(billing);
            
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReturned_Payment(@Payload Returned returned){

        if(!returned.validate()) return;

        System.out.println("\n\n##### listener Payment : " + returned.toJson() + "\n\n");

        // Sample Logic //
        Billing billing = billingRepository.findByRentid(returned.getRentid());
        
        //해당 rentid 에 대한 bill 상테를 Paid 로 update
        if(billing != null){
            billing.setStatus("Paid");
            billingRepository.save(billing);
        }
            
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whatever(@Payload String eventString){}


}
