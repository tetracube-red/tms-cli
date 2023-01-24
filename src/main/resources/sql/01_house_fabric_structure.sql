create table if not exists house_fabric.houses
(
    id uuid not null primary key,
    name varchar(255) not null,
    slug varchar(255) not null unique
);