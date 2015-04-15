package com.liudonghua.apps.movie_recommendation_demo.repository;

import java.util.List;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryResult;
import org.springframework.data.neo4j.annotation.ResultColumn;
import org.springframework.data.neo4j.repository.GraphRepository;

import com.liudonghua.apps.movie_recommendation_demo.domain.Genre;

public interface GenreRepository extends GraphRepository<Genre> {

	@Query("match (n:User)-[:RATED]->(m:Movie) "
			+ "where n.id={0} "
			+ "with count(distinct m) as totalMovies,n "
			+ "match (n)-[:RATED]->(:Movie)-[r:HAS_GENRE]-(g:Genre) "
			+ "with g.name as genre_name, sum(r.probability)/totalMovies as avg_liking "
			+ "return genre_name, avg_liking "
			+ "order by avg_liking desc "
			+ "limit {1}")
	List<UserLiking> findUserLikings(int userid, int limit);

	@QueryResult
	public interface UserLiking {
		@ResultColumn("genre_name")
		String getGenreName();

		@ResultColumn("avg_liking")
		double getAvgLiking();
	}
}
