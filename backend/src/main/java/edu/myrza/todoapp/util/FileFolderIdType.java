package edu.myrza.todoapp.util;

import edu.myrza.todoapp.model.entity.Edge;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileFolderIdType {

    private final String uuid;
    private final Edge.DESC_TYPE type;

}
