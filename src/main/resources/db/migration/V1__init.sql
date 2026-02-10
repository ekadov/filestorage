Create table users
(
    id       int primary key AUTO_INCREMENT,
    username varchar(255) not null,
    status   varchar(50)  not null
);

Create table files
(
    id       int primary key AUTO_INCREMENT,
    name     varchar(255) not null,
    location varchar(500) not null,
    status   varchar(50)  not null
);

Create table events
(
    id        int primary key AUTO_INCREMENT,
    user_id   int,
    file_id   int,
    status    varchar(50) not null,
    timestamp timestamp   not null default CURRENT_TIMESTAMP,
    foreign key (user_id) references users (id),
    foreign key (file_id) references files (id)
);

Create index idx_events_user_id on events (user_id);
Create index idx_events_file_id on events (file_id);
Create index idx_users_username on users (username);
