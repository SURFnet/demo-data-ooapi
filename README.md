# SURF DemoData for OOAPI

Generates OOAPI data and serves it as JSON or HTML over HTTP.

# Requirements:

   - Get [leiningen](https://leiningen.org/)
   - Clone this repo
   - Clone https://github.com/zeekat/surf-demodata
   - Install the surf demo-data jar:
```
   cd surf-demodata
   lein install
```
   - Run this repo
```
   cd surf-demodata-ooapi
   PORT=8080 HOST=0.0.0.0 SEED=42 lein run
```
   visit http://0.0.0.0:8080/?html=1 for the html version or
   http://0.0.0.0:8080/ for the JSON API

   You can edit [resources/ooapi-schema.json] to configure the entity
   schema, [resources/ooapi-population.json] configures the amounts of
   entities generated.

   Custom generators for OOAPI are defined in
   [src/nl/surf/demo_data_ooapi/config.clj].
