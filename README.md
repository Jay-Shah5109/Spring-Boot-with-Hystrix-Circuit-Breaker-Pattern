# Spring-Boot-with-Hystrix-Circuit-Breaker-Pattern
This project describes the use of Hystrix Circuit Breaker Pattern in Microservices 


<b>Microservices Level 2: Fault Tolerance and Resilience</b>

<i>Fault Tolerance</i>: How much a system is affected if a single MS goes down? Does the entire application gets affected or a part of application is affected? This is fault tolerance.

<i>Resilience</i>: How much faults a system can take before it can come down to its knees, meaning it can fully shutdown. This is resilience.

<b>Architecture:</b>

![image](https://user-images.githubusercontent.com/34195659/136240341-6f2e3b07-04d9-4d3a-bb58-15e72438b7d4.png)



After calling the movieDB API from the MOVIE-INFO microservice, we can see that the data is getting called when we are trying to hit the rest endpoint.

Code for retrieving the data via API call:

```
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
```


The two movie IDs are coming from the Ratings model class.

```

 public void initData(String userId) {
        this.setUserId(userId);
        this.setRatings(Arrays.asList(
                new Rating("100", 3), // new Rating(movieID, rating)
                new Rating("200", 4)
        ));
    }

```

Now, at this point of time we don’t have a even a slight of Fault Tolerant System. We need to make it Fault Tolerant.

Problems that we can face with microservices:

	• MS instance goes down
	• MS instance is slow
	• This will cause the entire application to get slow, because slowness in any one of the inter-dependent MS will cause entire application to get slow.

The way in which these problems can be addressed is - "THREADS"

Concept: What happens when a client calls the web server for a request, then the web server (for ex. Tomcat), will spin up a thread and that thread will serve the client's request, and after serving the request, the thread goes away.


At one point of time, it will happen that, all the threads will serve multiple requests and all the threads would get busy and thread pool would get exhausted. 

![image](https://user-images.githubusercontent.com/34195659/136240468-28d560e9-4cf6-4b71-978b-e61a4fb6a5f9.png)




<b>INTRODUCING THE CONCEPT OF TIMEOUTS:</b>

We can solve this problem using concept called - 'Timeout'. Now, if a thread takes time more than that of timeout, then the restTemplate will automatically close that request and give an error.

Timeouts can be used in cases where, there is a call from a microservice to an external service/MS/database. In this case, the timeouts can be for movie-info-service which calls to external DB and movie-catalog-service that calls to the 2 MS (movie-info-service and ratings-data-service)

<b>DRAWBACK OF ABOVE METHOD:</b>

The above workaround will not solve the issue permanently. This is because if we set the timeout for example to 3 seconds, then if the request are coming in at the speed of 1 request/second then too, the requests will have to wait and we will be in the same state as earlier.


We can implement the below solution where the MS will not send the request to the MS that takes time and will stop sending requests to it for a bit. Then again it will check if the timeout issues are resolved, and if yes, then it will continue sending requests to it, or else it will again ignore that MS that causes timeout issues and will check again.


![image](https://user-images.githubusercontent.com/34195659/136240517-33883e23-cc22-4399-a9e7-eb1dbcfbda8b.png)



<b>Circuit Breaker Pattern</b>


![image](https://user-images.githubusercontent.com/34195659/136240547-8e614103-3302-4fc8-a379-bde8bbe17a36.png)



Technically, we can apply the circuit breaker pattern to each and every MS in the application, that calls another MS or any external service, so that it can break the flow of communication if any time lag is there.

<b>CIRCUIT BREAKER PARAMETERS -</b>


![image](https://user-images.githubusercontent.com/34195659/136240578-64440349-a1a8-4223-a1ed-d7a9e0bd558b.png)


<b>Current Situation:</b>

![image](https://user-images.githubusercontent.com/34195659/136240602-8b2db0b5-890b-4642-b787-be18b89e630e.png)




  Answer to the above question is: <b>Fallback Mechanism. </b>

Below are the various techniques via which fallback can be done


![image](https://user-images.githubusercontent.com/34195659/136240625-3f52527c-61cc-4330-a58f-6338567ebdb6.png)



Implementing the fallback mechanism manually will involve a lot of work with threads and concurrency related stuff. Instead, there is a solution in Spring Boot called - Hystrix Circuit Breaker Pattern. Hystrix is an open source library created by Netflix. 


![image](https://user-images.githubusercontent.com/34195659/136240649-c4c89b3d-7422-41ac-a5bc-10fc84e5c8bf.png)



In our project, we are enabling the circuit breaker for the getCatalog(@PathVariable("userID") String userID) method of the MovieCatalogResource class. 

Now, when we just start the discovery server and movie-catalog-class (we don’t turn on the movie-info-service and ratings-data-service), we get the output as below. This is the fallback method that we are redirecting to in case of failures in any of the microservices.


![image](https://user-images.githubusercontent.com/34195659/136240685-8d03cc5d-33d9-4302-a499-6f55437c35c1.png)




  <b>How does the Hystrix handle the fallback mechanism? </b>

Answer to above question is that, Hystrix creates a proxy kind of class around the API class(RestController class), and it handles the circuit breaker functionality. This is depicted in the below diagram.


![image](https://user-images.githubusercontent.com/34195659/136240721-b492c70c-80d8-4111-86ca-e561ad1e86d8.png)



Now, this is not a good approach, since we will be almost providing null values to the end user as seen in some pictures above. Instead, we will introduce a fallback mechanism in ratings-data-service and movie-info-service. This will ensure that the movie-catalog-service will partly provide real values(correct value for one of the MS and null values for other MS incase corresponding MS is down). In this way, we will be including more granularity in the code for fallback mechanism.


Look the MovieCatalogResource.java for more granular code and fallback mechanisms introduced

  <b>The current scenario:</b>

Now, at the current moment, the fallback mechanism won't work since the call is not from Spring Framework to the method as in the earlier step where we got the output for fallback mechanism(see some pics above). Instead, the call is from the internal method(getCatalog(@PathVariable("userID") String userID)) to the fallback methods(getFallBackCatalogItem(Rating rating) and getFallBackUserRating(@PathVariable("userID") String userID)) this time. Look the MovieCatalogResource.java for more granular code and fallback mechanisms introduced.

  <b>GOLDEN POINT TO BE NOTED:</b>

So make a note that, when the method calls for fallback mechanisms are from methods inside a same class, then Hystrix Pattern can't be used. So instead, we will refactor the code in such a way that the calls are possible from methods and we will do it by creating more classes and placing the methods inside them. 

So,

We will create two classes for MovieInfo service and RatingsData service. The main point here is that these classes need to be Spring Bean so that Hystrix has the opportunity to create proxy as seen in previous screenshot.


Now, below is the output that we are getting when - MovieCatalogService and RatingsDataService are up and running. But the movie-info-service is still down, and hence we are getting the fallback output as seen in the below picture. This is the output for movie-catalog-service.

![image](https://user-images.githubusercontent.com/34195659/136240772-596fc81d-57ea-4d89-b624-b27bf9492b42.png)


And, below is the output that we are getting when the ratings-data-service is down, and movie-catalog-service and movie-info-service is UP. As it is seen in below picture, that it uses fallback mechanism for the ratings-data-service and takes 0 as the rating parameter.

![image](https://user-images.githubusercontent.com/34195659/136240792-221cab51-522a-485a-b39d-6331730cf773.png)



Now, we have handled this in a very good manner, where even if one of the microservice is down, still we get the fallback values for that microservice.

Some more parameters that Hystrix can take as input can be seen in below picture:

![image](https://user-images.githubusercontent.com/34195659/136240811-42e829a3-12fe-4dd3-9ea5-b7c2c512007e.png)



  <b>HYSTRIX DASHBOARD:</b>

Dependencies to be added for Hystrix Dashboard:

```
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>

```

Add below property in application.properties

```

server.port=8081
spring.application.name=movie-catalog-service
management.endpoints.web.exposure.include=hystrix.stream

```

As we can see below, we have hystrix running on localhost:8081/hystrix


![image](https://user-images.githubusercontent.com/34195659/136240901-dc8da91e-9e83-4a37-be91-f8fc668ef3e9.png)



Now, when we click on the Monitor Stream button and send the requests to the hystrix URL (Single Hystrix App) as mentioned in the picture above, we get the below output for Hystrix Dashboard with its parameters.


![image](https://user-images.githubusercontent.com/34195659/136240923-3020092e-9377-4857-ae01-7f553fab8372.png)



  <b>The Bulkhead Pattern:</b>

Till now we have used 2 ways to encounter this problem(microservices failure), first one is to introduce more and more replicas of services so that if one of the replicas fail then we can switch to other one and so on. But this was a too naïve way. Instead, we turned towards intelligent Circuit Breakers, which gave default output if anyone of the MS failed. The third one is the BulkHead Pattern.

The concept we are using here in bulkhead pattern is that we can use the separate thread pools(thread pool limit size) for each of the microservices so that the threads served for each of the microservices are different and according to the performance of the calls to the microservices.

We can see this in the below picture,


![image](https://user-images.githubusercontent.com/34195659/136240952-c96f8962-630a-47cc-b921-0bd823566777.png)




We can configure Bulkhead pattern in the code by adding below properties and annotations.


![image](https://user-images.githubusercontent.com/34195659/136240975-c4d6d0d8-6614-4467-9ed7-0c7a0be6bab4.png)




