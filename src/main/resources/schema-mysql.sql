create table if not exists IPCountryMap(

	id bigint(20) not null AUTO_INCREMENT,
	createdDate datetime not null,
	modifiedDate datetime not null,
	
	ipAddress varchar(45) NOT NULL UNIQUE,
	countryCode varchar(2) NOT NULL,
	
	version bigint(20) not null default 0,
	
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

create index IPCountryMap_ipAddress on IPCountryMap (ipAddress);


create table if not exists Question(

	id bigint(20) not null AUTO_INCREMENT,
	createdDate datetime not null,
	modifiedDate datetime not null,
	
	ipAddress varchar(45) NOT NULL,
	question LONGTEXT NOT NULL,
	questionHash varchar(500) NOT NULL,
	countryCode varchar(2) NOT NULL,
	
	version bigint(20) not null default 0,
	
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

create index Question_ipAddress_questionHash_createdDate on Question (ipAddress, questionHash, createdDate);
create index Question_countryCode_createdDate on Question (countryCode, createdDate);
create index Question_countryCode on Question (countryCode);

create table if not exists WordBlackList(

	id bigint(20) not null AUTO_INCREMENT,
	createdDate datetime not null,
	modifiedDate datetime not null,
	
	word varchar(255) NOT NULL,
	
	version bigint(20) not null default 0,
	
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

alter table WordBlackList add UNIQUE WordBlackList_word (word);

