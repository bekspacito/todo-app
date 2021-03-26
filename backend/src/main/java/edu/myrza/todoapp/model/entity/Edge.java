package edu.myrza.todoapp.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/*
    CLOSURE TABLE - technique for storing hierarchical structures in db
*/
@Getter
@Setter

@Entity
@Table(name = "edge")
@NoArgsConstructor
@AllArgsConstructor
public class Edge {

    public enum DESC_TYPE { FILE, FOLDER };
    public enum EDGE_TYPE { DIRECT, INDIRECT };

    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ancestor")
    private FolderRecord ancestor;

    private String descendant;

    @Enumerated(EnumType.STRING)
    private DESC_TYPE descType;
    @Enumerated(EnumType.STRING)
    private EDGE_TYPE edgeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "edge_owner_id")
    private User edgeOwner;
}
