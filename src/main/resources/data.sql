
-- Note: am not pre populating blacklist with list of words from unit test. Below words are OK for basic smoke testing
-- and unit tests have proven that the functionality works  
insert into WordBlackList (createdDate, modifiedDate, version, word) values (now(), now(), 0, 'poo');
insert into WordBlackList (createdDate, modifiedDate, version, word) values (now(), now(), 0, 'wee');
insert into WordBlackList (createdDate, modifiedDate, version, word) values (now(), now(), 0, 'socialist');
insert into WordBlackList (createdDate, modifiedDate, version, word) values (now(), now(), 0, 'socialism');


-- insert into Question (createdDate, modifiedDate, ipAddress, question, questionHash, countryCode, version ) values (now(), now(), '1.1.1.1', 'how are you today?', '1234', 'lv', 0);