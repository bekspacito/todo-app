package edu.myrza.todoapp.service;

import edu.myrza.todoapp.exceptions.SystemException;
import edu.myrza.todoapp.model.dto.files.FileFolderDto;
import edu.myrza.todoapp.model.entity.*;
import edu.myrza.todoapp.repos.EdgeRepository;
import edu.myrza.todoapp.repos.FileRepository;
import edu.myrza.todoapp.repos.FolderRepository;
import edu.myrza.todoapp.repos.StatusRepository;
import edu.myrza.todoapp.util.FileSystemUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FolderService {

    private final EdgeRepository edgeRepository;
    private final FolderRepository folderRepository;
    private final FileRepository fileRepository;
    private final StatusRepository statusRepository;
    private final FileSystemUtil fileSystemUtil;

    @Autowired
    public FolderService(
            FileSystemUtil fileSystemUtil,
            EdgeRepository edgeRepository,
            FolderRepository folderRepository,
            FileRepository fileRepository,
            StatusRepository statusRepository)
    {
        this.fileRepository = fileRepository;
        this.fileSystemUtil = fileSystemUtil;
        this.edgeRepository = edgeRepository;
        this.folderRepository = folderRepository;
        this.statusRepository = statusRepository;
    }

    // TODO : Make sure usernames are unique. (data invariant)
    public FolderRecord prepareUserRootFolder(User user) {

        try {
            // Create an actual folder/directory in fyle_system
            String rootFolderName = user.getUsername();
            fileSystemUtil.createUserRootFolder(rootFolderName);

            // Save a record about the created root folder in db
            FolderRecord rootFolderRecord = new FolderRecord();
            rootFolderRecord.setId(rootFolderName);
            rootFolderRecord.setName(rootFolderName);

            LocalDateTime _now = LocalDateTime.now();
            rootFolderRecord.setCreatedAt(_now);
            rootFolderRecord.setUpdatedAt(_now);

            rootFolderRecord.setOwner(user);
            rootFolderRecord.setStatus(statusRepository.findByCode(Status.Code.ENABLED));

            return folderRepository.save(rootFolderRecord);
        } catch (IOException ex) {
            throw new SystemException(ex, "Error creating a root folder for a user [" + user.getUsername() + "]");
        }
    }

    @Transactional
    public FolderRecord createFolder(User user, String parentId, String folderName) {

        // First we create folderRecord
        FolderRecord folderRecord = new FolderRecord();
        folderRecord.setId(UUID.randomUUID().toString());
        folderRecord.setName(folderName);

        LocalDateTime _now = LocalDateTime.now();
        folderRecord.setCreatedAt(_now);
        folderRecord.setUpdatedAt(_now);
        folderRecord.setOwner(user);
        folderRecord.setStatus(statusRepository.findByCode(Status.Code.ENABLED));

        FolderRecord newFolderRecord = folderRepository.save(folderRecord);
        // Then we create edges

        // access all of the ancestors of 'parent' folderRecord
        Set<Edge> ancestorsEdges = edgeRepository.serveAncestors(parentId).stream()
                        .map(ancestor -> new Edge(UUID.randomUUID().toString(), ancestor, newFolderRecord.getId(), Edge.DESC_TYPE.FOLDER, Edge.EDGE_TYPE.INDIRECT, user))
                        .collect(Collectors.toSet());

        Edge parentEdge = folderRepository.findById(parentId)
                            .map(parent -> new Edge(UUID.randomUUID().toString(), parent, newFolderRecord.getId(), Edge.DESC_TYPE.FOLDER, Edge.EDGE_TYPE.DIRECT, user))
                            .orElseThrow(() -> new RuntimeException("No folderRecord with id [" + parentId + "] is found"));

        ancestorsEdges.add(parentEdge);

        //save new edges
        edgeRepository.saveAll(ancestorsEdges);

        return newFolderRecord;
    }

    @Transactional
    public void deleteFolder(String folderId) {

        Status deleted = statusRepository.findByCode(Status.Code.DELETED);
        Set<FolderRecord> folders = edgeRepository.serveFolderDescendants(folderId);
        Set<FileRecord> files = edgeRepository.serveFileDescendants(folderId);

        // mark all of the descendant folders as 'deleted'
        for (FolderRecord folderRecord : folders) {
            folderRecord.setStatus(deleted);
        }

        // mark all of the descendant files as 'deleted'
        for (FileRecord fileRecord : files) {
            fileRecord.setStatus(deleted);
        }

        folderRepository.saveAll(folders);
        fileRepository.saveAll(files);
    }

    @Transactional
    public FolderRecord renameFolder(String folderId, String newName) {
        FolderRecord folderRecord = folderRepository.findById(folderId).orElseThrow(() -> new RuntimeException("No folderRecord with id [" + folderId + "] is found"));
        folderRecord.setName(newName);
        return folderRepository.save(folderRecord);
    }

}
