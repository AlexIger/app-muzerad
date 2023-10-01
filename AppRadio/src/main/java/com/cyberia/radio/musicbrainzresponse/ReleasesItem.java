package com.cyberia.radio.musicbrainzresponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReleasesItem{

	@SerializedName("release-group")
	private ReleaseGroup releaseGroup;

	@SerializedName("date")
	private String date;

	@SerializedName("country")
	private String country;

	@SerializedName("release-events")
	private List<ReleaseEventsItem> releaseEvents;

	@SerializedName("count")
	private int count;

	@SerializedName("artist-credit")
	private List<ArtistCreditItem> artistCredit;

	@SerializedName("id")
	private String id;

	@SerializedName("media")
	private List<MediaItem> media;

	@SerializedName("title")
	private String title;

	@SerializedName("track-count")
	private int trackCount;

	@SerializedName("status")
	private String status;

	public ReleaseGroup getReleaseGroup(){
		return releaseGroup;
	}

	public String getDate(){
		return date;
	}

	public String getCountry(){
		return country;
	}

	public List<ReleaseEventsItem> getReleaseEvents(){
		return releaseEvents;
	}

	public int getCount(){
		return count;
	}

	public List<ArtistCreditItem> getArtistCredit(){
		return artistCredit;
	}

	public String getId(){
		return id;
	}

	public List<MediaItem> getMedia(){
		return media;
	}

	public String getTitle(){
		return title;
	}

	public int getTrackCount(){
		return trackCount;
	}

	public String getStatus(){
		return status;
	}
}