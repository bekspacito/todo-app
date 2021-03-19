package edu.myrza.todoapp.repos;

import edu.myrza.todoapp.model.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {

    Status findByCode(Status.Code code);

}
