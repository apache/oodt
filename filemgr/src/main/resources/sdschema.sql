--
-- Table structure for table `dataPoint`
--

CREATE TABLE `dataPoint` (
  `dataPoint_id` int(10) unsigned NOT NULL auto_increment,
  `granule_id` int(10) unsigned NOT NULL,
  `dataset_id` int(10) unsigned NOT NULL,
  `parameter_id` int(10) unsigned NOT NULL,
  `time` datetime default NULL,
  `latitude` double default NULL,
  `longitude` double default NULL,
  `vertical` double default NULL,
  `value` double default NULL,
  PRIMARY KEY  (`dataPoint_id`)
) ENGINE=MyISAM AUTO_INCREMENT=1143297 DEFAULT CHARSET=latin1;

--
-- Table structure for table `dataset`
--

CREATE TABLE `dataset` (
  `dataset_id` int(10) unsigned NOT NULL auto_increment,
  `longName` varchar(120) default NULL,
  `shortName` varchar(60) default NULL,
  `description` text,
  `source` varchar(255) default NULL,
  `referenceURL` varchar(255) default NULL,
  PRIMARY KEY  (`dataset_id`)
) ENGINE=MyISAM AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;

--
-- Table structure for table `dpMap`
--

CREATE TABLE `dpMap` (
  `dataset_id` int(10) unsigned NOT NULL,
  `parameter_id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`dataset_id`,`parameter_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

--
-- Table structure for table `granule`
--
CREATE TABLE `granule` (
  `granule_id` int(10) unsigned NOT NULL auto_increment,
  `dataset_id` int(10) unsigned default NULL,
  `filename` varchar(255) NOT NULL,
  PRIMARY KEY  (`granule_id`),
  KEY `dataset_id` (`dataset_id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;

--
-- Table structure for table `parameter`
--

CREATE TABLE `parameter` (
  `parameter_id` int(10) unsigned NOT NULL auto_increment,
  `longName` varchar(120) default NULL,
  `shortName` varchar(60) default NULL,
  `description` text,
  `referenceURL` varchar(255) default NULL,
  `cellMethod` text,
  `missingDataFlag` float default NULL,
  `units` varchar(60) default NULL,
  `verticalUnits` varchar(120) default NULL,
  `database` varchar(80) NOT NULL,
  `dataset_id` int(10) unsigned NOT NULL,
  PRIMARY KEY  (`parameter_id`)
) ENGINE=MyISAM AUTO_INCREMENT=41 DEFAULT CHARSET=latin1;
