package com.liudonghua.apps.movie_recommendation_demo.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryResult;
import org.springframework.data.neo4j.annotation.ResultColumn;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.repository.query.Param;

import com.liudonghua.apps.movie_recommendation_demo.domain.Movie;
import com.liudonghua.apps.movie_recommendation_demo.domain.Rating;

public interface MovieRepository extends GraphRepository<Movie> {

	@Query("match (u:User)-[:RATED]->(m:Movie) where u.id in {0} return distinct m order by m.id")
	List<Movie> findByRatingUserIds(List<Integer> ids);

	@Query("match (u:User)-[rating:RATED]->(m:Movie) where u.id = {0} return distinct rating")
	List<Rating> findByRatingUserId(int id);

	@Query("match (u:User)-[:RATED]->(um:Movie) "
			+ "where u.id={0} "
			+ "with collect(distinct um.id) as ums "
			+ "match (u)-[:RATED]->(:Movie)<-[:RATED]-(ou:User)-[:RATED]->(om:Movie) "
			+ "where not om.id in ums " + "return distinct om " + "limit {1}")
	List<Movie> findRecommendMovies(int userid, int limit);

	@Query("match (m:Movie)<-[r:RATED]-(:User) where m.id={0} return avg(r.rate)")
	double getAvgRating(int movieId);

	Movie findByTitle(@Param("0") String title);

	List<Movie> findByTitleContaining(@Param("0") String title);

	@Query("MATCH (m:Movie)<-[:RATED]-(a:User) RETURN m.title as movie, collect(a.id) as user LIMIT {limit}")
	List<Map<String, Object>> graph(@Param("limit") int limit);
	
	@Query("match (u:User)-[:RATED]->(m:Movie) where u.id={0} return distinct m.id")
	List<Integer> findUserRatedMovieIds(int userId);
	
	@Query("match (n) return id(n) as id, case labels(n)[0] when 'Genre' then n.name when 'User' then n.id when 'Movie' then n.title end as caption, labels(n)[0] as type")
	List<NodeInfo> findAllNodes();
	
	@Query("match (m)-[r]->(n) return id(m) as source,type(r) as type, id(n) as target, case type(r) when 'HAS_GENRE' then r.probability when 'SIMILARITY' then r.similarity when 'RATED' then r.rate end as caption")
	List<EdgeInfo> findAllEdges();
	
	@Query("match (u:User)-[r:RATED]->(m:Movie)-[hg:HAS_GENRE]->(g:Genre) where u.id={0} return  id(u) as id, case labels(u)[0] when 'Genre' then u.name when 'User' then u.id when 'Movie' then u.title end as caption, labels(u)[0] as type "
			+ "union "
			+ "match (u:User)-[r:RATED]->(m:Movie)-[hg:HAS_GENRE]->(g:Genre) where u.id={0} return id(m) as id, case labels(m)[0] when 'Genre' then m.name when 'User' then m.id when 'Movie' then m.title end as caption, labels(m)[0] as type "
			+ "union "
			+ "match (u:User)-[r:RATED]->(m:Movie)-[hg:HAS_GENRE]->(g:Genre) where u.id={0} return id(g) as id, case labels(g)[0] when 'Genre' then g.name when 'User' then g.id when 'Movie' then g.title end as caption, labels(g)[0] as type")
	List<NodeInfo> findUserRelevantNodes(int userId);
	
	@Query("match (u:User)-[r:RATED]->(m:Movie)-[hg:HAS_GENRE]->(g:Genre) where u.id={0} return id(u) as source,type(r) as type, id(m) as target, case type(r) when 'HAS_GENRE' then r.probability when 'SIMILARITY' then r.similarity when 'RATED' then r.rate end as caption "
			+ "union "
			+ "match (u:User)-[r:RATED]->(m:Movie)-[hg:HAS_GENRE]->(g:Genre) where u.id={0} return id(m) as source,type(hg) as type, id(g) as target, case type(hg) when 'HAS_GENRE' then r.probability when 'SIMILARITY' then r.similarity when 'RATED' then r.rate end as caption")
	List<EdgeInfo> findUserRelevantEdges(int userId);

	@Query("match (u:User)-[r:RATED]->(m:Movie)-[hg:HAS_GENRE]->(g:Genre) where m.id={0} return  id(u) as id, case labels(u)[0] when 'Genre' then u.name when 'User' then u.id when 'Movie' then u.title end as caption, labels(u)[0] as type "
			+ "union "
			+ "match (u:User)-[r:RATED]->(m:Movie)-[hg:HAS_GENRE]->(g:Genre) where m.id={0} return id(m) as id, case labels(m)[0] when 'Genre' then m.name when 'User' then m.id when 'Movie' then m.title end as caption, labels(m)[0] as type "
			+ "union "
			+ "match (u:User)-[r:RATED]->(m:Movie)-[hg:HAS_GENRE]->(g:Genre) where m.id={0} return id(g) as id, case labels(g)[0] when 'Genre' then g.name when 'User' then g.id when 'Movie' then g.title end as caption, labels(g)[0] as type")
	List<NodeInfo> findMovieRelevantNodes(int movieId);

