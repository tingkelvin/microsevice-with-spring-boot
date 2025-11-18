package se.magnus.microservices.core.lpr.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LicencePlateRepository extends JpaRepository<LicencePlateEntity, Long> {

  /**
   * Find all licence plates by source ID (camera/detector)
   */
  List<LicencePlateEntity> findBySourceId(String sourceId);

  /**
   * Find all licence plates by object UUID
   */
  Optional<LicencePlateEntity> findByObjectUuid(String objectUuid);

  /**
   * Find all licence plates by plate number
   */
  List<LicencePlateEntity> findByPlateNum(String plateNum);

  /**
   * Find all licence plates by source ID within a time range
   */
  List<LicencePlateEntity> findBySourceIdAndUnixTimeBetween(String sourceId, long startTime, long endTime);

  /**
   * Find all licence plates within a time range
   */
  List<LicencePlateEntity> findByUnixTimeBetween(long startTime, long endTime);

  /**
   * Find all licence plates after a specific time
   */
  List<LicencePlateEntity> findByUnixTimeGreaterThanEqual(long unixTime);

  /**
   * Check if a licence plate exists by object UUID
   */
  boolean existsByObjectUuid(String objectUuid);
}

