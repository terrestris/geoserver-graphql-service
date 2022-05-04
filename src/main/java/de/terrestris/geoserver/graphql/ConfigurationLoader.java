package de.terrestris.geoserver.graphql;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.config.GeoServer;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.util.Classes;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

public class ConfigurationLoader {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationLoader.class);

    private GraphQLSchema graphQLSchema;

    private ConfigurationLoader(GeoServer geoServer) {
        try {
            SchemaParser schemaParser = new SchemaParser();
            List<FeatureTypeInfo> featureTypes = geoServer.getCatalog().getFeatureTypes();
            String schema = generateSchema(featureTypes);
            TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schema);

            RuntimeWiring wiring = generateWiring(featureTypes);

            SchemaGenerator schemaGenerator = new SchemaGenerator();
            graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, wiring);

            LOG.info("Successfully initialized the graphql service.");
        } catch (Exception e) {
            LOG.error("Unable to initialize graphql service: {}", e.getMessage());
            LOG.error("Stack trace:", e);
        }
    }

    private String generateSchema(List<FeatureTypeInfo> featureTypes) throws IOException {
        StringBuilder typesBuilder = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        for (FeatureTypeInfo info : featureTypes) {
            StringBuilder attBuilder = new StringBuilder();
            for (PropertyDescriptor attInfo : info.getFeatureType().getDescriptors()) {
                String type = Classes.getShortName(attInfo.getType().getBinding());
                switch (type) {
                    default:
                        LOG.warn("Unmapped type {} ", type);
                    case "Point":
                    case "LineString":
                    case "Polygon":
                    case "MultiPoint":
                    case "MultiLineString":
                    case "MultiPolygon":
                    case "Timestamp":
                    case "Geometry":
                        continue;
                    case "Integer":
                    case "Long":
                        type = "Int";
                        break;
                    case "Double":
                    case "BigDecimal":
                    case "Float":
                        type = "Float";
                        break;
                    case "String":
                        type = "String";
                        break;
                }
                attBuilder.append(attInfo.getName()).append(": ").append(type).append("\n");
            }
            if (attBuilder.length() != 0) {
                sb.append("\ntype ").append(info.getName()).append(" {\n");
                sb.append(attBuilder);
                sb.append("}\n");
                typesBuilder.append(info.getName()).append(": [").append(info.getName()).append("]\n");
            }
        }
        StringBuilder result = new StringBuilder();
        result.append("type Query {\n");
        result.append(typesBuilder);
        result.append("}\n");
        result.append(sb);
        LOG.debug("Generated graphql schema: {}", result);
        return result.toString();
    }

    private RuntimeWiring generateWiring(List<FeatureTypeInfo> featureTypes) throws IOException {
        RuntimeWiring.Builder builder = newRuntimeWiring();
        for (FeatureTypeInfo info : featureTypes) {
            String name = info.getName();
            FeatureSource<? extends FeatureType, ? extends Feature> source = info.getFeatureSource(null, null);

            builder.type("Query", b -> b.dataFetcher(name, dataFetchingEnvironment -> {
                Query query = new Query();
                query.setMaxFeatures(100); // the limit doesn't seem to be applied to the DB query itself unfortunately
                FeatureCollection<? extends FeatureType, ? extends Feature> features = source.getFeatures(query);
                List<Map<String, Object>> result = new ArrayList<>();
                FeatureIterator<? extends Feature> iterator = features.features();
                while (iterator.hasNext()) {
                    Feature feature = iterator.next();
                    Map<String, Object> map = new HashMap<>();
                    for (Property property : feature.getProperties()) {
                        map.put(property.getName().getLocalPart(), property.getValue());
                    }
                    result.add(map);
                }
                iterator.close();
                return result;
            }));
        }
        return builder.build();
    }

    public GraphQLSchema getGraphQLSchema() {
        return graphQLSchema;
    }

}
