insert into clients
values ('39db54e0-3627-4d6b-98a1-93ef930f96db'),
       ('53da57ce-5523-4c6e-ab61-68bddd0e64e9'),
       ('4ebbfb77-46c5-4d45-8657-5d30ee38424f'),
       ('c159dc02-50e2-4881-8737-1898a1c71d41'),
       ('792bb45c-0e37-4b7e-bd98-380ac7379b20');

insert into accounts(id, client_id, currency, balance)
values ('110dd9b9-a514-44af-aaed-5a3e915a0d1a', '39db54e0-3627-4d6b-98a1-93ef930f96db', 'USD', 0),
       ('02bbeffa-0de8-44f9-99a6-5ee2205ec2e9', '39db54e0-3627-4d6b-98a1-93ef930f96db', 'USD', 100000000),
       ('2e766e52-1417-490a-ad6d-6074e2b93c24', '39db54e0-3627-4d6b-98a1-93ef930f96db', 'EUR', 100000000),
       ('33bbbff2-6d23-4c9f-a456-4382aa45431a', '39db54e0-3627-4d6b-98a1-93ef930f96db', 'XYZ', 100000000),
       ('0e00ce5a-e2ac-461f-93bd-182202049364', '53da57ce-5523-4c6e-ab61-68bddd0e64e9', 'USD', 100000000),
       ('0ba0a07e-254b-4b0a-9348-7889c7905da2', '4ebbfb77-46c5-4d45-8657-5d30ee38424f', 'EUR', 100000000),
       ('696fc71e-9e99-4587-9310-780e608aa197', 'c159dc02-50e2-4881-8737-1898a1c71d41', 'XYZ', 100000000);



