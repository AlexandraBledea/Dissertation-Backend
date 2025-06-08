package ubb.dissertation.benchmark.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ubb.dissertation.benchmark.entity.ExperimentEntity;
import ubb.dissertation.benchmark.entity.Status;

@Repository
public interface ExperimentRepository extends JpaRepository<ExperimentEntity, Long> {

    boolean existsByStatus(Status status);

}
