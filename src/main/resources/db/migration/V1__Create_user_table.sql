create TABLE `user` (
    id int  primary key AUTO_INCREMENT,
    username varchar(255) default '' unique key,
    password varchar(255) default '',
    createdAt timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);