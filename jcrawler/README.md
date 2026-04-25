# JCrawler
Configurable multithreaded File system crawler written in Java SE.

## Features
* multi-threaded operation for increased performance
* configuration via JSON
* PostgreSQL database backend
* recording of relevant file meta data (size, uid, gid, mtime, ctime)
* configurable scan threshold to skip scanning inactive directories

## Planned features / ToDo
* store  NFS4  ACLs where available
* possibly compute configurable digest of regular files
* logging (currently configurable at compile time)
* make log level and log file configurable
* explanation of functionality and design assumptions and decisions
