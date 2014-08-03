package org.magnum.dataup.controller;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.VideoFileManager;
import org.magnum.dataup.VideoSvcApi;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoRepository;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 * This VideoSvc allows clients to send HTTP POST requests with
 * videos that are stored in memory using a list. Clients can also send HTTP GET
 * requests to receive a JSON listing of the videos that have been sent to
 * the controller so far. Stopping the controller will cause it to lose the history of
 * videos that have been sent to it because they are stored in memory.
 * 
 * @author Michael Teferra
 *
 */

//Tell Spring that this class is a Controller that should 
//handle certain HTTP requests for the DispatcherServlet
@Controller
public class VideoSvc {
	
	@Autowired
	private VideoRepository repository;

	// Receives GET requests to VIDEO_SVC_PATH and returns the current
	// list of videos in memory. Spring automatically converts
	// the list of videos to JSON because of the @ResponseBody
	// annotation.
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return repository.getVideos();
	}

	// Receives POST requests to VIDEO_SVC_PATH to add a video.
	//     --The server should generate a unique identifier for the Video
	//       object and assign it to the Video by calling its setId(...)
	//       method. The returned Video JSON should include this server-generated
	//       identifier so that the client can refer to it when uploading the
	//    -- The "data URL" is the URL of the binary data for a Video
    //       (e.g., the raw MPEG data). The URL should be the _full_ URL
    //       for the video and not just the path
	//       (e.g., http://localhost:8080/video/1/data would be valid)
	@RequestMapping(value=VideoSvcApi.VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		return repository.addVideo(v);
	}

	// MPEG video data for previously added Video objects via multipart POST 
	// requests is received. The URL for the POST requests should include the ID 
	// of the Video that the data should be associated with (e.g., replace {id} in
	// the URL /video/{id}/data with a valid ID of a video, such as /video/1/data
	// -- assuming that "1" is a valid ID of a video). 
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable("id") long id, 
			                       @RequestParam("data") MultipartFile videoData) {
		
		// check if Video exists
		Video v = repository.getVideo(id);
		if (v == null) {
			throw new VideoNotFoundException();
		}
		
		// get its data.
		try {
			VideoFileManager.get().saveVideoData(v, videoData.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
			throw new VideoSaveDataException();
		}
		VideoStatus stat = new VideoStatus(VideoState.READY);
		return stat;
	}
	
	@RequestMapping(value=VideoSvcApi.VIDEO_DATA_PATH, method=RequestMethod.GET)
	public void  getData(@PathVariable("id") long id, HttpServletResponse resp) throws IOException{
		
		Video v = repository.getVideo(id);
		if (v == null) {
			throw new VideoNotFoundException();
		}
		
		// get the video data.
		try {
			VideoFileManager.get().copyVideoData(v, resp.getOutputStream());
		} catch (IOException e) {
			setError(500, "Unable to get data for Video ID=" + id, resp);
		}	
	}
	
	private void setError(int errCode, String message, HttpServletResponse resp) {
		
		try {
			resp.sendError(errCode, message);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
