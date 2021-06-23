package rbook;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;


@Entity
@Table(name="Book_table")
public class Book {

    @Id
    //@GeneratedValue(strategy=GenerationType.AUTO)
    private Long bookid;
    private Integer stock;

    @PostUpdate
    public void onPostUpdate(){
        UpdatedStock updatedStock = new UpdatedStock();
        BeanUtils.copyProperties(this, updatedStock);
        updatedStock.publishAfterCommit();

    }

    public Long getBookid() {
        return bookid;
    }

    public void setBookid(Long bookid) {
        this.bookid = bookid;
    }
    
    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }




}
