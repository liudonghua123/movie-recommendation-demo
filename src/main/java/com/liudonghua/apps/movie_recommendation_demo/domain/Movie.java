package com.liudonghua.apps.movie_recommendation_demo.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;

@NodeEntity
public class Movie {

	@GraphId
	Long nodeId;

	@Indexed(unique = true)
	private long id;

	@Indexed
	private String title;
	private Date releaseDate;
	private String imdbUrl;

	@RelatedTo(type = "HAS_GENRE")
	@Fetch
	Set<Genre> genres = new HashSet<>();

	@RelatedToVia(type = "HAS_GENRE")
	@Fetch
	Set<GenreRel> genreRels = new HashSet<>();

	@RelatedToVia(type = "RATED", direction = Direction.INCOMING)
	@Fetch
	Set<Rating> ratings = new HashSet<>();

	public Movie() {
	}

	public Movie(long id, String title, Date releaseDate, String imdbUrl) {
		this.id = id;
		this.title = title;
		this.releaseDate = releaseDate;
		this.imdbUrl = imdbUrl;
	}

	public GenreRel hasGenre(Genre genre, double probability) {
		GenreRel genreRel = new GenreRel(probability, this, genre);
		genreRels.add(genreRel);
		return genreRel;
	}

	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getImdbUrl() {
		return imdbUrl;
	}

	public void setImdbUrl(String imdbUrl) {
		this.imdbUrl = imdbUrl;
	}

	public Set<Genre> getGenres() {
		return genres;
	}

	public void setGenres(Set<Genre> genres) {
		this.genres = genres;
	}

	public Set<GenreRel> getGenreRels() {
		return genreRels;
	}

	public void setGenreRels(Set<GenreRel> genreRels) {
		this.genreRels = genreRels;
	}

	public Set<Rating> getRatings() {
		return ratings;
	}

	public void setRatings(Set<Rating> ratings) {
		this.ratings = ratings;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((imdbUrl == null) ? 0 : imdbUrl.hashCode());
		result = prime * result
				+ ((releaseDate == null) ? 0 : releaseDate.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		Movie other = (Movie) obj;
		if (id != other.id)
			return false;
		if (imdbUrl == null) {
			if (other.imdbUrl != null)
				return false;
		} else if (!imdbUrl.equals(other.imdbUrl))
			return false;
		if (releaseDate == null) {
			if (other.releaseDate != null)
				return false;
		} else if (!releaseDate.equals(other.releaseDate))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

}
