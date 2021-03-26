package edu.myrza.todoapp.controller;

import edu.myrza.todoapp.model.dto.files.FileFolderDto;
import edu.myrza.todoapp.model.entity.FolderRecord;
import edu.myrza.todoapp.model.entity.User;
import edu.myrza.todoapp.service.FileService;
import edu.myrza.todoapp.service.FolderService;
import edu.myrza.todoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/folder")
public class FolderController {

    private final UserService userService;
    private final FolderService folderService;
    private final FileService fileService;

    @Autowired
    public FolderController(
            FolderService folderService,
            FileService fileService,
            UserService userService)
    {
        this.folderService = folderService;
        this.userService = userService;
        this.fileService = fileService;
    }

    @GetMapping
    public List<FileFolderDto> serveFiles(Principal principal, @RequestParam("folderId") String folderId) {
        User user = userService.loadUserByUsername(principal.getName());

        return folderService.serveFolderContent(user, folderId);
    }

    @PostMapping("/rename")
    public FolderRecord renameFolder(
            @RequestParam("folderId") String folderId,
            @RequestParam("newName") String newName)
    {
        return folderService.renameFolder(folderId, newName);
    }

    @PostMapping
    public FolderRecord createFolder(
            Principal principal,
            @RequestParam("parentFolderId") String parentId,
            @RequestParam("folderName") String folderName)
    {
        User user = userService.loadUserByUsername(principal.getName());
        return folderService.createFolder(user, parentId, folderName);
    }

    @DeleteMapping
    public void deleteFolder(@RequestParam("folderId") String folderId) {
        folderService.deleteFolder(folderId);
    }

}
