package com.liudonghua.apps.movie_recommendation_demo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.liudonghua.apps.movie_recommendation_demo.service.MovieService;

@Configuration
@Import(ApplicationConfiguration.class)
@RestController("/")
public class MovieApplication extends WebMvcConfigurerAdapter {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(MovieApplication.class, args);
	}

	@Autowired
	MovieService movieService;

	@RequestMapping("graph")
	public Map<String, Object> graph(
			@RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
		return movieService.graph(limit);
	}

	@RequestMapping("userGraph")
	public Map<String, Object> userGraph(
			@RequestParam(value = "userId", required = false, defaultValue = "1") int userId,
			@RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
		return movieService.userGraph(userId, limit);
	}

	@RequestMapping("movieGraph")
	public Map<String, Object> movieGraph(
			@RequestParam(value = "movieId", required = false, defaultValue = "1") int movieId,
			@RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
		return movieService.movieGraph(movieId, limit);
	}

	@RequestMapping("userNames")
	public List<Map<String, String>> userNames() {
		return movieService.userNames();
	}

	@RequestMapping("movieTitles")
	public List<Map<String, String>> movieTitles() {
		return movieService.movieTitles();
	}

	@RequestMapping("recommendationGraph")
	public Map<String, Object> recommendationGraph(
			@RequestParam(value = "userId", required = false, defaultValue = "1") int userId,
			@RequestParam(value = "limit", required = false, defaultValue = "3") int limit) {
		return movieService.recommendationGraph(userId, limit);
	}

}
