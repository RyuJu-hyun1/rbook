package rbook;

import org.springframework.data.repository.PagingAndSortingRepository;
//import org.springframework.data.rest.core.annotation.RepositoryRestResource;

//@RepositoryRestResource(collectionResourceRel="billings", path="billings")
public interface BillingRepository extends PagingAndSortingRepository<Billing, Long>{

    Billing findByRentid(Long rentid);

}
