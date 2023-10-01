package com.cyberia.radio.coverartresponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CoverArtResponse{

	@SerializedName("images")
	private List<ImagesItem> images;

	public List<ImagesItem> getImages(){
		return images;
	}

}