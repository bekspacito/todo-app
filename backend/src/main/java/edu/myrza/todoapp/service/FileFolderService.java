package edu.myrza.todoapp.service;

import edu.myrza.todoapp.model.dto.files.DownloadFilesFoldersRequest;
import edu.myrza.todoapp.model.dto.files.FileFolderDto;
import edu.myrza.todoapp.model.entity.*;
import edu.myrza.todoapp.repos.EdgeRepository;
import edu.myrza.todoapp.repos.FileRepository;
import edu.myrza.todoapp.repos.FolderRepository;
import edu.myrza.todoapp.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileFolderService {

    @Autowired
    private EdgeRepository edgeRepository;
    @Autowired
    private FileSystemUtil fileSystemUtil;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private FolderRepository folderRepository;

    public List<FileFolderDto> serveFolderContent(User user, String folderUUID) {

        Stream<FileFolderDto> folders = edgeRepository.serveSubfolders(folderUUID, Edge.DESC_TYPE.FOLDER, Edge.EDGE_TYPE.DIRECT).stream().map(this::toDto);
        Stream<FileFolderDto> files = edgeRepository.serveFiles(folderUUID, Edge.DESC_TYPE.FILE, Edge.EDGE_TYPE.DIRECT).stream().map(this::toDto);

        return Stream.concat(folders, files).collect(Collectors.toList());
    }

    public Resource downloadFilesFolders(User user, DownloadFilesFoldersRequest req) throws IOException {

        List<FileFolderIdType> idTypes = req.getIds().stream().map(body -> new FileFolderIdType(body.getId(), body.getType())).collect(Collectors.toList());

        // create a tree of files/folder you are gonna send back
        List<TreeNode> nodes = buildTree(idTypes);

        // use the tree to create appropriate .zip file
        File compressedFile = fileSystemUtil.compressAndReturnFiles(user.getUsername(), nodes);

        // turn the .zip file into resource
        return new FileSystemResource(compressedFile);
    }

    public List<FileFolderDto> deleteFilesFolders() {

    }

    private List<TreeNode> buildTree(List<FileFolderIdType> bodies) {
        List<TreeNode> nodes = new ArrayList<>();

        for(FileFolderIdType body : bodies) {

            if(body.getType().equals(Edge.DESC_TYPE.FILE)) {
                Optional<FileRecord> optFileRecord = fileRepository.findById(body.getUuid());
                if(!optFileRecord.isPresent())
                    continue;
                FileRecord fileRecord = optFileRecord.get();
                if(fileRecord.getStatus().getCode().equals(Status.Code.DELETED))
                    continue;

                FileTreeNode treeNode = new FileTreeNode();
                treeNode.setId(fileRecord.getId());
                treeNode.setType(TreeNode.Type.FILE);
                treeNode.setName(fileRecord.getName());
                nodes.add(treeNode);
            }

            if(body.getType().equals(Edge.DESC_TYPE.FOLDER)) {
                Optional<FolderRecord> optFolderRecord = folderRepository.findById(body.getUuid());
                if(!optFolderRecord.isPresent())
                    continue;
                FolderRecord folderRecord = optFolderRecord.get();
                if(folderRecord.getStatus().getCode().equals(Status.Code.DELETED))
                    continue;

                FolderTreeNode folderTreeNode = new FolderTreeNode();
                folderTreeNode.setId(folderRecord.getId());
                folderTreeNode.setName(folderRecord.getName());
                folderTreeNode.setType(TreeNode.Type.FOLDER);

                List<FileFolderIdType> subfilesIds = edgeRepository.serveSubnodes(folderRecord.getId(), Edge.EDGE_TYPE.DIRECT);

                folderTreeNode.setSubnodes(buildTree(subfilesIds));
                nodes.add(folderTreeNode);
            }
        }

        return nodes;
    }

    private FileFolderDto toDto(FolderRecord folder) {
        FileFolderDto dto = new FileFolderDto();
        dto.setId(folder.getId());
        dto.setSize(-1);
        dto.setName(folder.getName());
        dto.setLastUpdate(folder.getUpdatedAt());
        dto.setType(FileFolderDto.Type.FOLDER);
        return dto;
    }

    private FileFolderDto toDto(FileRecord file) {
        FileFolderDto dto = new FileFolderDto();
        dto.setId(file.getId());
        dto.setSize(file.getSize());
        dto.setName(file.getName());
        dto.setLastUpdate(file.getUpdatedAt());
        dto.setType(FileFolderDto.Type.FILE);
        return dto;
    }

}
