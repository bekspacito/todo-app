package edu.myrza.todoapp.model.dto.files;

import edu.myrza.todoapp.model.entity.Edge;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DownloadFilesFoldersRequest {

    List<Body> ids = new ArrayList<>();

    @Getter
    @Setter
    public static class Body {
        private String id;
        private Edge.DESC_TYPE type;
    }
}
