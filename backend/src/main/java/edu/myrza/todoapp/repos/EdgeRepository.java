package edu.myrza.todoapp.repos;

import edu.myrza.todoapp.model.entity.Edge;
import edu.myrza.todoapp.model.entity.FileRecord;
import edu.myrza.todoapp.model.entity.FolderRecord;
import edu.myrza.todoapp.util.FileFolderIdType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


@Repository
public interface EdgeRepository extends JpaRepository<Edge, String> {

    @Query("select e.ancestor from Edge e where e.descendant = :descendantId")
    Set<FolderRecord> serveAncestors(@Param("descendantId") String fileId);

    @Query("select f from FolderRecord f where f.id in (select e.descendant from Edge e where e.ancestor.id=:ancestorId)")
    Set<FolderRecord> serveFolderDescendants(@Param("ancestorId") String ancestorId);

    @Query("select f from FileRecord f where f.id in (select e.descendant from Edge e where e.ancestor.id=:ancestorId)")
    Set<FileRecord> serveFileDescendants(@Param("ancestorId") String ancestorId);

    @Query("select f from FolderRecord f where f.id in " +
            "(select e.descendant from Edge e where e.ancestor.id=:folderId " +
            "and e.descType=:descType " +
            "and e.edgeType=:edgeType) " +
            "and f.status.code<>'DELETED'")
    Set<FolderRecord> serveSubfolders(
            @Param("folderId") String folderId,
            @Param("descType") Edge.DESC_TYPE descType,
            @Param("edgeType") Edge.EDGE_TYPE edgeType);

    @Query("select f from FileRecord f where f.id in " +
            "(select e.descendant from Edge e where e.ancestor.id=:folderId " +
            "and e.descType=:descType " +
            "and e.edgeType=:edgeType) " +
            "and f.status.code<>'DELETED'")
    Set<FileRecord> serveFiles(
            @Param("folderId") String folderId,
            @Param("descType") Edge.DESC_TYPE descType,
            @Param("edgeType") Edge.EDGE_TYPE edgeType);

    @Query("select new edu.myrza.todoapp.util.FileFolderIdType(e.id, e.descType) from Edge e where e.ancestor.id=:folderId and e.edgeType=:edgeType")
    List<FileFolderIdType> serveSubnodes(
            @Param("folderId") String folderId,
            @Param("edgeType") Edge.EDGE_TYPE edgeType);
}
