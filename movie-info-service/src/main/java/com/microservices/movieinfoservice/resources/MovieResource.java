package com.microservices.movieinfoservice.resources;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.microservices.movieinfoservice.models.Movie;
import com.microservices.movieinfoservice.models.MovieSummary;

@RestController
@RequestMapping("/movies")
public class MovieResource {
	
	// The below commented code is from the previous project: Level 1: Microservices Communication and Service Discovery
	
	/*@RequestMapping("/{movieID}")
	public Movie getMovie(@PathVariable("movieID") String movie){
		return new Movie("Transformers","Hollywood Movie");
	}*/
	
	// Comment finished
	
	@Value("${api.key}")
	private String apiKey;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@RequestMapping("/{movieID}")
	public Movie getMovieInfo(@PathVariable("movieID") String movieID){
		
		// Below is the code to fetch the data from the API using the API key and URL
		
		MovieSummary movieSummary=restTemplate.getForObject(
				"https://api.themoviedb.org/3/movie/"+ movieID +"?api_key="+ apiKey , MovieSummary.class);	
		
		return new Movie(movieID,movieSummary.getTitle(),movieSummary.getOverview());
	}
	
	
	
	
	

}
