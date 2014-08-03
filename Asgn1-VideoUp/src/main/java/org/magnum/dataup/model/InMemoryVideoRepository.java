package org.magnum.dataup.model;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;

import org.magnum.dataup.VideoSvcApi;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class InMemoryVideoRepository implements VideoRepository {

	// In Memory collection of videos will be uniquely identified by its ID
	private ConcurrentHashMap<Long, Video> videoMap = new ConcurrentHashMap<>();
	
	private static AtomicLong uniqueId = new AtomicLong(0);
	
	// Add a video 
	//             - Generates a unique Id for the video and adds it  to the repository
	//             - sets the data URL as the server-name plus the video path to it.
	@Override
	public Video addVideo(Video v) {
		if (v.getId() == 0) {
			v.setId(uniqueId.incrementAndGet());
		}
		String dataURL = getDataUrl(v.getId());
		v.setDataUrl(dataURL);
		videoMap.put(v.getId(), v);
		return v;
	}

	// get list of Videos
	@Override
	public Collection<Video> getVideos() {
		return videoMap.values();
	}

	// get a video by its ID
	@Override
	public Video getVideo(long id) {
		return videoMap.get(id);
	}
	
    private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + VideoSvcApi.VIDEO_SVC_PATH + "/ " + videoId + 
                      "/" + VideoSvcApi.DATA_PARAMETER;
        return url;
    }
	
	 private String getUrlBaseForLocalServer() {
		 HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		 String base = "http://"+request.getServerName()+((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
		 return base;
	}

}
