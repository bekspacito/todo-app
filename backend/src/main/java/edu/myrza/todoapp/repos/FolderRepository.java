package edu.myrza.todoapp.repos;

import edu.myrza.todoapp.model.entity.FolderRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends JpaRepository<FolderRecord, String> { }
