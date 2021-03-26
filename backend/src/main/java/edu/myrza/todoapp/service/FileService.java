package edu.myrza.todoapp.service;

import edu.myrza.todoapp.model.dto.files.FileFolderDto;
import edu.myrza.todoapp.model.entity.*;
import edu.myrza.todoapp.repos.EdgeRepository;
import edu.myrza.todoapp.repos.FileRepository;
import edu.myrza.todoapp.repos.FolderRepository;
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
    private final FolderRepository folderRepository;

    @Autowired
    public FileService(
            FileSystemUtil fileSystemUtil,
            StatusRepository statusRepository,
            FileRepository fileRepository,
            EdgeRepository edgeRepository,
            FolderRepository folderRepository)
    {
        this.fileSystemUtil = fileSystemUtil;
        this.statusRepository = statusRepository;
        this.fileRepository = fileRepository;
        this.edgeRepository = edgeRepository;
        this.folderRepository = folderRepository;
    }

    @Transactional(readOnly = true)
    public List<FileFolderDto> serveFilesInFolder(User user, String folderId) {
        return edgeRepository.serveFiles(folderId, Edge.DESC_TYPE.FILE, Edge.EDGE_TYPE.DIRECT)
                            .stream()
                            .map(this::toDto)
                            .collect(Collectors.toList());
    }

    @Transactional
    public List<FileFolderDto> uploadFiles(User user, String folderId, MultipartFile[] files) {

        // Save files in disk
        List<MultipartFileDecorator> fileDecorators = Stream.of(files)
                .map(mf -> new MultipartFileDecorator(mf, UUID.randomUUID().toString()))
                .collect(Collectors.toList());

        List<MultipartFileDecorator> savedFiles = fileSystemUtil.saveFile(user.getUsername(), fileDecorators);

        // Save records about files
        List<FileRecord> fileRecordRecords = new ArrayList<>();
        for(MultipartFileDecorator savedFile : savedFiles) {
            fileRecordRecords.add(toFile(user, savedFile));
        }
        fileRecordRecords = fileRepository.saveAll(fileRecordRecords);

        // Create and save edges/connection from all of the ancestors to the files (The Closure table)
        FolderRecord parent = folderRepository.getOne(folderId);
        Set<FolderRecord> ancestors = edgeRepository.serveAncestors(folderId);
        ancestors.add(parent);

        List<Edge> edges = fileRecordRecords.stream()
                                        .flatMap(descendant -> ancestors.stream().map(ancestor -> toEdge(user,parent,ancestor,descendant)))
                                        .collect(Collectors.toList());

        edgeRepository.saveAll(edges);

        return fileRecordRecords.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ResourceDecorator serveFile(User user, String fileId) {

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

    @Transactional
    public void deleteFile(User user, String fileId) {
        fileRepository.findById(fileId)
                        .ifPresent(fileRecord -> {
                            fileRecord.setStatus(statusRepository.findByCode(Status.Code.DELETED));
                            fileRepository.save(fileRecord);
                        });
    }

    public FileFolderDto renameFile(String fileId, String fileNewName) {
        return null;
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

    private FileFolderDto toDto(FileRecord fileRecord) {
        FileFolderDto dto = new FileFolderDto();
        dto.setId(fileRecord.getId());
        dto.setName(fileRecord.getName());
        dto.setType(FileFolderDto.Type.FILE);
        dto.setSize(fileRecord.getSize());
        dto.setLastUpdate(fileRecord.getUpdatedAt());
        return dto;
    }

    private Edge toEdge(User owner, FolderRecord parent, FolderRecord ancestor, FileRecord descendant) {
        Edge edge = new Edge();
        edge.setId(UUID.randomUUID().toString());
        edge.setAncestor(ancestor);
        edge.setDescendant(descendant.getId());
        edge.setDescType(Edge.DESC_TYPE.FILE);
        edge.setEdgeOwner(owner);
        if (ancestor.equals(parent)) {
            edge.setEdgeType(Edge.EDGE_TYPE.DIRECT);
        } else {
            edge.setEdgeType(Edge.EDGE_TYPE.INDIRECT);
        }
        return edge;
    }

}
