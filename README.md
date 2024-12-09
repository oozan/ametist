# Ametist

> A Simple Full-Stack Clojure Application

Learn more about [Clojure project structure](https://souenzzo.com.br/creating-a-clojure-project.html).

Checkout the [live demo](https://ametist.herokuapp.com/).

This is a simple full-stack Clojure application developed to explain how to deploy a Clojure app.

---

## Table of Contents

- [Overview](#overview)
- [Development](#development)
- [Building the Project](#building-the-project)
- [Quick Commands](#quick-commands)
- [FAQ](#faq)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

Ametist consists of the following components:

- **Backend**: Located in `src/ametist/server.clj`, the backend provides JSON and HTML endpoints.
- **Frontend**: Located in `src/ametist/client.cljs`, the frontend is a React application that interacts with the JSON endpoints.
- **Build Script**: Found in `dev/ametist/build.clj`, this script compiles ClojureScript to a minified bundle and Clojure to Java classes, then generates a JAR file containing both Java class files and JavaScript static assets.
- **Dockerfile**: Utilizes the `node:alpine` image to install `npm` dependencies, then the `clojure:alpine` image to compile and create the JAR file. Finally, it creates an `openjdk:alpine` image with just the JAR file as the final product.
- **Heroku Deployment**: Configured to operate in `container` mode as described in `heroku.yml`, with GitHub integration that triggers the deploy process.

---

## Development

### Prerequisites

- **Clojure**: Install via [Homebrew](https://brew.sh/) or from the [official website](https://clojure.org/guides/getting_started).
- **Node.js**: Install via [Homebrew](https://brew.sh/) or from the [official website](https://nodejs.org/).
- **Docker**: Install via [Homebrew](https://brew.sh/) or from the [official website](https://www.docker.com/get-started).

### Setting Up

1. **Install `npm` Dependencies**
```bash
npm run ci
```

## Start PostgreSQL

Ametist requires a running PostgreSQL instance. Start one using Docker:

```bash
docker run --name my-postgres --env POSTGRES_PASSWORD=postgres --rm -p 5432:5432 postgres:alpine
```

##  Start the REPL

```bash
clj -M:dev
```

## Initialize the Backend Server

In the REPL, execute:

(doto 'ametist.server require in-ns)
(dev-main)

After a few seconds, the application will be available at http://localhost:8080.


##  Building the Project

The build script located in dev/ametist/build.clj performs the following tasks:

    Clean: Deletes the target directory.
    Compile ClojureScript: Starts the shadow-cljs server, generates a production bundle of ametist.client in target/classes/public, and stops the shadow-cljs server.
    Generate Metadata: Writes the pom file and other JAR metadata files in target/classes/META-INF.
    Compile Clojure: Compiles all Clojure namespaces found in src and all required dependencies into target/classes.
    Create Uberjar: Packages everything in target/classes into an uberjar with ametist.server as the entry point.

Building the Production JAR

To generate the production-ready JAR file:

```bash
clj -M:dev -m ametist.build
```

The resulting JAR file will be located at target/ametist.jar.


# Quick Commands


# Start a Developer REPL

# Install npm dependencies
```bash
npm run ci
```

# Start the REPL
```bash
clj -M:dev
```

# Run Tests
```bash
clojure -M:dev:test-runner
```

# Build Production JAR
```bash
clojure -M:dev -m ametist.build
```

# From REPL

# Start Development HTTP Server
```bash
(doto 'ametist.server require in-ns)
(dev-main)
```

# Run All Tests
```bash
(require 'clojure.test)
(clojure.test/run-all-tests)
```
