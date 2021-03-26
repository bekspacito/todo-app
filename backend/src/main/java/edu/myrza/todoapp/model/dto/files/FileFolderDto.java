package edu.myrza.todoapp.model.dto.files;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FileFolderDto {

    public enum Type { FILE, FOLDER };

    private String id;
    private String name;
    private LocalDateTime lastUpdate;
    private Type type;
    private long size; // in bytes

}
