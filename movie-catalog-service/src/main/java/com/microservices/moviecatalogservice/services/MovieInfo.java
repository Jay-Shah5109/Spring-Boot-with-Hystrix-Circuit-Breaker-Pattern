package com.microservices.moviecatalogservice.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.microservices.moviecatalogservice.models.CatalogItem;
import com.microservices.moviecatalogservice.models.Movie;
import com.microservices.moviecatalogservice.models.Rating;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
public class MovieInfo {
	
	@Autowired
	private RestTemplate restTemplate; // use this way so that the object will be created only once
	
	@HystrixCommand(fallbackMethod="getFallBackCatalogItem")
	public CatalogItem getCatalogItem(Rating rating) {
		Movie movie=restTemplate.getForObject(
				"http://movie-info-service/movies/"+rating.getMovieID(), Movie.class);
		return new CatalogItem(movie.getName(),movie.getName(),rating.getRating());
	}
	
	//FallBack method for getCatalogItem method
	public CatalogItem getFallBackCatalogItem(Rating rating){
		
		return new CatalogItem("Movie name not found", "", rating.getRating());
	}

}
