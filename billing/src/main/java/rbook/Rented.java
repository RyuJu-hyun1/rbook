package rbook;

public class Rented extends AbstractEvent {

    private Long rentid;
    private Long userid;
    private Long bookid;
    private String status;

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