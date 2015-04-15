package com.liudonghua.apps.movie_recommendation_demo.domain;

import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;
//import org.springframework.data.rest.core.annotation.RestResource;

@RelationshipEntity(type = "SIMILARITY")
public class Similarity {

	@GraphId
	Long relationId;

	private double similarity;

	@StartNode
	//@Fetch
	//@RestResource(exported = false)
	private User user1;

	@EndNode
	//@Fetch
	//@RestResource(exported = false)
	private User user2;

	public Similarity() {
	}

	public Similarity(double similarity, User user1, User user2) {
		this.similarity = similarity;
		this.user1 = user1;
		this.user2 = user2;
	}

	public Long getRelationId() {
		return relationId;
	}

	public void setRelationId(Long relationId) {
		this.relationId = relationId;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public User getUser1() {
		return user1;
	}

	public void setUser1(User user1) {
		this.user1 = user1;
	}

	public User getUser2() {
		return user2;
	}

	public void setUser2(User user2) {
		this.user2 = user2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(similarity);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((user1 == null) ? 0 : user1.hashCode());
		result = prime * result + ((user2 == null) ? 0 : user2.hashCode());
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
		Similarity other = (Similarity) obj;
		if (Double.doubleToLongBits(similarity) != Double
				.doubleToLongBits(other.similarity))
			return false;
		if (user1 == null) {
			if (other.user1 != null)
				return false;
		} else if (!user1.equals(other.user1))
			return false;
		if (user2 == null) {
			if (other.user2 != null)
				return false;
		} else if (!user2.equals(other.user2))
			return false;
		return true;
	}

}
