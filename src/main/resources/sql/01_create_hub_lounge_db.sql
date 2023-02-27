create table hub_lounge.domains_authorities
(
    id      uuid         not null,
    context varchar(255) not null,
    action  varchar(255) not null,
    primary key (id)
);

create table hub_lounge.domains_authorities_guests_groups
(
    domainsAuthorities_id uuid not null,
    guestsGroups_id       uuid not null
);

create table hub_lounge.guests
(
    id             uuid         not null,
    name           varchar(255) not null,
    password       varchar(255) not null,
    guest_group_id uuid         not null,
    primary key (id)
);

create table hub_lounge.guests_groups
(
    id   uuid         not null,
    name varchar(255) not null,
    primary key (id)
);

create table hub_lounge.hubs
(
    id   uuid         not null,
    name varchar(255) not null,
    slug varchar(255) not null,
    primary key (id)
);

alter table if exists hub_lounge.guests
    add constraint UK_4sfxdegdke9sevyo1yg3he58w unique (name);

alter table if exists hub_lounge.guests_groups
    add constraint UK_lee9ootytsjbns9n3d1aqd2iy unique (name);

alter table if exists hub_lounge.hubs
    add constraint UK_pe9385is4x9qd44tyafm3cib5 unique (slug);

alter table if exists hub_lounge.domains_authorities_guests_groups
    add constraint FKmxvtyug71k3vt0imt8p4a1g6x
        foreign key (guestsGroups_id)
            references hub_lounge.guests_groups;

alter table if exists hub_lounge.domains_authorities_guests_groups
    add constraint FKkf8hwu0fv74td7qrtj7b3bdi9
        foreign key (domainsAuthorities_id)
            references hub_lounge.domains_authorities;

alter table if exists hub_lounge.guests
    add constraint FKe9arrfkca0tniswxam2n8q2gw
        foreign key (guest_group_id)
            references hub_lounge.guests_groups;
