package com.microservices.moviecatalogservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
@EnableHystrixDashboard // This will ensure that Hystrix dashboard will be enabled for this application
public class MovieCatalogServiceApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(MovieCatalogServiceApplication.class, args);
	}
	
	@Bean // @Bean - One and only one instance will be created and be used by multiple services
	@LoadBalanced // Load Balanced annotation is used in Service Discovery model, which indicates that does service discovery in load balanced way
	public RestTemplate getRestTemplate(){
		
		// MS Level 2: We have commented the below part of return statement, since it will not support timeouts issue
		// return new RestTemplate(); // restTemplates will be obsolete after sometime, so using the WebClient 
									// functionality listed below
		
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = 
				new HttpComponentsClientHttpRequestFactory(); // This class will allow us to create timeouts for HTTP requests and pass 
															// it to HTTP client
		clientHttpRequestFactory.setConnectTimeout(3000);// Connection timeout set
		
		return new RestTemplate(clientHttpRequestFactory); // This statement will indicate that if the request comes back within 3 seconds
														// then it is fine, else it will throw an error
	}
	
	// By writing the @LoadBalanced annotation, the rest template will get to know which URL to call, means 
	// it will start discovering services
	
	/*@Bean
	public WebClient.Builder getWebClientBuilder(){
		return WebClient.builder(); // create the instance of WebClient Builder, asynchronous feature!!
	}*/

}
