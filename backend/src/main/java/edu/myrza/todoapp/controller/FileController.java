package edu.myrza.todoapp.controller;

import edu.myrza.todoapp.model.dto.files.FileIdsWrapper;
import edu.myrza.todoapp.model.dto.files.FileRecordDto;
import edu.myrza.todoapp.model.entity.User;
import edu.myrza.todoapp.service.FileService;
import edu.myrza.todoapp.service.UserService;
import edu.myrza.todoapp.util.ResourceDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
public class FileController {

    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String CONTENT_DISPOSITION_ATTACH = "attachment; filename=\"%s\"";

    private final UserService userService;
    private final FileService fileService;

    @Autowired
    public FileController(UserService userService, FileService fileService) {
        this.userService = userService;
        this.fileService = fileService;
    }

    // OPERATIONS APPLIED TO BOTH FILES AND FOLDERS

    @DeleteMapping("/files/delete")
    public void deleteFiles(Principal principal, @RequestBody FileIdsWrapper idsWrapper) {

        User user = userService.loadUserByUsername(principal.getName());
        fileService.deleteFiles(user, idsWrapper.getFileIds());
    }

    @PostMapping("/file/{fileId}/rename/{newName}")
    public ResponseEntity<?> renameFile(Principal principal, @PathVariable("fileId") String fileId, @PathVariable("newName") String newName) {

        User user = userService.loadUserByUsername(principal.getName());
        Optional<FileRecordDto> optDto = fileService.renameFile(user, fileId, newName);

        return optDto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/files/download")
    public ResponseEntity<Resource> serveMultipleFiles(Principal principal, @RequestBody FileIdsWrapper idsWrapper) throws IOException {

        User user = userService.loadUserByUsername(principal.getName());
        Resource resource = fileService.downloadFiles(user, idsWrapper.getFileIds());

        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_ATTACH, "files.zip"))
                .contentLength(resource.contentLength())
                .body(resource);
    }

    // FOLDER OPERATIONS

    @PostMapping("/folder/{parentFolderId}/new/{newFolderName}")
    public FileRecordDto createFolder(
            Principal principal,
            @PathVariable("parentFolderId") String parentFolderId,
            @PathVariable("newFolderName") String newFolderName)
    {
        User user = userService.loadUserByUsername(principal.getName());
        return fileService.createFolder(user, parentFolderId, newFolderName);
    }

    @GetMapping("/folder/{folderId}/content")
    public List<FileRecordDto> serveFolderContent(Principal principal, @PathVariable("folderId") String folderId) {
        User user = userService.loadUserByUsername(principal.getName());
        return fileService.serveFolderContent(user, folderId);
    }

    // FILE OPERATIONS

    @PostMapping("/folder/{folderId}/upload")
    public List<FileRecordDto> uploadFiles(
            Principal principal,
            @PathVariable("folderId") String folderId,
            @ModelAttribute("files") MultipartFile[] files)
    {
        User user = userService.loadUserByUsername(principal.getName());
        return fileService.uploadFiles(user, folderId, files);
    }

    @GetMapping("/file/download/{fileId}")
    public ResponseEntity<Resource> serveSingleFile(Principal principal, @PathVariable("fileId") String fileId) throws IOException {
        User user = userService.loadUserByUsername(principal.getName());
        ResourceDecorator decorator = fileService.downloadFile(user, fileId);
        Resource resource = decorator.getResource();
        String origName = decorator.getOriginalName();

        return ResponseEntity.ok()
                             .header(CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_ATTACH, origName))
                             .contentLength(resource.contentLength())
                             .body(resource);
    }


}
