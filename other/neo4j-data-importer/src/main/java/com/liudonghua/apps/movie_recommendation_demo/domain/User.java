package com.liudonghua.apps.movie_recommendation_demo.domain;

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
public class User {

	@GraphId
	Long nodeId;

	@Indexed(unique = true)
	private long id;
	private int age;
	private String gender;
	private String occupation;
	private String zipCode;
//
//	@RelatedTo(type = "RATED")
//	@Fetch
//	private Set<Movie> movies = new HashSet<>();

	@RelatedToVia(type = "RATED")
	@Fetch
	private Set<Rating> ratings = new HashSet<>();

	@RelatedToVia(type = "SIMILARITY", direction = Direction.BOTH)
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

	public Similarity similarTo(User user, double similarityValue) {
		Similarity similarity = new Similarity(similarityValue, this, user);
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
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
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
		if (id != other.id)
			return false;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		return true;
	}

}
