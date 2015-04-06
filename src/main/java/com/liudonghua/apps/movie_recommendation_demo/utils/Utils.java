package com.liudonghua.apps.movie_recommendation_demo.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.liudonghua.apps.movie_recommendation_demo.domain.GenreRel;
import com.liudonghua.apps.movie_recommendation_demo.domain.Movie;
import com.liudonghua.apps.movie_recommendation_demo.domain.Rating;
import com.liudonghua.apps.movie_recommendation_demo.repository.GenreRepository.UserLiking;
import com.liudonghua.apps.movie_recommendation_demo.repository.MovieRepository.EdgeInfo;
import com.liudonghua.apps.movie_recommendation_demo.repository.MovieRepository.NodeInfo;

public class Utils {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy");

	public static Date parseDate(String dateString) {
		try {
		    LocalDate localDate = LocalDate.parse(dateString, formatter);
		    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		}
		catch (DateTimeParseException ex) {
			// fix "1373|Good Morning (1971)|4-Feb-1971||http://us.imdb.com/M/title-exact?Good%20Morning%20(1971)|1|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0|0"
		    ex.printStackTrace();
		}
		return null;
	}


	public static String stringDate(Date date) {
		try {
			 return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).format(formatter);
		}
		catch (DateTimeParseException ex) {
		    ex.printStackTrace();
		}
		return null;
	}


	public static double[] generateGenreProbabilities(long size) {
		if(size == 1) {
			return new double[] {1.0};
		}
		else if(size == 2) {
			return new double[] {0.6, 0.4};
		}
		else if(size == 3) {
			return new double[] {0.5, 0.3, 0.2};
		}
		else if(size == 4) {
			return new double[] {0.4, 0.3, 0.2, 0.1};
		}
		double[] genreProbabilities = new double[(int) size];
		for(int i = 0; i < size; i++) {
			genreProbabilities[i] = 0.1;
		}
		return genreProbabilities;
	}
	
	


	public static double getThreshold(int size) {
		double[] thresholds = {1, 1.2, 1.5, 1.7, 1.9};
		return thresholds[size];
	}

	public static List<UserLiking> getCommonLiking(Set<GenreRel> genreRels,
			List<UserLiking> userLikings) {
		List<UserLiking> commonUserLikings = new ArrayList<>();
		for(UserLiking userLiking : userLikings) {
			for(GenreRel genre : genreRels) {
				if(userLiking.getGenreName().equals(genre.getGenre().getName())) {
					commonUserLikings.add(userLiking);
					break;
				}
			}
		}
		return commonUserLikings;
	}

	public static List<GenreRel> getCommonGenre(Collection<GenreRel> genreRels,
			List<UserLiking> userLikings) {
		List<GenreRel> genres = new ArrayList<>();
		for(UserLiking userLiking : userLikings) {
			for(GenreRel genre : genreRels) {
				if(userLiking.getGenreName().equals(genre.getGenre().getName())) {
					genres.add(genre);
					break;
				}
			}
		}
		return genres;
	}

	public static int[] createMovieRatingMatrix(
			List<Movie> allRelevantMovies, List<Rating> userRatings) {
		int[] movieRatingMatrix = new int[allRelevantMovies.size()];
		List<Movie> userRatedMovie = userRatings.stream().map(rating -> rating.getMovie()).collect(Collectors.toList());
		for(int i = 0; i < allRelevantMovies.size(); i++) {
			Movie movie = allRelevantMovies.get(i);
			movieRatingMatrix[i] = 0;
			for(Rating rating : userRatings) {
				if(rating.getMovie().getNodeId()==movie.getNodeId()) {
					movieRatingMatrix[i] = rating.getRate();
					break;
				}
			}
		}
		return movieRatingMatrix;
	}

	public static double calculateSimilarity(int[] movieRatingMatrix1,
			int[] movieRatingMatrix2) {
		double dotProduct = 0;
		for(int i = 0; i < movieRatingMatrix1.length; i++) {
			dotProduct += movieRatingMatrix1[i] * movieRatingMatrix2[i];
		}
		double matrixlength1 = Math.sqrt(Arrays.stream(movieRatingMatrix1).reduce(0, (a,b)->a + b * b));
		double matrixlength2 = Math.sqrt(Arrays.stream(movieRatingMatrix2).reduce(0, (a,b)->a + b * b));
		return dotProduct / (matrixlength1 * matrixlength2);
	}
	

    public static Map<String, Object> toGraphJSONFormat(List<NodeInfo> nodeInfos, List<EdgeInfo> edgeInfos) {
        List<Map<String,Object>> nodes = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> edges= new ArrayList<Map<String,Object>>();
        Map<String, Object> properties;
        for(NodeInfo nodeInfo : nodeInfos) {
        	properties = new HashMap<String,Object>();
        	properties.put("id", nodeInfo.getId());
        	properties.put("caption", nodeInfo.getCaption());
        	properties.put("type", nodeInfo.getType());
        	nodes.add(properties);
        }
        for(EdgeInfo edgeInfo : edgeInfos) {
        	properties = new HashMap<String,Object>();
        	properties.put("source", edgeInfo.getSource());
        	properties.put("target", edgeInfo.getTarget());
        	properties.put("caption", edgeInfo.getCaption());
        	properties.put("type", edgeInfo.getType());
        	edges.add(properties);
        }
        return map("nodes", nodes, "edges", edges);
    }


	public static Map<String, Object> toGraphJSONFormat(
			List<NodeInfo> nodeInfos, List<EdgeInfo> edgeInfos,
			Map<Double, Movie> recommendations) {
        List<Map<String,Object>> nodes = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> edges= new ArrayList<Map<String,Object>>();
        Map<Long, Double> recommendationMatrix = convertToRecommendationMatrix(recommendations);
        Map<String, Object> properties;
        for(NodeInfo nodeInfo : nodeInfos) {
        	properties = new HashMap<String,Object>();
        	properties.put("id", nodeInfo.getId());
        	properties.put("caption", nodeInfo.getCaption());
        	properties.put("type", nodeInfo.getType());
        	Long nodeId = new Long(nodeInfo.getId());
			if(recommendationMatrix.containsKey(nodeId)) {
				properties.put("type", "RecommendedMovie");
        		properties.put("recommendation_value", recommendationMatrix.get(nodeId));
        	}
        	nodes.add(properties);
        }
        for(EdgeInfo edgeInfo : edgeInfos) {
        	properties = new HashMap<String,Object>();
        	properties.put("source", edgeInfo.getSource());
        	properties.put("target", edgeInfo.getTarget());
        	properties.put("caption", edgeInfo.getCaption());
        	properties.put("type", edgeInfo.getType());
        	edges.add(properties);
        }
        return map("nodes", nodes, "edges", edges);
	}

    private static Map<Long, Double> convertToRecommendationMatrix(
			Map<Double, Movie> recommendations) {
    	Iterator<Entry<Double, Movie>> iterator = recommendations.entrySet().iterator();
    	Map<Long, Double> recommendationMatrix = new TreeMap<Long,Double>(Collections.reverseOrder());
    	while(iterator.hasNext()) {
    		Entry<Double, Movie> entry = iterator.next();
    		recommendationMatrix.put(entry.getValue().getNodeId(), entry.getKey());
    	}
		return recommendationMatrix;
	}


	public static Map<String, Object> map(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> result = new HashMap<String,Object>(2);
        result.put(key1,value1);
        result.put(key2,value2);
        return result;
    }
	
}
