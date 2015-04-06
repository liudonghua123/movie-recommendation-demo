package com.liudonghua.apps.movie_recommendation_demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.liudonghua.apps.movie_recommendation_demo.domain.GenreRel;
import com.liudonghua.apps.movie_recommendation_demo.domain.Movie;
import com.liudonghua.apps.movie_recommendation_demo.domain.Rating;
import com.liudonghua.apps.movie_recommendation_demo.domain.User;
import com.liudonghua.apps.movie_recommendation_demo.repository.GenreRepository.UserLiking;
import com.liudonghua.apps.movie_recommendation_demo.repository.GenreRepository;
import com.liudonghua.apps.movie_recommendation_demo.repository.MovieRepository;
import com.liudonghua.apps.movie_recommendation_demo.repository.UserRepository;
import com.liudonghua.apps.movie_recommendation_demo.repository.MovieRepository.EdgeInfo;
import com.liudonghua.apps.movie_recommendation_demo.repository.MovieRepository.NodeInfo;
import com.liudonghua.apps.movie_recommendation_demo.repository.UserRepository.UserPair;
import com.liudonghua.apps.movie_recommendation_demo.utils.Utils;

@Service
@Transactional
public class MovieService {

    @Autowired 
    MovieRepository movieRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	GenreRepository genreRepository;

    @Autowired 
    Neo4jTemplate template;

    public Map<String, Object> graph(int limit) {
        List<NodeInfo> nodeInfos = movieRepository.findAllNodes();
        List<EdgeInfo> edgeInfos = movieRepository.findAllEdges();
        return Utils.toGraphJSONFormat(nodeInfos, edgeInfos);
    }

	public Map<String, Object> userGraph(int userId, int limit) {
        List<NodeInfo> nodeInfos = movieRepository.findUserRelevantNodes(userId);
        List<EdgeInfo> edgeInfos = movieRepository.findUserRelevantEdges(userId);
        return Utils.toGraphJSONFormat(nodeInfos, edgeInfos);
	}

	public Map<String, Object> movieGraph(int movieId, int limit) {
        List<NodeInfo> nodeInfos = movieRepository.findMovieRelevantNodes(movieId);
        List<EdgeInfo> edgeInfos = movieRepository.findMovieRelevantEdges(movieId);
        return Utils.toGraphJSONFormat(nodeInfos, edgeInfos);
	}

	public List<Map<String, String>> userNames() {
        return movieRepository.getAllUsers();
	}

	public List<Map<String, String>> movieTitles() {
        return movieRepository.getAllMovies();
	}
	
	public Map<String, Object> recommendationGraph(int userId, int limit) {
        List<Integer> userRatedMovieIds = movieRepository.findUserRatedMovieIds(userId);
        Map<Double, Movie> recommendations = getRecommendations(userId, limit);
        
        List<NodeInfo> nodeInfos = movieRepository.findRecommendationRelevantNodes(userId, userRatedMovieIds);
        List<EdgeInfo> edgeInfos = movieRepository.findRecommendationRelevantEdges(userId, userRatedMovieIds);
        
        return Utils.toGraphJSONFormat(nodeInfos, edgeInfos, recommendations);
	}

	private Map<Double, Movie> getRecommendations(int userId, int size) {
		int candidateSize = 100;
		List<Movie> recommendMovies = movieRepository.findRecommendMovies(userId, candidateSize);
		List<UserLiking> userLikings = genreRepository.findUserLikings(userId, 3);
		Map<Double, Movie> recommendCandidateCompoundedMovies = new TreeMap<>(Collections.reverseOrder());
		for(Movie movie : recommendMovies) {
			Set<GenreRel> genreRels = movie.getGenreRels();
			List<UserLiking> commonLikings = Utils.getCommonLiking(genreRels, userLikings);
			List<GenreRel> commonGenreRels = Utils.getCommonGenre(genreRels, userLikings);
			double threshold = Utils.getThreshold(commonGenreRels.size());
			double totalLiking = commonLikings.parallelStream().mapToDouble(liking -> liking.getAvgLiking()).sum();
			double totalThreshold = commonGenreRels.parallelStream().mapToDouble(genreRel -> genreRel.getProbability()).sum();
			double avgRating = movieRepository.getAvgRating((int) movie.getId());
			double recommendValue = threshold * totalLiking * totalThreshold * avgRating;
			recommendCandidateCompoundedMovies.put(recommendValue, movie);
		}
		Map<Double, Movie> recommendCompoundedMovies = new TreeMap<>(Collections.reverseOrder());
		Iterator<Entry<Double, Movie>> iterator = recommendCandidateCompoundedMovies.entrySet().iterator();
		while(iterator.hasNext() && size-- > 0) {
			Entry<Double, Movie> entry = iterator.next();
			recommendCompoundedMovies.put(entry.getKey(), entry.getValue());
		}
		return recommendCompoundedMovies;
	}

	private void createSimilarity() {
		List<UserPair> relevantUserPair = userRepository.findRelevantUserPair();
		for(UserPair userPair : relevantUserPair) {
			int user1Id = userPair.getUser1Id();
			int user2Id = userPair.getUser2Id();
			double similarity = calculateUserSimilarity(user1Id, user2Id);
			User user1 = userRepository.findBySchemaPropertyValue("id", user1Id);
			User user2 = userRepository.findBySchemaPropertyValue("id", user2Id);
			user1.similarTo(user2, similarity);
			User savedUser1 = userRepository.save(user1);
		}
	}

	private double calculateUserSimilarity(int userId1, int userId2) {
		List<Movie> allRelevantMovies = movieRepository.findByRatingUserIds(Arrays.asList(userId1, userId2));
		List<Rating> userRatings1 = movieRepository.findByRatingUserId(userId1);
		List<Rating> userRatings2 = movieRepository.findByRatingUserId(userId2);
		int[] movieRatingMatrix1 = Utils.createMovieRatingMatrix(allRelevantMovies, userRatings1);
		int[] movieRatingMatrix2 = Utils.createMovieRatingMatrix(allRelevantMovies, userRatings2);
		return Utils.calculateSimilarity(movieRatingMatrix1, movieRatingMatrix2);
	}
	


}
