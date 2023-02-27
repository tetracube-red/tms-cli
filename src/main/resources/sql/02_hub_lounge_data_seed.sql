insert into hub_lounge.domains_authorities(id, context, action)
VALUES ('86d14023-0fd5-40d4-8d75-8d6d98b3e02c', 'IOT', 'LIST_DEVICES');
insert into hub_lounge.domains_authorities(id, context, action)
VALUES ('8694854f-72a4-4af9-b65b-bdedf23f6cec', 'IOT', 'TOUCH_SWITCH');
insert into hub_lounge.domains_authorities(id, context, action)
VALUES ('86d14023-0fd5-40d4-8d75-8d6d98b3e02c', 'IOT', 'LIST_DEVICES');
insert into hub_lounge.domains_authorities(id, context, action)
VALUES ('8694854f-72a4-4af9-b65b-bdedf23f6cec', 'IOT', 'TOUCH_SWITCH');
INSERT INTO hub_lounge.guests_groups(id, name)
VALUES ('226a7aa5-3265-4b00-b48e-3fe8a4b5a3cf', 'Householder');
INSERT INTO hub_lounge.guests_groups(id, name)
VALUES ('5dbc9580-d342-4135-9b44-448b2e67e567', 'Guest');
insert into hub_lounge.domains_authorities_guests_groups(domainsAuthorities_id, guestsGroups_id)
values ('86d14023-0fd5-40d4-8d75-8d6d98b3e02c', '226a7aa5-3265-4b00-b48e-3fe8a4b5a3cf');
insert into hub_lounge.domains_authorities_guests_groups(domainsAuthorities_id, guestsGroups_id)
values ('8694854f-72a4-4af9-b65b-bdedf23f6cec', '226a7aa5-3265-4b00-b48e-3fe8a4b5a3cf');
insert into hub_lounge.domains_authorities_guests_groups(domainsAuthorities_id, guestsGroups_id)
values ('86d14023-0fd5-40d4-8d75-8d6d98b3e02c', '5dbc9580-d342-4135-9b44-448b2e67e567');