Profile Manager - Lightweight profile manager for Balance applications
======================================================================

Overview
--------

The Profile Manager provides an interface to simplify the process of accessing and 
managing the information within the underlying directory services. The module provides 
views that make it easy to login, browse, and manage profile information for a specified 
user. It also provides a simplified way to create new profiles. However, an administrative 
user must grant the new user privileges from within the underlying directory service. The 
primary use case for this module involves authentication and authorization using LDAP, 
although it should be possible to support additional back-ends with moderate customization.


How it Works
------------

The Profile Manager has been designed so that it is easy to plug in custom back end 
directory services (LDAP, MySql, etc). The profile manager comes with a ready-to-use 
LDAP provider but customizing it is very simple. Once all the attributes in the 
config.ini file are set, the profile manager will display all attributes defined. 
These attributes will be displayed on the view.php page and can be modified through 
the manage.php page. The profile manager also provides the ability to change user 
passwords. Moreover, user groups can be viewed on the groups.php page if authorization 
configurations are setup.


Installation
------------

Copy the Profile module (this directory) to the /modules directory of
your Balance application. 


Dependencies
------------

 CAS-Single Sign On ( For LDAP users )
: If LDAP is used for authentication and/or authorization, the [CAS-SSO](https://svn.apache.org/repos/asf/oodt/tags/0.3/sso/src/main/php/pear/) 
  library must be available on the host environment to allow the module to communicate 
   with LDAP through this library.  


Configuration
-------------

All configuration for the Profile Manager takes place in the module's 'config.ini' file. 
See the inline documentation in 'config.ini' for detailed information about each
configuration option. 


Developer Guide
---------------

### Including the Profile Manager in your application

In general, the Profile Manager needs to be available on each application view. To avoid 
having to load the module at the top of each view, it is possible to include the module once,
in the Balance index.php at the root of your application. Simply add the following 
lines to the 'index.php' file where it says 'Initialize any globally required modules here':

// Profile Manager initialization
$module = App::Get()->loadModule('NAME_OF_PROFILE_DIRECTORY');


### Including the Profile Manager in your application views ### 

Before displaying the manager first you must initialize the UserStatusWidget by adding the 
following:

		// Load profile module for path access 
		$module 		= App::Get()->loadModule('profile');
		// Prepare User login details
		$authentication = App::Get()->getAuthProvider();
		$username 		= $authentication->getCurrentUsername();
		require_once( $module->modulePath . '/scripts/widgets/UserStatusWidget.php' );
		$userStatus = new UserStatusWidget(
			array( $authentication->isLoggedIn(),
				   $username,
				   // Set profile management on. 'false' will disable the link to profile management.
				   true), true );
		   
To display the user status in your application views simply add the following:

		<?php $userStatus->render();?>


