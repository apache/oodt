---
layout: post
title: Apache OODT 0.12 hits the shelves
description: Apache OODT 0.12 hits the shelves
---

Ok so, once again, I’m using this blog as an announcement platform because, well over at Apache OODT we don’t really have one… really need to fix that. Anyway, with my completely unofficial news reporter hat on once more, Apache OODT 0.12 has been released to the world.

For those of you who don’t know what Apache OODT is, it is an open source Apache Software Foundation run data management platform originally developed and still used by NASA.

This is a bug fix release mostly for those people who love to use Solr with Apache OODT. Thanks to some great bug reports from people testing Apache OODT with Solr we managed to track down and fix a number of what seem to be long-standing Solr support issues.

The change log is available here, but as its quite short here’s what was fixed:

* OODT-781 Add missing XMLRPC endpoint for remove hashtable.

* OODT-923 Fix start script for solr deployments

* OODT-924 Fix missing timestamp metadata field in returned data from solr

* OODT-992 Fix incorrect product count for solr backed store

As ever, as an open source project it depends on volunteers to make it happen. If you are interested in working on some cool technology and chat to a lot of friendly PhD folk (not me), then swing by the mailing list and say hello!
