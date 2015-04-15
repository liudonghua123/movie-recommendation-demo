package com.liudonghua.apps.movie_recommendation_demo.domain;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;
//import org.springframework.data.rest.core.annotation.RestResource;

@RelationshipEntity(type="HAS_GENRE")
public class GenreRel {

	@GraphId
	Long relationId;

	private double probability;

	@StartNode
	//@Fetch
	// Could not write content: Detected multiple association links with same relation type! Disambiguate association class com.liudonghua.apps.movie_recommendation_demo.domain.Movie movie rel: true idx: false using @RestResource!
	// see GenreRel
	//@RestResource(exported=false, rel="genre_movie")
	private Movie movie;
	
	@EndNode
	//@Fetch
	private Genre genre;

	public GenreRel() {
	}

	public GenreRel(double probability, Movie movie, Genre genre) {
		super();
		this.probability = probability;
		this.movie = movie;
		this.genre = genre;
	}

	public Long getRelationId() {
		return relationId;
	}

	public void setRelationId(Long relationId) {
		this.relationId = relationId;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public Movie getMovie() {
		return movie;
	}

	public void setMovie(Movie movie) {
		this.movie = movie;
	}

	public Genre getGenre() {
		return genre;
	}

	public void setGenre(Genre genre) {
		this.genre = genre;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((genre == null) ? 0 : genre.hashCode());
		result = prime * result + ((movie == null) ? 0 : movie.hashCode());
		long temp;
		temp = Double.doubleToLongBits(probability);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenreRel other = (GenreRel) obj;
		if (genre == null) {
			if (other.genre != null)
				return false;
		} else if (!genre.equals(other.genre))
			return false;
		if (movie == null) {
			if (other.movie != null)
				return false;
		} else if (!movie.equals(other.movie))
			return false;
		if (Double.doubleToLongBits(probability) != Double
				.doubleToLongBits(other.probability))
			return false;
		return true;
	}

}
