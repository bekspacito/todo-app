package edu.myrza.todoapp.service;

import edu.myrza.todoapp.exceptions.SystemException;
import edu.myrza.todoapp.model.dto.files.FileRecordDto;
import edu.myrza.todoapp.model.entity.*;
import edu.myrza.todoapp.model.enums.EdgeType;
import edu.myrza.todoapp.model.enums.FileType;
import edu.myrza.todoapp.repos.EdgeRepository;
import edu.myrza.todoapp.repos.FileRepository;
import edu.myrza.todoapp.repos.StatusRepository;
import edu.myrza.todoapp.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileService {

    private final FileSystemUtil fileSystemUtil;
    private final StatusRepository statusRepository;
    private final FileRepository fileRepository;
    private final EdgeRepository edgeRepository;

    @Autowired
    public FileService(
            FileSystemUtil fileSystemUtil,
            StatusRepository statusRepository,
            FileRepository fileRepository,
            EdgeRepository edgeRepository)
    {
        this.fileSystemUtil = fileSystemUtil;
        this.statusRepository = statusRepository;
        this.fileRepository = fileRepository;
        this.edgeRepository = edgeRepository;
    }

    // FOLDER/FILE OPERATIONS

    @Transactional
    public void deleteFiles(User user, List<String> ids) {

        Status deleted = statusRepository.findByCode(Status.Code.DELETED);

        for(String id : ids) {

            Optional<FileRecord> optFile = fileRepository.findById(id);
            if(!optFile.isPresent())
                continue;

            FileRecord file = optFile.get();

            // mark the file as 'deleted'
            file.setStatus(deleted);
            if(!file.getFileType().equals(FileType.FOLDER)) {
                fileRepository.save(file);
                continue;
            }

            // if the file is a folder then mark it's sub folders/files as 'deleted'
            Set<FileRecord> descendants = edgeRepository.serveAllDescendants(file.getId());
            for(FileRecord descendant : descendants) {
                descendant.setStatus(deleted);
            }

            descendants.add(file);
            fileRepository.saveAll(descendants);

        }

    }

    @Transactional
    public Optional<FileRecordDto> renameFile(User user, String folderId, String newName) {
        Optional<FileRecord> optFileRecord = fileRepository.findById(folderId);
        if(optFileRecord.isPresent()) {
            FileRecord fileRecord = optFileRecord.get();
            fileRecord.setName(newName);
            fileRepository.save(fileRecord);
            return Optional.of(toDto(fileRecord));
        }

        return Optional.empty();
    }

    // Download multiple files
    @Transactional(readOnly = true)
    public Resource downloadFiles(User user, List<String> ids) throws IOException {

        // create a tree of files/folder you are gonna send back
        List<TreeNode> nodes = buildTree(fileRepository.findAllById(ids));

        // use the tree to create appropriate .zip file
        File compressedFile = fileSystemUtil.compressAndReturnFiles(user.getUsername(), nodes);

        // turn the .zip file into resource
        return new FileSystemResource(compressedFile);
    }

    // FOLDER OPERATIONS

    // TODO : Make sure usernames are unique. (data invariant)
    @Transactional
    public FileRecordDto prepareUserRootFolder(User user) {

        try {
            // Create an actual folder/directory in fyle_system
            String rootFolderName = user.getUsername();
            fileSystemUtil.createUserRootFolder(rootFolderName);

            // Save a record about the created root folder in db
            FileRecord rootFolderRecord = new FileRecord();
            rootFolderRecord.setId(rootFolderName);
            rootFolderRecord.setName(rootFolderName);

            LocalDateTime _now = LocalDateTime.now();
            rootFolderRecord.setCreatedAt(_now);
            rootFolderRecord.setUpdatedAt(_now);

            rootFolderRecord.setOwner(user);
            rootFolderRecord.setStatus(statusRepository.findByCode(Status.Code.ENABLED));
            rootFolderRecord.setFileType(FileType.FOLDER);

            return toDto(fileRepository.save(rootFolderRecord));
        } catch (IOException ex) {
            throw new SystemException(ex, "Error creating a root folder for a user [" + user.getUsername() + "]");
        }
    }

    @Transactional
    public FileRecordDto createFolder(User user, String parentId, String folderName) {

        // First we create folderRecord
        FileRecord folderRecord = new FileRecord();
        folderRecord.setId(UUID.randomUUID().toString());
        folderRecord.setName(folderName);

        LocalDateTime _now = LocalDateTime.now();
        folderRecord.setCreatedAt(_now);
        folderRecord.setUpdatedAt(_now);
        folderRecord.setOwner(user);
        folderRecord.setStatus(statusRepository.findByCode(Status.Code.ENABLED));

        FileRecord savedFolderRecord = fileRepository.save(folderRecord);

        // Then we create edges
        // access all of the ancestors of 'parent' folderRecord
        Set<Edge> ancestorsEdges = edgeRepository.serveAncestors(parentId).stream()
                .map(ancestor -> new Edge(UUID.randomUUID().toString(), ancestor, savedFolderRecord, EdgeType.INDIRECT, user))
                .collect(Collectors.toSet());

        // access 'parent' folder
        Edge parentEdge = fileRepository.findById(parentId)
                .map(parent -> new Edge(UUID.randomUUID().toString(), parent, savedFolderRecord, EdgeType.DIRECT, user))
                .orElseThrow(() -> new RuntimeException("No folderRecord with id [" + parentId + "] is found"));

        ancestorsEdges.add(parentEdge);

        //save new edges
        edgeRepository.saveAll(ancestorsEdges);

        return toDto(savedFolderRecord);
    }

    @Transactional(readOnly = true)
    public List<FileRecordDto> serveFolderContent(User user, String folderId) {
        return edgeRepository.serveDescendants(folderId, EdgeType.DIRECT).stream()
                             .map(this::toDto)
                             .collect(Collectors.toList());
    }

    // FILE OPERATIONS

    @Transactional
    public List<FileRecordDto> uploadFiles(User user, String folderId, MultipartFile[] files) {

        // Save files in disk
        List<MultipartFileDecorator> fileDecorators = Stream.of(files)
                .map(mf -> new MultipartFileDecorator(mf, UUID.randomUUID().toString()))
                .collect(Collectors.toList());

        List<MultipartFileDecorator> savedFiles = fileSystemUtil.saveFile(user.getUsername(), fileDecorators);

        // Save records about files
        List<FileRecord> fileRecords = new ArrayList<>();
        for(MultipartFileDecorator savedFile : savedFiles) {
            fileRecords.add(toFile(user, savedFile));
        }
        fileRecords = fileRepository.saveAll(fileRecords);

        // Create and save edges/connection from all of the ancestors to the files (The Closure table)
        FileRecord parent = fileRepository.getOne(folderId);
        Set<FileRecord> ancestors = edgeRepository.serveAncestors(folderId);
        ancestors.add(parent);

        List<Edge> edges = fileRecords.stream()
                                        .flatMap(descendant -> ancestors.stream().map(ancestor -> toEdge(user,parent,ancestor,descendant)))
                                        .collect(Collectors.toList());

        edgeRepository.saveAll(edges);

        return fileRecords.stream().map(this::toDto).collect(Collectors.toList());
    }

    // Download single file
    @Transactional
    public ResourceDecorator downloadFile(User user, String fileId) {

        FileRecord fileRecord = fileRepository.findById(fileId).orElseThrow(() -> new RuntimeException("File [" + fileId + "] not found"));

        // Check if the file has been deleted by owner
        if(fileRecord.getStatus().getCode().equals(Status.Code.DELETED)) {
            throw new RuntimeException("throw appropriate exception here");
        }

        ResourceDecorator resourceDecorator = new ResourceDecorator();

        File file = fileSystemUtil.serveFile(user.getUsername(), fileId, fileRecord.getExtension());
        resourceDecorator.setResource(new FileSystemResource(file));
        resourceDecorator.setOriginalName(fileRecord.getName());

        return resourceDecorator;
    }

    // HELPER OPERATIONS

    private List<TreeNode> buildTree(List<FileRecord> files) {
        List<TreeNode> nodes = new ArrayList<>();

        for(FileRecord file : files) {

            if(file.getStatus().getCode().equals(Status.Code.DELETED))
                continue;

            if(file.getFileType().equals(FileType.FILE)) {
                FileTreeNode treeNode = new FileTreeNode();
                treeNode.setId(file.getId());
                treeNode.setType(TreeNode.Type.FILE);
                treeNode.setName(file.getName());
                nodes.add(treeNode);
                continue;
            }

            if(file.getFileType().equals(FileType.FOLDER)) {
                FolderTreeNode folderTreeNode = new FolderTreeNode();
                folderTreeNode.setId(file.getId());
                folderTreeNode.setName(file.getName());
                folderTreeNode.setType(TreeNode.Type.FOLDER);

                List<FileRecord> subFiles = edgeRepository.serveDescendants(file.getId(), EdgeType.DIRECT);

                folderTreeNode.setSubnodes(buildTree(subFiles));
                nodes.add(folderTreeNode);
            }
        }

        return nodes;
    }

    private String extractExt(String fileOriginalName) {

        if(fileOriginalName == null || fileOriginalName.isEmpty()) return "";

        int lastIndexOfDot = fileOriginalName.lastIndexOf('.');
        if(lastIndexOfDot < 0 || lastIndexOfDot == fileOriginalName.length() - 1) return "";

        return fileOriginalName.substring(lastIndexOfDot);
    }

    private FileRecord toFile(User owner, MultipartFileDecorator savedFile) {
        MultipartFile mFile = savedFile.getMultipartFile();

        FileRecord fileRecord = new FileRecord();
        fileRecord.setId(savedFile.getName());
        fileRecord.setName(mFile.getOriginalFilename());
        fileRecord.setExtension(extractExt(mFile.getOriginalFilename()));
        fileRecord.setOwner(owner);
        fileRecord.setSize(mFile.getSize());
        fileRecord.setStatus(statusRepository.findByCode(Status.Code.ENABLED));

        LocalDateTime _now = LocalDateTime.now();
        fileRecord.setCreatedAt(_now);
        fileRecord.setUpdatedAt(_now);

        return fileRecord;
    }

    private FileRecordDto toDto(FileRecord fileRecord) {
        FileRecordDto dto = new FileRecordDto();
        dto.setId(fileRecord.getId());
        dto.setName(fileRecord.getName());
        dto.setType(FileType.FILE);
        dto.setSize(fileRecord.getSize());
        dto.setLastUpdate(fileRecord.getUpdatedAt());
        return dto;
    }

    private Edge toEdge(User owner, FileRecord parent, FileRecord ancestor, FileRecord descendant) {
        Edge edge = new Edge();
        edge.setId(UUID.randomUUID().toString());
        edge.setAncestor(ancestor);
        edge.setDescendant(descendant);
        edge.setEdgeOwner(owner);
        if (ancestor.equals(parent)) {
            edge.setEdgeType(EdgeType.DIRECT);
        } else {
            edge.setEdgeType(EdgeType.INDIRECT);
        }
        return edge;
    }

}
