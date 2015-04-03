package com.liudonghua.apps.movie_recommendation_demo;

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
public class MyNeo4jConfiguration extends Neo4jConfiguration {
    public MyNeo4jConfiguration() {
        setBasePackage("com.liudonghua.apps.movie_recommendation_demo.domain");
    }

    public static final String URL = System.getenv("NEO4J_URL") != null ? System.getenv("NEO4J_URL") : "http://localhost:7474/db/data/";

    @Bean
    public GraphDatabaseService graphDatabaseService() {
        return new SpringRestGraphDatabase(URL);
    }

//    // Caused by: org.neo4j.kernel.impl.storemigration.StoreUpgrader$UpgradingStoreVersionNotFoundException:
//    // 'neostore.nodestore.db' does not contain a store version, please ensure that the original database was shut down in a clean state.
//	private static final String databaseName = "resources/database_movie_recommend_neo4j.db";
//
//	@Bean(destroyMethod = "shutdown")
//	public GraphDatabaseService graphDatabaseService() {
//		return new GraphDatabaseFactory().newEmbeddedDatabase(databaseName);
//	}

    @Override
    public TypeRepresentationStrategy<Relationship> relationshipTypeRepresentationStrategy() throws Exception {
        return new NoopRelationshipTypeRepresentationStrategy();
    }
}
