package org.magnum.mobilecloud.video.controller;

import java.security.Principal;
import java.util.Collection;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

/**
 * This simple VideoSvc allows clients to send HTTP POST requests with
 * videos that are stored in memory using a list. Clients can send HTTP GET
 * requests to receive a JSON listing of the videos that have been sent to
 * the controller so far. Stopping the controller will cause it to lose the history of
 * videos that have been sent to it because they are stored in memory.
 * 
 * Notice how much simpler this VideoSvc is than the original VideoServlet?
 * Spring allows us to dramatically simplify our service. Another important
 * aspect of this version is that we have defined a VideoSvcApi that provides
 * strong typing on both the client and service interface to ensure that we
 * don't send the wrong paraemters, etc.
 * 
 * @author jules
 *
 */

// Tell Spring that this class is a Controller that should 
// handle certain HTTP requests for the DispatcherServlet
@Controller
public class VideoSvc {
	
	// The VideoRepository that we are going to store our videos
	// in. We don't explicitly construct a VideoRepository, but
	// instead mark this object as a dependency that needs to be
	// injected by Spring. Our Application class has a method
	// annotated with @Bean that determines what object will end
	// up being injected into this member variable.
	//
	// Also notice that we don't even need a setter for Spring to
	// do the injection.
	//
	@Autowired
	private VideoRepository repository;

	
	// Receives GET requests to /video and returns the current
	// list of videos in memory. Spring automatically converts
	// the list of videos to JSON because of the @ResponseBody
	// annotation.
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	@PreAuthorize("hasAuthority('USER')")
	public @ResponseBody Collection<Video> getVideoList(){
		return Lists.newArrayList(repository.findAll());
	}

	// Receives GET requests to /video/{id} and returns the video, if found
	// otherwise, returns a status code of 404
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method=RequestMethod.GET)
	@PreAuthorize("hasAuthority('USER')")
	public @ResponseBody Video getVideoById(@PathVariable("id") long id) throws VideoNotFoundException {
		
		Video v = repository.findOne(id);
		if (v == null) {
			throw new VideoNotFoundException();
		}
		
		return v;
	}

	// Receives POST requests to /video and converts the HTTP
	// request body, which should contain json, into a Video
	// object before adding it to the list. The @RequestBody
	// annotation on the Video parameter is what tells Spring
	// to interpret the HTTP request body as JSON and convert
	// it into a Video object to pass into the method. The
	// @ResponseBody annotation tells Spring to convert the
	// return value from the method back into JSON and put
	// it into the body of the HTTP response to the client.
	//
	// The VIDEO_SVC_PATH is set to "/video" in the VideoSvcApi
	// interface. We use this constant to ensure that the 
	// client and service paths for the VideoSvc are always
	// in synch.
	//
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		return repository.save(v);
	}

	// Receives POST requests to /video/{id}/like. 
	// The path variable {id} is used to find the video, and if successfully found,
	// the UserName is added to list of users that like this video.
	// 1 - If the video with {id}, is not found, returns an HTTP statusCode of 404.
	// 2 - If the video with {id}, is already liked by the user, returns HTTP statusCode of 400
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH  + "/{id}/like", method=RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public @ResponseBody void likeVideo(@PathVariable("id")long id, Principal p) {
		
		Video v = repository.findOne(id);
		if (v == null) {
			throw new VideoNotFoundException();
		}
		
		String userName = p.getName();
		if (!v.likeBy(userName)) {
			throw new VideoBadRequestException();
		}
		
		repository.save(v);
	}

	// Receives POST requests to /video/{id}/unlike. 
	// The path variable {id} is used to find the video, and if successfully found,
	// the UserName is removed from list of users that like this video.
	// 1 - If the video with {id}, is not found, returns an HTTP statusCode of 404.
	// 2 - If the video with {id}, has not been previously liked by the user, returns HTTP statusCode of 400
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH  + "/{id}/unlike", method=RequestMethod.POST)
	@PreAuthorize("hasAuthority('ADMIN')")
	public @ResponseBody void unlikeVideo(@PathVariable("id")long id, Principal p) {
		
		Video v = repository.findOne(id);
		if (v == null) {
			throw new VideoNotFoundException();
		}
		
		String userName = p.getName();
		if (!v.unlikeBy(userName)) {
			throw new VideoBadRequestException();
		}
		
		repository.save(v);
	}
	
	// Receives GET requests to /video/search/findByName and returns all Videos
	// that have a title (e.g., Video.name) matching the "title" request
	// parameter value that is passed by the client
	@RequestMapping(value=VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method=RequestMethod.GET)
	@PreAuthorize("hasAuthority('USER')")
	public @ResponseBody Collection<Video> findByTitle(
			// Tell Spring to use the "title" parameter in the HTTP request's query
			// string as the value for the title method parameter
			@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title
	){
		return repository.findByName(title);
	}

	// Receives GET requests to /video/search/findByDurationLessThan and returns all Videos
	// that have duration less than the request parameter value that is passed by the client
	@RequestMapping(value=VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method=RequestMethod.GET)
	@PreAuthorize("hasAuthority('USER')")
	public @ResponseBody Collection<Video> findByDurationLessThan(
			// Tell Spring to use the "title" parameter in the HTTP request's query
			@RequestParam(VideoSvcApi.DURATION_PARAMETER)long duration) {
		
		return repository.findByDurationLessThan(duration);
	}

	// Receives GET requests to /video/{id}/likedBy. 
	// The path variable {id} is used to find the video, and if successfully found,
	// the UserName is added to list of users that like this video.
	// 1 - If the video with {id}, is not found, returns an HTTP statusCode of 404.
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method=RequestMethod.GET)
	@PreAuthorize("hasAuthority('USER')")
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable("id")long id) {
		
		Video v = repository.findOne(id);
		if (v == null) {
			throw new VideoNotFoundException();
		}
		
		return v.getUsers();
	}

}
