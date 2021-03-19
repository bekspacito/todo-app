package edu.myrza.todoapp.repos;

import edu.myrza.todoapp.model.entity.Edge;
import edu.myrza.todoapp.model.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;


@Repository
public interface EdgeRepository extends JpaRepository<Edge, String> {

    @Query("select e.ancestor from Edge e where e.descendant = :descendantId")
    Set<Folder> serveAncestors(@Param("descendantId") String fileId);

    @Query("select f from Folder f where f.id in " +
            "(select e.descendant from Edge e where e.ancestor.id=:ancestorId)")
    Set<Folder> serveFolderDescendants(@Param("ancestorId") String ancestorId);

    @Query("select f from Folder f where f.id in " +
            "(select e.descendant from Edge e where e.ancestor.id=:folderId " +
            "and e.descType=:descType " +
            "and e.edgeType=:edgeType)")
    Set<Folder> serveSubfolders(
            @Param("folderId") String folderId,
            @Param("descType") Edge.DESC_TYPE descType,
            @Param("edgeType") Edge.EDGE_TYPE edgeType);
}
