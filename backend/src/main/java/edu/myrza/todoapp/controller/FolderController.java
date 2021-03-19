package edu.myrza.todoapp.controller;

import edu.myrza.todoapp.model.entity.Folder;
import edu.myrza.todoapp.model.entity.User;
import edu.myrza.todoapp.service.FolderService;
import edu.myrza.todoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping("/folder")
public class FolderController {

    private final UserService userService;
    private final FolderService folderService;

    @Autowired
    public FolderController(FolderService folderService, UserService userService) {
        this.folderService = folderService;
        this.userService = userService;
    }

    @GetMapping
    public Set<Folder> accessFolderContent(@RequestParam("folderId") String folderId) {
        return folderService.serveSubfolders(folderId);
    }

    @PostMapping("/rename")
    public Folder renameFolder(
            @RequestParam("folderId") String folderId,
            @RequestParam("newName") String newName)
    {
        return folderService.renameFolder(folderId, newName);
    }

    @PostMapping
    public Folder createFolder(
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
