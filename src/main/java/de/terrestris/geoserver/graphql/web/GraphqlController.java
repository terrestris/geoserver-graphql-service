package de.terrestris.geoserver.graphql.web;

import de.terrestris.geoserver.graphql.ConfigurationLoader;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@ControllerAdvice
@RequestMapping(
    path = {
        "/graphql"
    },
    produces = {
        MediaType.APPLICATION_JSON_VALUE
    }
)
public class GraphqlController {

    private ConfigurationLoader config;

    public void setConfig(ConfigurationLoader config) {
        this.config = config;
    }

    @GetMapping(value = "/query")
    public Map<String, Object> query(@RequestParam String query) {
        GraphQL build = GraphQL.newGraphQL(config.getGraphQLSchema()).build();
        ExecutionResult executionResult = build.execute(query);
        return executionResult.toSpecification();
    }

}