	@Query("match (u:User)-[r:RATED]->(m:Movie)-[hg:HAS_GENRE]->(g:Genre) where m.id={0} return id(u) as source,type(r) as type, id(m) as target, case type(r) when 'HAS_GENRE' then r.probability when 'SIMILARITY' then r.similarity when 'RATED' then r.rate end as caption "
			+ "union "
			+ "match (u:User)-[r:RATED]->(m:Movie)-[hg:HAS_GENRE]->(g:Genre) where m.id={0} return id(m) as source,type(hg) as type, id(g) as target, case type(hg) when 'HAS_GENRE' then r.probability when 'SIMILARITY' then r.similarity when 'RATED' then r.rate end as caption")
	List<EdgeInfo> findMovieRelevantEdges(int movieId);
	
	@Query("match (n:User) return n.id as id, n.id as caption")
	List<Map<String, String>> getAllUsers();
	
	@Query("match (n:Movie) return n.id as id, n.title as caption")
	List<Map<String, String>> getAllMovies();
	
	@Query("match (u:User)-[r1:RATED]->(m:Movie)<-[r2:RATED]-(ou:User)-[:RATED]->(om:Movie), (u)-[s:SIMILARITY]-(ou) where u.id={0} and not om.id in {1} return id(u) as id, case labels(u)[0] when 'Genre' then u.name when 'User' then u.id when 'Movie' then u.title end as caption, labels(u)[0] as type "
			+ "union "
			+ "match (u:User)-[r1:RATED]->(m:Movie)<-[r2:RATED]-(ou:User)-[:RATED]->(om:Movie), (u)-[s:SIMILARITY]-(ou) where u.id={0} and not om.id in {1} return id(m) as id, case labels(m)[0] when 'Genre' then m.name when 'User' then m.id when 'Movie' then m.title end as caption, labels(m)[0] as type "
			+ "union "
			+ "match (u:User)-[r1:RATED]->(m:Movie)<-[r2:RATED]-(ou:User)-[:RATED]->(om:Movie), (u)-[s:SIMILARITY]-(ou) where u.id={0} and not om.id in {1} return id(ou) as id, case labels(ou)[0] when 'Genre' then ou.name when 'User' then ou.id when 'Movie' then ou.title end as caption, labels(ou)[0] as type "
			+ "union "
			+ "match (u:User)-[r1:RATED]->(m:Movie)<-[r2:RATED]-(ou:User)-[:RATED]->(om:Movie), (u)-[s:SIMILARITY]-(ou) where u.id={0} and not om.id in {1} return id(om) as id, case labels(om)[0] when 'Genre' then om.name when 'User' then om.id when 'Movie' then om.title end as caption, labels(om)[0] as type")
	List<NodeInfo> findRecommendationRelevantNodes(int userId, List<Integer> alreadyRatedMovieIds);
	
	@Query("match (u:User)-[r1:RATED]->(m:Movie)<-[r2:RATED]-(ou:User)-[r3:RATED]->(om:Movie), (u)-[s:SIMILARITY]-(ou) where u.id={0} and not om.id in {1} return id(u) as source,type(r1) as type, id(m) as target, case type(r1) when 'HAS_GENRE' then r1.probability when 'SIMILARITY' then  r1.similarity when 'RATED' then r1.rate end as caption "
			+ "union "
			+ "match (u:User)-[r1:RATED]->(m:Movie)<-[r2:RATED]-(ou:User)-[r3:RATED]->(om:Movie), (u)-[s:SIMILARITY]-(ou) where u.id={0} and not om.id in {1} return id(ou) as source,type(r2) as type, id(m) as target, case type(r2) when 'HAS_GENRE' then r2.probability when 'SIMILARITY' then  r2.similarity when 'RATED' then r2.rate end as caption "
			+ "union "
			+ "match (u:User)-[r1:RATED]->(m:Movie)<-[r2:RATED]-(ou:User)-[r3:RATED]->(om:Movie), (u)-[s:SIMILARITY]-(ou) where u.id={0} and not om.id in {1} return id(ou) as source,type(r3) as type, id(om) as target, case type(r3) when 'HAS_GENRE' then r3.probability when 'SIMILARITY' then  r3.similarity when 'RATED' then r3.rate end as caption "
			+ "union "
			+ "match (u:User)-[r1:RATED]->(m:Movie)<-[r2:RATED]-(ou:User)-[r3:RATED]->(om:Movie), (u)-[s:SIMILARITY]-(ou) where u.id={0} and not om.id in {1} return id(u) as source,type(s) as type, id(ou) as target, case type(s) when 'HAS_GENRE' then s.probability when 'SIMILARITY' then  s.similarity when 'RATED' then s.rate end as caption")
	List<EdgeInfo> findRecommendationRelevantEdges(int userId, List<Integer> alreadyRatedMovieIds);
	
	@QueryResult
	public interface NodeInfo {
		@ResultColumn("id")
		int getId();

		@ResultColumn("caption")
		String getCaption();

		@ResultColumn("type")
		String getType();
	}
	
	@QueryResult
	public interface EdgeInfo {
		@ResultColumn("source")
		int getSource();

		@ResultColumn("type")
		String getType();
		
		@ResultColumn("target")
		int getTarget();

		@ResultColumn("caption")
		String getCaption();
	}

}
