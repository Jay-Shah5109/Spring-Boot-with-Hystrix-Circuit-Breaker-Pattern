package com.microservices.movieinfoservice.models;

public class Movie {
	
	private String movieID;
	private String name;
	private String description;
	
	
	
	
	public Movie(String movieID, String name, String description) {
		super();
		this.movieID = movieID;
		this.name = name;
		this.description = description;
	}

	public String getMovieID() {
		return movieID;
	}
	
	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public void setMovieID(String movieID) {
		this.movieID = movieID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	

}
