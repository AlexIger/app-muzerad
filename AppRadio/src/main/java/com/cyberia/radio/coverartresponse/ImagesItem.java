package com.cyberia.radio.coverartresponse;

import com.google.gson.annotations.SerializedName;

public class ImagesItem{

	@SerializedName("image")
	private String image;

	@SerializedName("thumbnails")
	private Thumbnails thumbnails;

	public Thumbnails getThumbnails(){
		return thumbnails;
	}
}