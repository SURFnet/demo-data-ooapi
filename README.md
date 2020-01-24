# SURF DemoData for OOAPI

Generates OOAPI data and serves it as JSON or HTML over HTTP.

# Requirements:

   - Get [leiningen](https://leiningen.org/)
   - Install the surf demo-data jar:
```
   git clone https://github.com/zeekat/surf-demodata.git
   cd surf-demodata
   lein install
```
   - Run this repo
```
   git clone https://github.com/zeekat/surf-demodata-ooapi
   cd surf-demodata-ooapi
   PORT=8080 HOST=0.0.0.0 lein run
```
   visit http://0.0.0.0:8080/?html=1 for the html version or
   http://0.0.0.0:8080/ for the JSON API

   You can edit [resources/ooapi-schema.json] to configure the entity
   schema, [resources/ooapi-population.json] configures the amounts of
   entities generated.

   Custom generators for OOAPI are defined in
   [src/nl/surf/demo_data_ooapi/config.clj].

