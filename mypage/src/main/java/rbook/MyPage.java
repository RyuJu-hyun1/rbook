package rbook;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="MyPage_table")
public class MyPage {

        @Id
        //@GeneratedValue(strategy=GenerationType.AUTO)
        private Long rentid;
        private Long userid;
         private Long bookid;
        private String rentstatus;
        private Long billid;
        private Integer fee;
        private String billstatus;

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
        public String getRentstatus() {
            return rentstatus;
        }

        public void setRentstatus(String rentstatus) {
            this.rentstatus = rentstatus;
        }
        public Long getBillid() {
            return billid;
        }

        public void setBillid(Long billid) {
            this.billid = billid;
        }
        public Integer getFee() {
            return fee;
        }

        public void setFee(Integer fee) {
            this.fee = fee;
        }
        public String getBillstatus() {
            return billstatus;
        }

        public void setBillstatus(String billstatus) {
            this.billstatus = billstatus;
        }

}
