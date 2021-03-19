package edu.myrza.todoapp.service;

import edu.myrza.todoapp.exceptions.SystemException;
import edu.myrza.todoapp.model.entity.Edge;
import edu.myrza.todoapp.model.entity.Folder;
import edu.myrza.todoapp.model.entity.Status;
import edu.myrza.todoapp.model.entity.User;
import edu.myrza.todoapp.repos.EdgeRepository;
import edu.myrza.todoapp.repos.FolderRepository;
import edu.myrza.todoapp.repos.StatusRepository;
import edu.myrza.todoapp.util.FileSystemUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FolderService {

    private final EdgeRepository edgeRepository;
    private final FolderRepository folderRepository;
    private final FileSystemUtil fileSystemUtil;

    @Autowired
    public FolderService(
            FileSystemUtil fileSystemUtil,
            EdgeRepository edgeRepository,
            FolderRepository folderRepository,
            StatusRepository statusRepository)
    {
        this.fileSystemUtil = fileSystemUtil;
        this.edgeRepository = edgeRepository;
        this.folderRepository = folderRepository;
    }

    // TODO : Make sure usernames are unique. (data invariant)
    public Folder prepareUserRootFolder(User user) {

        try {
            // Create an actual folder/directory in fyle_system
            String rootFolderName = user.getUsername();
            fileSystemUtil.createUserRootFolder(rootFolderName);

            // Save a record about the created root folder in db
            Folder rootFolder = new Folder();
            rootFolder.setId(rootFolderName);
            rootFolder.setName(rootFolderName);
            rootFolder.setCreatedAt(LocalDateTime.now());
            rootFolder.setOwner(user);
            rootFolder.setStatus(Folder.Status.OK);

            return folderRepository.save(rootFolder);
        } catch (IOException ex) {
            throw new SystemException(ex, "Error creating a root folder for a user [" + user.getUsername() + "]");
        }
    }

    public Set<Folder> serveSubfolders(String folderId) {
        return edgeRepository.serveSubfolders(folderId, Edge.DESC_TYPE.FOLDER, Edge.EDGE_TYPE.DIRECT);
    }

    @Transactional
    public Folder createFolder(User user, String parentId, String folderName) {

        // First we create folder
        Folder folder = new Folder();
        folder.setId(UUID.randomUUID().toString());
        folder.setName(folderName);
        folder.setCreatedAt(LocalDateTime.now());
        folder.setOwner(user);
        folder.setStatus(Folder.Status.OK);

        Folder newFolder = folderRepository.save(folder);
        // Then we create edges

        // access all of the ancestors of 'parent' folder
        Set<Edge> ancestorsEdges = edgeRepository.serveAncestors(parentId).stream()
                        .map(ancestor -> new Edge(UUID.randomUUID().toString(), ancestor, newFolder.getId(), Edge.DESC_TYPE.FOLDER, Edge.EDGE_TYPE.INDIRECT, user))
                        .collect(Collectors.toSet());

        Edge parentEdge = folderRepository.findById(parentId)
                            .map(parent -> new Edge(UUID.randomUUID().toString(), parent, newFolder.getId(), Edge.DESC_TYPE.FOLDER, Edge.EDGE_TYPE.DIRECT, user))
                            .orElseThrow(() -> new RuntimeException("No folder with id [" + parentId + "] is found"));

        ancestorsEdges.add(parentEdge);

        //save new edges
        edgeRepository.saveAll(ancestorsEdges);

        return newFolder;
    }

    @Transactional
    public void deleteFolder(String folderId) {
        // access all descendant folders and mark them 'deleted'
        for (Folder folder : edgeRepository.serveFolderDescendants(folderId)) {
            folder.setStatus(Folder.Status.DELETED);
        }
    }

    @Transactional
    public Folder renameFolder(String folderId, String newName) {
        Folder folder = folderRepository.findById(folderId).orElseThrow(() -> new RuntimeException("No folder with id [" + folderId + "] is found"));
        folder.setName(newName);
        return folderRepository.save(folder);
    }

}
