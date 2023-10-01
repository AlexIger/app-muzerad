package com.cyberia.radio.musicbrainzresponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MediaItem{

	@SerializedName("format")
	private String format;

	@SerializedName("position")
	private int position;

	@SerializedName("track-offset")
	private int trackOffset;

	@SerializedName("track")
	private List<TrackItem> track;

	@SerializedName("track-count")
	private int trackCount;

	public String getFormat(){
		return format;
	}

	public int getPosition(){
		return position;
	}

	public int getTrackOffset(){
		return trackOffset;
	}

	public List<TrackItem> getTrack(){
		return track;
	}

	public int getTrackCount(){
		return trackCount;
	}
}