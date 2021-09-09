## GeoServer GraphQL service

This plugin adds a GraphQL endpoint and generates a GraphQL schema for all feature types.

It also adds the graphiql client to the publishing tab when editing layers.

To install, add the jar to the GeoServer jar (additional libs folder or `WEB-INF/lib` depending on your installation).
You'll also have to make sure to add the jackson-databind and jackson-annotation jars in the correct version. For
current versions these are [jackson-databind](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.10.5/jackson-databind-2.10.5.jar)
and [jackson-annotations](https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.10.5/jackson-annotations-2.10.5.jar).
