# SURF DemoData for OOAPI

Generate OOAPI data and serve it as JSON or HTML over HTTP.

This codebase uses the
[surf-demodata](https://github.com/zeekat/surf-demodata) library to
generate entities according to a subset of
[the OOAPI specification](https://github.com/open-education-api/specification).

## Getting started
The easiest way to get started is by building the Docker image and running it:
- Get [docker](https://docs.docker.com/get-docker/).
- Clone this repo and navigate to it.
- Run `docker build . -t ooapi-server:latest`. This will build the docker image on your computer.
- Run `docker run -e SEED=42 -e PORT=8080 -e HOST=0.0.0.0 -p 8080:8080 ooapi-server:latest`

visit http://0.0.0.0:8080/?html=1 for the html version or http://0.0.0.0:8080/
for the JSON API.

## Code organization

The entity schema configuration is provided as a demo-data schema in
[resources/ooapi-schema.json](resources/ooapi-schema.json).

The schema uses a number of custom generators defined in
[src/nl/surf/demo_data_ooapi/config.clj](src/nl/surf/demo_data_ooapi/config.clj#L44),
which also contains [an export
config](src/nl/surf/demo_data_ooapi/config.clj#L118) to map entities
to web resources and link related entities.

The [resources/ooapi-population.json](resources/ooapi-population.json)
file configures how many entities of each type will be generated.

The HTTP server and middleware serving the endpoints is set up in
[src/nl/surf/demo_data_ooapi/web.clj](src/nl/surf/demo_data_ooapi/web.clj).

Also provided is [a Dockerfile](Dockerfile) for running the service
without additional local dependencies. See [Building and running
docker images](#building-and-running-docker-images)

# Local development

## Usage

- Get [leiningen](https://leiningen.org/)
- Clone this repo

```
cd surf-demodata-ooapi
PORT=8080 HOST=0.0.0.0 SEED=42 lein run
```

visit http://0.0.0.0:8080/?html=1 for the html version or http://0.0.0.0:8080/
for the JSON API.

## License

Copyright (C) 2020 SURFnet B.V.

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see http://www.gnu.org/licenses/.

