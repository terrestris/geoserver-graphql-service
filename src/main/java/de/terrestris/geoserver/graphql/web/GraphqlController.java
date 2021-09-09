package de.terrestris.geoserver.graphql.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.terrestris.geoserver.graphql.ConfigurationLoader;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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

    @Autowired
    private HttpServletRequest request;

    private ConfigurationLoader config;

    public void setConfig(ConfigurationLoader config) {
        this.config = config;
    }

    @GetMapping
    public Map<String, Object> query(@RequestParam String query) {
        GraphQL build = GraphQL.newGraphQL(config.getGraphQLSchema()).build();
        ExecutionResult executionResult = build.execute(query);
        return executionResult.toSpecification();
    }

    @PostMapping
    public Map<String, Object> schema() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        GraphQL build = GraphQL.newGraphQL(config.getGraphQLSchema()).build();
        JsonNode node = mapper.readTree(request.getInputStream());
        String actualQuery = node.get("query").asText();
        return build.execute(actualQuery).toSpecification();
    }

}
