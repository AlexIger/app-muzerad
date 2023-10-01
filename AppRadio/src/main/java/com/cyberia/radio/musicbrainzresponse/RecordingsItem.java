package com.cyberia.radio.musicbrainzresponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RecordingsItem{

	@SerializedName("score")
	private int score;

	@SerializedName("length")
	private int length;

	@SerializedName("artist-credit")
	private List<ArtistCreditItem> artistCredit;

	@SerializedName("id")
	private String id;

	@SerializedName("video")
	private Object video;

	@SerializedName("title")
	private String title;

	@SerializedName("releases")
	private List<ReleasesItem> releases;

	public int getScore(){
		return score;
	}

	public int getLength(){
		return length;
	}

	public List<ArtistCreditItem> getArtistCredit(){
		return artistCredit;
	}

	public String getId(){
		return id;
	}

	public Object getVideo(){
		return video;
	}

	public String getTitle(){
		return title;
	}

	public List<ReleasesItem> getReleases(){
		return releases;
	}
}