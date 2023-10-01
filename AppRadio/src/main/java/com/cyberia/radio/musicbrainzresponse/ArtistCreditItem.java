package com.cyberia.radio.musicbrainzresponse;

import com.google.gson.annotations.SerializedName;

public class ArtistCreditItem{

	@SerializedName("artist")
	private Artist artist;

	@SerializedName("name")
	private String name;

	public Artist getArtist(){
		return artist;
	}

	public String getName(){
		return name;
	}
}