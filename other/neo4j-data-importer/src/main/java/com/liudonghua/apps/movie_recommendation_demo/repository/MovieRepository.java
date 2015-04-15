package com.liudonghua.apps.movie_recommendation_demo.repository;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryResult;
import org.springframework.data.neo4j.annotation.ResultColumn;
import org.springframework.data.neo4j.repository.GraphRepository;

import com.liudonghua.apps.movie_recommendation_demo.domain.Movie;
import com.liudonghua.apps.movie_recommendation_demo.domain.Rating;
import com.liudonghua.apps.movie_recommendation_demo.domain.User;

public interface MovieRepository extends GraphRepository<Movie> {

	@Query("match (u:User)-[:RATED]->(m:Movie) where u.id in {0} return distinct m order by m.id")
	List<Movie> findByRatingUserIds(List<Integer> ids);

	@Query("match (u:User)-[:RATED]->(m:Movie) where u.id in {0} return distinct id(m)")
	List<Long> findMovieNodeIdByRatingUserIds(List<Integer> ids);

	@Query("match (u:User)-[:RATED]->(m:Movie) where id(u) in {0} return distinct id(m)")
	List<Long> findMovieNodeIdByRatingUserNodeIds(List<Long> ids);
	
	@Query("match (u:User)-[rating:RATED]->(m:Movie) where u.id = {0} return rating")
	List<Rating> findByRatingUserId(int id);
	
	@Query("match (n) return count(n)")
	int getNodesCount();
	
	@Query("match ()-[r]->() return count(r)")
	int getEdgesCount();
	
	@Query("match (u:User)-[rating:RATED]->(m:Movie) where u.id = {0} return rating.rate as rate, id(endNode(rating)) as movie_node_id")
	List<SimpleRatingInfo> findSimpleRatingInfoByRatingUserId(int id);
	
	@Query("match (u:User)-[rating:RATED]->(m:Movie) where id(u) = {0} return rating.rate as rate, id(endNode(rating)) as movie_node_id")
	List<SimpleRatingInfo> findSimpleRatingInfoByRatingUserNodeId(Long id);
	
	@QueryResult
	public interface SimpleRatingInfo {
		@ResultColumn("rate")
		int getRate();

		@ResultColumn("movie_node_id")
		Long getMovieNodeId();
	}
	
	@Query("match (u:User)-[:RATED]->(um:Movie) "
			+ "where u.id={0} "
			+ "with collect(distinct um.id) as ums "
			+ "match (u)-[:RATED]->(:Movie)<-[:RATED]-(ou:User)-[:RATED]->(om:Movie) "
			+ "where not om.id in ums "
			+ "return distinct om "
			+ "limit {1}")
	List<Movie> findRecommendMovies(int userid, int limit);
	
	@Query("match (m:Movie)<-[r:RATED]-(:User) where m.id={0} return avg(r.rate)")
	double getAvgRating(int movieId);
	
}
