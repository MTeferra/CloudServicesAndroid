package org.magnum.dataup.model;

import java.util.Collection;

/**
 * An interface for a repository that can store Video
 * objects and allow them to be searched by title.
 * 
 * @author Michael Teferra
 *
 */

public interface VideoRepository {
	
	// Add a Video
	public Video addVideo(Video v);
	
	// Get the Videos that have been added so far
	public Collection<Video> getVideos();
	
	// Get a Video by ID
	public Video getVideo(long id);

}
