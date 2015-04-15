package com.liudonghua.apps.movie_recommendation_demo.repository;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryResult;
import org.springframework.data.neo4j.annotation.ResultColumn;
import org.springframework.data.neo4j.repository.GraphRepository;

import com.liudonghua.apps.movie_recommendation_demo.domain.User;

public interface UserRepository extends GraphRepository<User> {

	@Query("match (u:User)-[:RATED]->(:Movie)<-[:RATED]-(others:User) where u.id={0} return others")
	List<User> findRelevantUser(int userId);

	@Query("match (u1:User)-[:RATED]->(:Movie)<-[:RATED]-(u2:User) where u1.id < u2.id return distinct u1.id as u1_id, u2.id as u2_id")
	List<UserPair> findRelevantUser();

	@Query("match (u1:User)-[:RATED]->(:Movie)<-[:RATED]-(u2:User) where u1.id < u2.id return distinct id(u1) as u1_node_id, id(u2) as u2_node_id")
	List<UserPair> findRelevantUserNodeId();

	@QueryResult
	public interface UserPair {
		@ResultColumn("u1_node_id")
		long getUser1NodeId();

		@ResultColumn("u2_node_id")
		long getUser2NodeId();
	}
}
