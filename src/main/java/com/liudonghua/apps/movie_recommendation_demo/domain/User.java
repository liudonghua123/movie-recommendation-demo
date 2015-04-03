package com.liudonghua.apps.movie_recommendation_demo.domain;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;
import org.springframework.data.rest.core.annotation.RestResource;

@NodeEntity
public class User {

	@GraphId
	Long nodeId;
	
	@Indexed(unique=true)
	private long id;
	private int age;
	private String gender;
	private String occupation;
	private String zipCode;
	
	@RelatedTo(type="RATED")
	@Fetch
	private Set<Movie> movies = new HashSet<>();
	
	@RelatedToVia(type="RATED")
	@Fetch
	private Set<Rating> ratings = new HashSet<>();
	
	@RelatedToVia(type="SIMILARITY", direction=Direction.BOTH)
	@Fetch
	private Set<Similarity> similarities = new HashSet<>();
	

	public User() {
	}

	public User(long id, int age, String gender, String occupation,
			String zipCode) {
		this.id = id;
		this.age = age;
		this.gender = gender;
		this.occupation = occupation;
		this.zipCode = zipCode;
	}
	
	public Rating rate(Movie movie, int rate) {
		Rating rating = new Rating(this, movie, rate);
		ratings.add(rating);
		return rating;
	}
	
	public Similarity similarTo(User user2, double similarityValue) {
		Similarity similarity = new Similarity(similarityValue, this, user2);
		similarities.add(similarity);
		return similarity;
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

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getOccupation() {
		return occupation;
	}

	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public Set<Movie> getMovies() {
		return movies;
	}

	public void setMovies(Set<Movie> movies) {
		this.movies = movies;
	}

	public Set<Rating> getRatings() {
		return ratings;
	}

	public void setRatings(Set<Rating> ratings) {
		this.ratings = ratings;
	}

	public Set<Similarity> getSimilarities() {
		return similarities;
	}

	public void setSimilarities(Set<Similarity> similarities) {
		this.similarities = similarities;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + age;
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
		result = prime * result
				+ ((occupation == null) ? 0 : occupation.hashCode());
		result = prime * result + ((zipCode == null) ? 0 : zipCode.hashCode());
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
		User other = (User) obj;
		if (age != other.age)
			return false;
		if (gender == null) {
			if (other.gender != null)
				return false;
		} else if (!gender.equals(other.gender))
			return false;
		if (id != other.id)
			return false;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		if (occupation == null) {
			if (other.occupation != null)
				return false;
		} else if (!occupation.equals(other.occupation))
			return false;
		if (zipCode == null) {
			if (other.zipCode != null)
				return false;
		} else if (!zipCode.equals(other.zipCode))
			return false;
		return true;
	}


	
}
