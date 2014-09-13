# JDBC HTTP Server

[![Build Status](https://travis-ci.org/bjornharrtell/jdbc-http-server.svg)](https://travis-ci.org/bjornharrtell/jdbc-http-server)

Attempt to implement something like the proposal at http://wiki.postgresql.org/wiki/HTTP_API.

## API Usage

The API is discoverable which means you can access the root resource at /
and follow links to subresources from there but lets say you have a database
named testdb with a table named testtable in the public schema you can then 
do the following operations:

    Retrieve (GET), update (PUT) or delete (DELETE) a single row at:
    /db/testdb/schemas/public/tables/testtable/rows/1

    Retrieve (GET), update (PUT) rows or create a new row (POST) at:
    /db/testdb/schemas/public/tables/testtable/rows

The above resources accepts parameters select, where, limit, offset
and orderby where applicable. Examples:

    GET a maximum of 10 rows where cost>100 at:
    /db/testdb/schemas/public/tables/testtable/rows?where=cost>100&limit=10

The default and currently the only dataformat is JSON. POSTing or PUTing
expects a JSON object with properties corresponding to column names.

## Configuration

### Database access

By default jdbc-http-server will try to find a DataSource from a JDNI Resource named
`jdbc/db`. The name can be overriden with system property `jdbc-http-server.jdni`.

### Logging

The default logging level is INFO. Refer to [Logback](http://logback.qos.ch/) for
further configuration options.

## TODOs

* Other options for database access configuration
* Configurable on/off switch for select and where support (as they can be considered a security risk)
* Configurable on/off switch for update and deletes
* Optional raw SQL support
* Spatial support
* Security configuration allowing partial db access (similar to a firewall)

## License 

The MIT License (MIT)

Copyright (c) 2014 Björn Harrtell

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.