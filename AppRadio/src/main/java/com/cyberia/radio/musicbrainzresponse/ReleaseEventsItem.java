package com.cyberia.radio.musicbrainzresponse;

import com.google.gson.annotations.SerializedName;

public class ReleaseEventsItem{

	@SerializedName("date")
	private String date;

	@SerializedName("area")
	private Area area;

	public String getDate(){
		return date;
	}

	public Area getArea(){
		return area;
	}
}