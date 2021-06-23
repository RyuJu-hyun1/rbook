package rbook;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="rents", path="rents")
public interface RentRepository extends PagingAndSortingRepository<Rent, Long>{


}
