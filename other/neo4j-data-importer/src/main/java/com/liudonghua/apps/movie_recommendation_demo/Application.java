package com.liudonghua.apps.movie_recommendation_demo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;
//import org.neo4j.kernel.impl.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.data.neo4j.support.Neo4jTemplate;

import com.liudonghua.apps.movie_recommendation_demo.domain.Genre;
import com.liudonghua.apps.movie_recommendation_demo.domain.GenreRel;
import com.liudonghua.apps.movie_recommendation_demo.domain.Movie;
import com.liudonghua.apps.movie_recommendation_demo.domain.Rating;
import com.liudonghua.apps.movie_recommendation_demo.domain.User;
import com.liudonghua.apps.movie_recommendation_demo.repository.GenreRepository;
import com.liudonghua.apps.movie_recommendation_demo.repository.GenreRepository.UserLiking;
import com.liudonghua.apps.movie_recommendation_demo.repository.MovieRepository;
import com.liudonghua.apps.movie_recommendation_demo.repository.MovieRepository.SimpleRatingInfo;
import com.liudonghua.apps.movie_recommendation_demo.repository.UserRepository;
import com.liudonghua.apps.movie_recommendation_demo.repository.UserRepository.UserPair;
import com.liudonghua.apps.movie_recommendation_demo.utils.Utils;

