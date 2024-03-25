# Property reg

The service registers new property

## The environment variables to launch the service:

`REGISTRY_` postfix means service registry

`REGISTRY_CLIENT_PORT` - Registry Client Port Number

`REGISTRY_CLIENT_USER` - Registry Client User

`REGISTRY_CLIENT_PASSWORD` - Registry Client Password

`DB_HOST` - Database host

`DB_PORT` - Database port

`DB_USER_NAME` - Database user

`DB_USER_PASSWORD` - Database user's password

## Command-line properties:

`gradle --project-prop dbname=<database_name> build` - dbname property has precedence over env variable

## JVM run options

Memory: -Xms32m -Xmx128m