# SURF DemoData for OOAPI

Generates OOAPI data and serves it as JSON or HTML over HTTP.

# Local development

## Requirements

- Get [leiningen](https://leiningen.org/)
- Clone this repo
- Clone https://github.com/zeekat/surf-demodata
- Install the surf demo-data jar:

```
cd surf-demodata
lein install
```

## Usage

```
cd surf-demodata-ooapi
PORT=8080 HOST=0.0.0.0 SEED=42 lein run
```

visit http://0.0.0.0:8080/?html=1 for the html version or http://0.0.0.0:8080/
for the JSON API.

You can edit [resources/ooapi-schema.json](resources/ooapi-schema.json) to
configure the entity schema,
[resources/ooapi-population.json](resources/ooapi-population.json) configures
the amounts of entities generated.

Custom generators for OOAPI are defined in
[src/nl/surf/demo_data_ooapi/config.clj](src/nl/surf/demo_data_ooapi/config.clj)

# Building and running docker images

## Build docker image

```
docker build . -t ooapi-demo
```

## Run server from docker

```
dokcer run -eSEED=42 -ePORT=8080 -eHOST=0.0.0.0 -p8080:8080 -it ooapi-demo
```

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

