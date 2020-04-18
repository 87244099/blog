create TABLE blog (
    id int primary key AUTO_INCREMENT,
    title varchar(255) default '',
    userId int default 0,
    description text,
    content text,
    createdAt timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);