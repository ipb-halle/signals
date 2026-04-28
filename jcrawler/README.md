# JCrawler
Configurable multithreaded File system crawler written in Java SE.

## Features
* multi-threaded operation for increased performance
* configuration via JSON
* PostgreSQL database backend
* recording of relevant file meta data (size, uid, gid, mtime, ctime)
* configurable scan threshold to skip scanning inactive directories
* optional computation of file digests (MD5, SHA-1, SHA-256)
* logging; log level configurable at runtime
* store  NFS4  ACLs where available (currently, this is little brittle)

## Planned features / ToDo
* propagation of missing property to files in missing directories
* test coverage
* real implementation for Windows ACLs (possibly Posix ACLs too?)
* explanation of functionality and design assumptions and decisions
