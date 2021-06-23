package rbook;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Billing_table")
public class Billing {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long billid;
    private Long rentid;
    private Integer fee;
    private String status;

    @PostPersist
    public void onPostPersist(){
        RegisteredBill registeredBill = new RegisteredBill();
        BeanUtils.copyProperties(this, registeredBill);
        registeredBill.publishAfterCommit();
   
    }

    @PostUpdate
    public void onPostUpdate(){
        Paid paid = new Paid();
        BeanUtils.copyProperties(this, paid);
        paid.publishAfterCommit();

    }

    public Long getBillid() {
        return billid;
    }

    public void setBillid(Long billid) {
        this.billid = billid;
    }
    public Long getRentid() {
        return rentid;
    }

    public void setRentid(Long rentid) {
        this.rentid = rentid;
    }
    public Integer getFee() {
        return fee;
    }

    public void setFee(Integer fee) {
        this.fee = fee;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
