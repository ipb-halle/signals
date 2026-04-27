# JCrawler
Configurable multithreaded File system crawler written in Java SE.

## Features
* multi-threaded operation for increased performance
* configuration via JSON
* PostgreSQL database backend
* recording of relevant file meta data (size, uid, gid, mtime, ctime)
* configurable scan threshold to skip scanning inactive directories
* optional computation of file digests (MD5, SHA-1, SHA-256)
* store  NFS4  ACLs where available (currently, this is little brittle)

## Planned features / ToDo
* logging (currently configurable at compile time)
* make log level and log file configurable
* explanation of functionality and design assumptions and decisions
