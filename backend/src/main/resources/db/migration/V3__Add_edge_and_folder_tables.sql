create table folder (
    id varchar(63) not null primary key,
    name varchar(127) not null,
    created_at timestamp not null,
    owner_id integer constraint fk_folder_usr references usr(id) on delete cascade,
    status varchar(31) not null
);

create table edge(
    id varchar(63) not null primary key,
    ancestor varchar(63) constraint fk_edge_folder references folder(id) on delete cascade,
    descendant varchar(63) not null,
    desc_type varchar(31) not null,
    edge_type varchar(31) not null,
    edge_owner_id integer constraint fk_edge_user references usr(id) on delete cascade
);