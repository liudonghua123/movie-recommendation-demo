import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.data.neo4j.support.node.Neo4jHelper;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import com.liudonghua.apps.movie_recommendation_demo.MyNeo4jConfiguration;
import com.liudonghua.apps.movie_recommendation_demo.repository.GenreRepository;
import com.liudonghua.apps.movie_recommendation_demo.repository.MovieRepository;
import com.liudonghua.apps.movie_recommendation_demo.repository.UserRepository;
import com.liudonghua.apps.movie_recommendation_demo.repository.MovieRepository.EdgeInfo;
import com.liudonghua.apps.movie_recommendation_demo.repository.MovieRepository.NodeInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MyNeo4jConfiguration.class)
@Transactional
public class ApplicationTests {
	
	@Autowired
	private Neo4jTemplate template;
	@Autowired
	MovieRepository movieRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	GenreRepository genreRepository;
	@Autowired
	GraphDatabase graphDatabase;

	@Rollback(false)
	@BeforeTransaction
	public void cleanDb() {
		
	}

//	@Test
//	public void testGraph() {
//		List<NodeInfo> nodes = movieRepository.findAllNodes();
//		List<EdgeInfo> edges = movieRepository.findAllEdges();
//		Assert.assertEquals(27, nodes.size());
//		Assert.assertEquals(24, edges.size());
//	}
//	
//	@Test
//	public void testUserGraph() {
//        List<NodeInfo> nodes = movieRepository.findUserRelevantNodes(1);
//        List<EdgeInfo> edges = movieRepository.findUserRelevantEdges(1);
//		Assert.assertEquals(10, nodes.size());
//		Assert.assertEquals(10, edges.size());
//	}
//	
//	@Test
//	public void testMovieGraph() {
//        List<NodeInfo> nodes = movieRepository.findMovieRelevantNodes(1);
//        List<EdgeInfo> edges = movieRepository.findMovieRelevantEdges(1);
//		Assert.assertEquals(5, nodes.size());
//		Assert.assertEquals(4, edges.size());
//	}
	
//	@Test
//	public void testGetUsers() {
//		List<Map<String, String>> users = movieRepository.getAllUsers();
//		Assert.assertEquals(3, users.size());
//	}
//	
//	@Test
//	public void testGetMovies() {
//		List<Map<String, String>> users = movieRepository.getAllMovies();
//		Assert.assertEquals(5, users.size());
//	}
	
	@Test
	public void testRecommendationGraph() {
		List<Integer> userRatedMovieIds = movieRepository.findUserRatedMovieIds(1);
		List<NodeInfo> recommendationRelevantNodes = movieRepository.findRecommendationRelevantNodes(1, userRatedMovieIds);
		Assert.assertEquals(3, userRatedMovieIds.size());
		Assert.assertEquals(7, recommendationRelevantNodes.size());
	}

}
