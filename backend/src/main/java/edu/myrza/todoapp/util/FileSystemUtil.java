package edu.myrza.todoapp.util;

/*
*  Encapsulates all the interactions with an actual file system.
* */

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileSystemUtil {

    @Value("${file.storage.dir}")
    private String root;

    public void createUserRootFolder(String username) throws IOException {

        Path path = Paths.get(root, username);
        Files.createDirectories(path);
    }

}
