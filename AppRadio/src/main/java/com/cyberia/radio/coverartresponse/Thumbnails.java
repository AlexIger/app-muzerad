package com.cyberia.radio.coverartresponse;

import com.google.gson.annotations.SerializedName;

public class Thumbnails{

	@SerializedName("small")
	private String small;

	@SerializedName("large")
	private String large;

	public String getSmall(){
		return small;
	}

	public String getLarge(){
		return large;
	}
}