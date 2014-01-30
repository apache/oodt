#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

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

# Example of 'products' table using a 'product_id' column of type string
# Host: spawn
# Database: test_cas
# Table: 'products'
# 
#CREATE TABLE `products` (
#  `product_id` varchar(100) NOT NULL,
#  `product_structure` varchar(20) NOT NULL default '',
#  `product_type_id` int(11) NOT NULL default '0',
#  `product_name` varchar(255) NOT NULL default '',
#  `product_transfer_status` varchar(255) NOT NULL default 'TRANSFERING',
#  `product_datetime` datetime NOT NULL,
#  PRIMARY KEY  (`product_id`)
#) ENGINE=MyISAM DEFAULT CHARSET=latin1; 