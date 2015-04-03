package com.liudonghua.apps.movie_recommendation_demo.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import com.liudonghua.apps.movie_recommendation_demo.domain.Movie;
import com.liudonghua.apps.movie_recommendation_demo.domain.Rating;
import com.liudonghua.apps.movie_recommendation_demo.domain.User;

public interface MovieRepository extends GraphRepository<Movie> {

	@Query("match (u:User)-[:RATED]->(m:Movie) where u.id in {0} return distinct m order by m.id")
	List<Movie> findByRatingUserIds(List<Integer> ids);
	
	@Query("match (u:User)-[rating:RATED]->(m:Movie) where u.id = {0} return distinct rating")
	List<Rating> findByRatingUserId(int id);
	
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
	
    Movie findByTitle(@Param("0") String title);

    List<Movie> findByTitleContaining(@Param("0") String title);

    @Query("MATCH (m:Movie)<-[:RATED]-(a:User) RETURN m.title as movie, collect(a.id) as user LIMIT {limit}")
    List<Map<String,Object>> graph(@Param("limit") int limit);
	
}
