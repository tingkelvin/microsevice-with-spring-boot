package se.magnus.microservices.core.reid.persistence;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ReidRepository extends PagingAndSortingRepository<ReidEntity, String>, CrudRepository<ReidEntity, String> {

  List<ReidEntity> findBySourceId(String sourceId);
}

