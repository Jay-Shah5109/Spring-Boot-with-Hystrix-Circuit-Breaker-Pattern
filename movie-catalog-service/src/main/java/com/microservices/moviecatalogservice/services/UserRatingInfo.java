package com.microservices.moviecatalogservice.services;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import com.microservices.moviecatalogservice.models.Rating;
import com.microservices.moviecatalogservice.models.UserRating;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@Service
public class UserRatingInfo {
	
	@Autowired
	private RestTemplate restTemplate; // use this way so that the object will be created only once
	
	@HystrixCommand(fallbackMethod="getFallBackUserRating")
	public UserRating getUserRating(@PathVariable("userID") String userID) {
		UserRating ratings=restTemplate.getForObject(
				"http://ratings-data-service/ratingsdata/user/"+userID, UserRating.class);
		return ratings;
	}
	
	
	// Fallback method for getUserRating method
	public UserRating getFallBackUserRating(@PathVariable("userID") String userID) {
		
		UserRating userRating= new UserRating();
		userRating.setUserId(userID);
		userRating.setRatings(Arrays.asList(new Rating("0",0))); // We have initialized the ratings to 0 to act as fallback values
		
		return userRating;
		
	}

}
