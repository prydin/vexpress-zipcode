package vexpress.zipcode;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZipCodeRepository extends CrudRepository<ZipCodeDetails, Integer> {
  List<ZipCodeDetails> findByZip(int zip);
}