@Configuration
@EnableNeo4jRepositories(basePackages = "com.liudonghua.apps.movie_recommendation_demo.repository")
public class Application extends Neo4jConfiguration implements
		CommandLineRunner {
	
	private static final String RESOURCES_ROOT = "resources/ml-100k-sm";
	private Log log = LogFactory.getLog(getClass());

	public Application() {
		setBasePackage("com.liudonghua.apps.movie_recommendation_demo.domain");
	}

	private static final String databaseName = "resources/database_movie_recommend_neo4j.db";

	@Bean
	GraphDatabaseService graphDatabaseService() {
		return new GraphDatabaseFactory().newEmbeddedDatabase(databaseName);
	}

	@Autowired
	MovieRepository movieRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	GenreRepository genreRepository;
	@Autowired
	GraphDatabase graphDatabase;
	@Autowired
	Neo4jTemplate template;
	
	private Map<Integer, Genre> genreCachedMap = new HashMap<>();
	private Map<Integer, User> userCachedMap = new HashMap<>();
	private Map<Integer, Movie> movieCachedMap = new HashMap<>();

	private Map<Long, List<SimpleRatingInfo>> userRatings = new HashMap<>();

	Transaction tx;
	
	@Override
	public void run(String... arg0) {
		parseDatabase();
		createSimilarity();
	}

	private Map<Double, Movie> getRecommendations(int userId) {
		List<Movie> recommendMovies = movieRepository.findRecommendMovies(userId, 5);
		List<UserLiking> userLikings = genreRepository.findUserLikings(userId, 3);
		Map<Double, Movie> recommendCompoundedMovies = new TreeMap<>(Collections.reverseOrder());
		for(Movie movie : recommendMovies) {
			Set<GenreRel> genreRels = movie.getGenreRels();
			List<UserLiking> commonLikings = getCommonLiking(genreRels, userLikings);
			List<GenreRel> commonGenreRels = getCommonGenre(genreRels, userLikings);
			double threshold = getThreshold(commonGenreRels.size());
			double totalLiking = commonLikings.parallelStream().mapToDouble(liking -> liking.getAvgLiking()).sum();
			double totalThreshold = commonGenreRels.parallelStream().mapToDouble(genreRel -> genreRel.getProbability()).sum();
			double avgRating = movieRepository.getAvgRating((int) movie.getId());
			double recommendValue = threshold * totalLiking * totalThreshold * avgRating;
			recommendCompoundedMovies.put(recommendValue, movie);
		}
		return recommendCompoundedMovies;
	}

	private double getThreshold(int size) {
		double[] thresholds = {1, 1.2, 1.5, 1.7, 1.9};
		return thresholds[size];
	}

	private List<UserLiking> getCommonLiking(Set<GenreRel> genreRels,
			List<UserLiking> userLikings) {
		List<UserLiking> commonUserLikings = new ArrayList<>();
		for(UserLiking userLiking : userLikings) {
			for(GenreRel genre : genreRels) {
				template.fetch(genre.getGenre());
				if(userLiking.getGenreName().equals(genre.getGenre().getName())) {
					commonUserLikings.add(userLiking);
					break;
				}
			}
		}
		return commonUserLikings;
	}

	private List<GenreRel> getCommonGenre(Collection<GenreRel> genreRels,
			List<UserLiking> userLikings) {
		List<GenreRel> genres = new ArrayList<>();
		for(UserLiking userLiking : userLikings) {
			for(GenreRel genre : genreRels) {
				template.fetch(genre.getGenre());
				if(userLiking.getGenreName().equals(genre.getGenre().getName())) {
					genres.add(genre);
					break;
				}
			}
		}
		return genres;
	}

	private void createSimilarity() {
		tx = graphDatabase.beginTx();
		log.info("findRelevantUserNodeId starting ...");
		List<UserPair> relevantUserPair = userRepository.findRelevantUserNodeId();
		log.info("createSimilarity starting ...");
		int size = relevantUserPair.size();
		int currentFinished = 0;
		// 初始化所有userRating
		log.info("initializationUserRating starting ...");
		initializationUserRating(relevantUserPair);
		log.info("initializationUserRating ended ...");
		String logFormatStr = "createSimilarity %d, %f%% between %d and %d with similarity %f";
		for(UserPair userPair : relevantUserPair) {
			Long user1NodeId = userPair.getUser1NodeId();
			Long user2NodeId = userPair.getUser2NodeId();
			log.info("calculate user similarity start");
			double similarity = calculateUserSimilarity(user1NodeId, user2NodeId);
			log.info("calculate user similarity end");
			User user1 = userRepository.findOne(user1NodeId);
			User user2 = userRepository.findOne(user2NodeId);
			user1.similarTo(user2, similarity);
			log.debug("save user start");
			userRepository.save(user1);
			log.debug("save user end");
			currentFinished ++;
			log.info(String.format(logFormatStr, currentFinished, (currentFinished * 100.0 / size), user1NodeId, user2NodeId, similarity));
			if(currentFinished % 1000 == 0) {
				log.warn("commit a transaction");
				tx.success();
				tx.close();
				tx = graphDatabase.beginTx();
			}
		}
		tx.success();
		tx.close();
		log.info("createSimilarity end ...");
	}

	private void initializationUserRating(List<UserPair> relevantUserPair) {
		for(UserPair userPair : relevantUserPair) {
			long user1Id = userPair.getUser1NodeId();
			long user2Id = userPair.getUser2NodeId();
			if(!userRatings.containsKey(user1Id)) {
				List<SimpleRatingInfo> userRatings1 = movieRepository.findSimpleRatingInfoByRatingUserNodeId(user1Id);
				userRatings.put(user1Id, userRatings1);
			}
			if(!userRatings.containsKey(user2Id)) {
				List<SimpleRatingInfo> userRatings2 = movieRepository.findSimpleRatingInfoByRatingUserNodeId(user2Id);
				userRatings.put(user2Id, userRatings2);
			}
		}
	}

	private double calculateUserSimilarity(Long user1NodeId, Long user2NodeId) {
//		List<Movie> allRelevantMovies = movieRepository.findByRatingUserIds(Arrays.asList(userId1, userId2));
//		List<Rating> userRatings1 = movieRepository.findByRatingUserId(userId1);
//		List<Rating> userRatings2 = movieRepository.findByRatingUserId(userId2);
		List<Long> allRelevantMovies = movieRepository.findMovieNodeIdByRatingUserNodeIds(Arrays.asList(user1NodeId, user2NodeId));
		List<SimpleRatingInfo> userRatings1 = userRatings.get(user1NodeId);
		List<SimpleRatingInfo> userRatings2 = userRatings.get(user2NodeId);
		int[] movieRatingMatrix1 = createMovieRatingMatrix2(allRelevantMovies, userRatings1);
		int[] movieRatingMatrix2 = createMovieRatingMatrix2(allRelevantMovies, userRatings2);
		return calculateSimilarity(movieRatingMatrix1, movieRatingMatrix2);
	}

	private int[] createMovieRatingMatrix(
			List<Movie> allRelevantMovies, List<Rating> userRatings) {
		int[] movieRatingMatrix = new int[allRelevantMovies.size()];
		userRatings.stream().forEach(rating -> template.fetch(rating.getMovie()));
		List<Movie> userRatedMovie = userRatings.stream().map(rating -> rating.getMovie()).collect(Collectors.toList());
		for(int i = 0; i < allRelevantMovies.size(); i++) {
			Movie movie = allRelevantMovies.get(i);
			movieRatingMatrix[i] = 0;
			for(Rating rating : userRatings) {
				if(rating.getMovie().getNodeId().equals(movie.getNodeId())) {
					movieRatingMatrix[i] = rating.getRate();
					break;
				}
			}
		}
		return movieRatingMatrix;
	}

	private int[] createMovieRatingMatrix2(
			List<Long> allRelevantMovies, List<SimpleRatingInfo> userRatings) {
//		Map<Long, Integer> movieRatingMatrixMap = new TreeMap<>();
//		for(Long movieNodeId : allRelevantMovies) {
//			movieRatingMatrixMap.put(movieNodeId, 0);
//		}
//		for(SimpleRatingInfo rating : userRatings1) {
//			movieRatingMatrixMap.put(rating.getMovieNodeId(), rating.getRate());
//		}
//		int[] movieRatingMatrix = new int[allRelevantMovies.size()];
//		int i = 0;
//		for(int rate: movieRatingMatrixMap.values()) {
//			movieRatingMatrix[i] = rate;
//			i++;
//		}
		int[] movieRatingMatrix = new int[allRelevantMovies.size()];
		for(int i = 0; i < allRelevantMovies.size(); i++) {
			movieRatingMatrix[i] = 0;
			for(SimpleRatingInfo rating : userRatings) {
				if(rating.getMovieNodeId().equals(allRelevantMovies.get(i))) {
					movieRatingMatrix[i] = rating.getRate();
					break;
				}
			}
		}
		return movieRatingMatrix;
	}

	private double calculateSimilarity(int[] movieRatingMatrix1,
			int[] movieRatingMatrix2) {
		double dotProduct = 0;
		for(int i = 0; i < movieRatingMatrix1.length; i++) {
			dotProduct += movieRatingMatrix1[i] * movieRatingMatrix2[i];
		}
		double matrixlength1 = Math.sqrt(Arrays.stream(movieRatingMatrix1).reduce(0, (a,b)->a + b * b));
		double matrixlength2 = Math.sqrt(Arrays.stream(movieRatingMatrix2).reduce(0, (a,b)->a + b * b));
		return dotProduct / (matrixlength1 * matrixlength2);
	}

	private void parseDatabase() {
		// Parse genre file (u.genre) to insert Genre nodes
		tx = graphDatabase.beginTx();
		log.info("Parse genre file start");
		try (Stream<String> lines = Files.lines(
				Paths.get(RESOURCES_ROOT, "u.genre"),
				Charset.defaultCharset())) {
			lines.forEach(line -> processGenreEachLine(line));
		} catch (IOException e) {
			e.printStackTrace();
		}
		tx.success();
		tx.close();
		log.info("Parse genre file end");

		log.info("Parse user file start");
		tx = graphDatabase.beginTx();
		// Parse user file (u.user) to insert User nodes
		try (Stream<String> lines = Files.lines(
				Paths.get(RESOURCES_ROOT, "u.user"),
				Charset.defaultCharset())) {
			lines.forEach(line -> processUserEachLine(line));
		} catch (IOException e) {
			e.printStackTrace();
		}
		tx.success();
		tx.close();
		log.info("Parse user file end");

		log.info("Parse movie file start");
		tx = graphDatabase.beginTx();
		// Parse movie file (u.item) to insert Movie nodes and HAS_GENRE relations
		try (Stream<String> lines = Files.lines(
				Paths.get(RESOURCES_ROOT, "u.item"),
				Charset.defaultCharset())) {
			lines.forEach(line -> processMovieEachLine(line));
		} catch (IOException e) {
			e.printStackTrace();
		}
		tx.success();
		tx.close();
		log.info("Parse movie file end");

		log.info("Parse user-movie-ratings file start");
		tx = graphDatabase.beginTx();
		// Parse user-movie-ratings (u.item) file to insert ratings
		try (Stream<String> lines = Files.lines(
				Paths.get(RESOURCES_ROOT, "u.data"),
				Charset.defaultCharset())) {
			lines.forEach(line -> processRatingsEachLine(line));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//userRepository.save(userCachedMap.values());
		for(int i = 0; i < userCachedMap.size(); i++) {
			log.debug("saving updated user " + (i + 1));
			userRepository.save(userCachedMap.get(i + 1));
		}
//		User problemUser = userCachedMap.get(404);
//		userRepository.save(problemUser);
		int nodesCount = movieRepository.getNodesCount();
		int edgesCount = movieRepository.getEdgesCount();
		log.info(String.format("create total %d nodes and %d edges", nodesCount, edgesCount));
		tx.success();
		tx.close();
		log.info("Parse user-movie-ratings file end");
	}

	private void processGenreEachLine(String line) {
		if(line != null && !line.equals("")) {
			// genre name | genre id
			String[] genreParts = line.split("\\|");
			String genreName = genreParts[0];
			int genreId = Integer.parseInt(genreParts[1]);
			Genre genre = new Genre(genreId, genreName);
			Genre persistedGenre = genreRepository.save(genre);
			genreCachedMap.put(genreId, genreRepository.findOne(persistedGenre.getNodeId()));
		}
	}

	private void processUserEachLine(String line) {
		if(line != null && !line.equals("")) {
			// user id | age | gender | occupation | zip code
			String[] usereParts = line.split("\\|");
			int userId = Integer.parseInt(usereParts[0]);
			int age = Integer.parseInt(usereParts[1]);
			String gender = usereParts[2];
			String occupation = usereParts[3];
			String zipCode = usereParts[4];
			User user = new User(userId, age, gender, occupation, zipCode);
			User persistedUser = userRepository.save(user);
			userCachedMap.put(userId, userRepository.findOne(persistedUser.getNodeId()));
		}
	}

	private void processMovieEachLine(String line) {
		if(line != null && !line.equals("")) {
			// movie id | movie title | release date | video release date |
            // IMDb URL | unknown | Action | Adventure | Animation |
            // Children's | Comedy | Crime | Documentary | Drama | Fantasy |
            // Film-Noir | Horror | Musical | Mystery | Romance | Sci-Fi |
            // Thriller | War | Western |
			// 1|Toy Story (1995)|01-Jan-1995||http://us.imdb.com/M/title-exact?Toy%20Story%20(1995)|0|0|0|1|1|1|0|0|0|0|0|0|0|0|0|0|0|0|0
			String[] movieParts = line.split("\\|");
			int movieId = Integer.parseInt(movieParts[0]);
			String title = movieParts[1];
			Date releaseDate = null;
			// fix entry "267|unknown||||1|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0" in u.item
			if(!movieParts[2].equals("")) {
				releaseDate = Utils.parseDate(movieParts[2]);
			}
			String imdbUrl = movieParts[4];
			String[] genres = Arrays.copyOfRange(movieParts, 5, movieParts.length);
			// ???
//			Stream.of(
//					IntStream.range(0, genres.length)
//							.filter(i -> genres[i].equals("1")).toArray())
//					.map(i -> genreCachedMap.get(i))
//					.collect(Collectors.toSet());
			
			Movie movie = new Movie(movieId, title, releaseDate, imdbUrl);
			//movie = movieRepository.save(movie);
			// add random genre probability
			long genreCount = Stream.of(genres).filter(flag -> flag.equals("1")).count();
			double[] genreProbabilities = Utils.generateGenreProbabilities(genreCount);
			int index = 0;
			for(int i = 0; i < genres.length; i++) {
				if(genres[i].equals("1")) {
					GenreRel genreRel = movie.hasGenre(genreCachedMap.get(i), genreProbabilities[index]);
					//template.save(genreRel);
					index ++;
				}
			}
			
			Movie persistedMovie = movieRepository.save(movie);
			movieCachedMap.put(movieId, movieRepository.findOne(persistedMovie.getNodeId()));
		}
	}

	private void processRatingsEachLine(String line) {
		if(line != null && !line.equals("")) {
			// user id | item id | rating | timestamp(seconds). 
			String[] ratingParts = line.split("\\s");
			int userId = Integer.parseInt(ratingParts[0]);
			int movieId = Integer.parseInt(ratingParts[1]);
			int rate = Integer.parseInt(ratingParts[2]);
			int timeStamp = Integer.parseInt(ratingParts[3]);
			
			User user = userCachedMap.get(userId);
			Movie movie = movieCachedMap.get(movieId);
			log.debug("add user-rate-movie " + line);
			Rating rating = user.rate(movie, rate);
			//template.save(rating);
		}
	}

	public static void main(String[] args) throws Exception {
		FileUtils.deleteRecursively(new File(databaseName));
		SpringApplication.run(Application.class, args);
	}

}
