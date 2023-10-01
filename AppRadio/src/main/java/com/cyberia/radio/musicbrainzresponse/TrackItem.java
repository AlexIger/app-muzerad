package com.cyberia.radio.musicbrainzresponse;

import com.google.gson.annotations.SerializedName;

public class TrackItem{

	@SerializedName("number")
	private String number;

	@SerializedName("length")
	private int length;

	@SerializedName("id")
	private String id;

	@SerializedName("title")
	private String title;

	public String getNumber(){
		return number;
	}

	public int getLength(){
		return length;
	}

	public String getId(){
		return id;
	}

	public String getTitle(){
		return title;
	}
}