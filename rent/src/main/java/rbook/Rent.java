package rbook;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Rent_table")
public class Rent {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long rentid;
    private Long userid;
    private Long bookid;
    private String status;

    private static final String STATUS_RENTED = "대여";
    private static final String STATUS_RETURNED = "반납";

    @PrePersist
    public void onPrePersist() throws Exception {
        //book 재고 확인
         boolean result = RentApplication.applicationContext.getBean(rbook.external.BookService.class)
                .chkAndUpdateStock(this.bookid);
        System.out.println("book 재고 확인 : " + result);
        if (result) {
            //book 대여 가능 상태, 상태를 대여로 set
            this.status = STATUS_RENTED;
            System.out.println(this.getBookid() + " 책 대여 가능합니다.");
        } else {
            throw new Exception(this.getBookid() + " 책은 재고가 없어 대여가 불가합니다.");
        }
    }

    @PostPersist
    public void onPostPersist() {
        //Rent 저장 후 Rented 이벤트를 pub 한다. 
        System.out.println("Rent 저장 후 Rented 이벤트를 pub");
        Rented rented = new Rented();
        BeanUtils.copyProperties(this, rented);
        rented.publishAfterCommit();
    }

    @PreUpdate
    public void onPreUpdate() {
        //상태를 반납으로 set 
        System.out.println("상태를 반납으로 set");
        this.status = STATUS_RETURNED;
    }

    @PostUpdate
    public void onPostUpdate() {
        //returned 저장 후 Returned 이벤트를 pub 한다. 
        System.out.println("returned 저장 후 Returned 이벤트를 pub");
        Returned returned = new Returned();
        BeanUtils.copyProperties(this, returned);
        returned.publishAfterCommit();
    }


    public Long getRentid() {
        return rentid;
    }

    public void setRentid(Long rentid) {
        this.rentid = rentid;
    }
    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }
    public Long getBookid() {
        return bookid;
    }

    public void setBookid(Long bookid) {
        this.bookid = bookid;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }




}
