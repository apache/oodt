# Connection: localhost
# Host: spawn
# Saved: 2006-01-02 18:12:06
# 

# Host: spawn
# Database: test_cas
# Table: 'elements'
# 
CREATE TABLE `elements` (
  `element_id` int(11) NOT NULL auto_increment,
  `element_name` varchar(255) NOT NULL default '',
  `data_type_id` int(11) NOT NULL default '0',
  `dc_element` varchar(100) default '',
  `element_description` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`element_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1; 

# Host: spawn
# Database: test_cas
# Table: 'product_type_element_map'
# 
CREATE TABLE `product_type_element_map` (
  `product_type_element_map_id` int(11) NOT NULL auto_increment,
  `product_type_id` int(11) NOT NULL default '0',
  `element_id` int(11) NOT NULL default '0',
  PRIMARY KEY  (`product_type_element_map_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1; 

# Host: spawn
# Database: test_cas
# Table: 'product_types'
# 
CREATE TABLE `product_types` (
  `product_type_id` int(11) NOT NULL auto_increment,
  `product_type_name` varchar(255) NOT NULL default '',
  `product_type_description` varchar(255) NOT NULL default '',
  `product_type_versioner_class` varchar(255) default '',
  `product_type_repository_path` varchar(255) NOT NULL default '',
  PRIMARY KEY  (`product_type_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1; 

# Host: spawn
# Database: test_cas
# Table: 'products'
# 
CREATE TABLE `products` (
  `product_id` int(11) NOT NULL auto_increment,
  `product_structure` varchar(20) NOT NULL default '',
  `product_type_id` int(11) NOT NULL default '0',
  `product_name` varchar(255) NOT NULL default '',
  `product_transfer_status` varchar(255) NOT NULL default 'TRANSFERING',
  PRIMARY KEY  (`product_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1; 

