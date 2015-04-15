package com.liudonghua.apps.movie_recommendation_demo;

import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.core.TypeRepresentationStrategy;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;
import org.springframework.data.neo4j.support.typerepresentation.NoopRelationshipTypeRepresentationStrategy;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@Import(RepositoryRestMvcConfiguration.class)
@EnableScheduling
@EnableAutoConfiguration
@ComponentScan(basePackages = {"com.liudonghua.apps.movie_recommendation_demo.service"})
@Configuration
@EnableNeo4jRepositories(basePackages = "com.liudonghua.apps.movie_recommendation_demo.repository")
public class ApplicationConfiguration extends Neo4jConfiguration {
	
	Log log = LogFactory.getLog(getClass());
	
    public ApplicationConfiguration() {
        setBasePackage("com.liudonghua.apps.movie_recommendation_demo.domain");
    }

    private static final String DATABASE_PATH = "resources/database_movie_recommend_neo4j.db";
    private static final String IS_EMBEDED = System.getenv("NEO4J_IS_EMBEDED") != null ? System.getenv("NEO4J_IS_EMBEDED") : "true";
    private static final String NEO4J_URL = System.getenv("NEO4J_URL") != null ? System.getenv("NEO4J_URL") : "http://localhost:7474/db/data/";
     
    @Bean
    public GraphDatabaseService graphDatabaseService() {
        if (Boolean.parseBoolean(IS_EMBEDED)) {
        	log.info("Using embed model");
            return new GraphDatabaseFactory()
                    .newEmbeddedDatabase(DATABASE_PATH);
        } else {
        	log.info("Using rest http model");
            return new SpringRestGraphDatabase(NEO4J_URL);
        }
    }

    @Override
    public TypeRepresentationStrategy<Relationship> relationshipTypeRepresentationStrategy() throws Exception {
        return new NoopRelationshipTypeRepresentationStrategy();
    }
}
