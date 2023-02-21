# stackoverflow-stats

Simple service for retrieving stackoverflow tag statistics

## Prerequisites

You will need [Leiningen] 2.0.0 or above installed.

## Running

To start a web server for the application, run:

```
lein ring server
```
or

```
lein ring server-headless
```

and go to:

[http://localhost:3000/search?tag=clojure&tag=python&tag=clojurescript](http://localhost:3000/search?tag=clojure&tag=python&tag=clojurescript)

or execute from command line:

```
curl http://localhost:3000/search?tag=clojure&tag=python&tag=clojurescript
```
