package edu.myrza.todoapp.controller;

import edu.myrza.todoapp.model.dto.files.DownloadFilesFoldersRequest;
import edu.myrza.todoapp.model.dto.files.FileFolderDto;
import edu.myrza.todoapp.model.entity.FileRecord;
import edu.myrza.todoapp.model.entity.FolderRecord;
import edu.myrza.todoapp.model.entity.User;
import edu.myrza.todoapp.service.FileFolderService;
import edu.myrza.todoapp.service.FileService;
import edu.myrza.todoapp.service.FolderService;
import edu.myrza.todoapp.service.UserService;
import edu.myrza.todoapp.util.ResourceDecorator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
public class FileFolderController {

    @Autowired
    private FolderService folderService;
    @Autowired
    private UserService userService;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileFolderService fileFolderService;

    //*****************
    //  FILE/FOLDER OPS (here are the ops that apply to both)
    //*****************

    @GetMapping("/folder/{folderId}")
    public List<FileFolderDto> serveFolderContent(Principal principal, @PathVariable("folderId") String folderId) {
        User user = userService.loadUserByUsername(principal.getName());

        return fileFolderService.serveFolderContent(user, folderId);
    }

    @GetMapping
    public ResponseEntity<Resource> downloadFilesFolders(Principal principal, @RequestBody DownloadFilesFoldersRequest reqBody) throws IOException
    {

        List<String> fileIds = reqBody.getIds();

        if(fileIds.size() > 1) {
            //return zip with given files
//            User user = userService.loadUserByUsername(principal.getName());
            User user = userService.loadUserByUsername(principal.getName());

            Resource resource = fileFolderService.downloadFilesFolders(user, reqBody);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"files.zip\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);


        } else if (fileIds.size() == 1){

            User user = userService.loadUserByUsername(principal.getName());

            ResourceDecorator resource = fileService.serveFile(user, fileIds.get(0));

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + resource.getOriginalName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.getResource().contentLength())
                    .body(resource.getResource());
        }

        // return empty .zip file
        return null;
    }

    @DeleteMapping
    public void deleteFilesFolders(
            Principal principal,
            @RequestParam("fileId") String fileId)
    {
        User user = userService.loadUserByUsername(principal.getName());

        fileService.deleteFile(user, fileId);
    }

    //*****************
    //  FOLDER OPS
    //*****************

    @PostMapping("/folder")
    public FolderRecord createFolder(
            Principal principal,
            @RequestParam("parentFolderId") String parentId,
            @RequestParam("folderName") String folderName)
    {
        User user = userService.loadUserByUsername(principal.getName());

        return folderService.createFolder(user, parentId, folderName);
    }

    @DeleteMapping("/folder/{folderId}")
    public void deleteFolder(@PathVariable("folderId") String folderId) {
        // TODO : doesn't work right, fix it !!!!
        folderService.deleteFolder(folderId);
    }

    @PostMapping("/rename")
    public FolderRecord renameFolder(
            @RequestParam("folderId") String folderId,
            @RequestParam("newName") String newName)
    {
        return folderService.renameFolder(folderId, newName);
    }

    //*****************
    //  FILE OPS
    //*****************

    @PostMapping("/rename")
    public FileFolderDto renameFile(
            @RequestParam("fileId") String fileId,
            @RequestParam("newName") String newName)
    {
        return fileService.renameFile(fileId, newName);
    }

    @PostMapping
    public List<FileFolderDto> uploadFiles(
            Principal principal,
            @RequestParam("folderId") String folderId,
            @RequestParam("files")  MultipartFile[] files)
    {
        User user = userService.loadUserByUsername(principal.getName());

        return fileService.uploadFiles(user, folderId, files);
    }
}
