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
* store  NFS4  ACLs where available (may not be 100% complete)
* support for Windows ACLs (work in progress)

## Planned features / ToDo
* test idempotency and improved test coverage
* less parallel DB connections
* Possibly support for posix ACLs (attributes 'system.posix_acl_default' and 'system.posix_acl_access')
* explanation of functionality and of design assumptions and decisions
