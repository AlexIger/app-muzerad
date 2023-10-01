package com.cyberia.radio.musicbrainzresponse;

import com.google.gson.annotations.SerializedName;

public class Artist{

	@SerializedName("name")
	private String name;

	@SerializedName("disambiguation")
	private String disambiguation;

	@SerializedName("id")
	private String id;

	@SerializedName("sort-name")
	private String sortName;

	public String getName(){
		return name;
	}

	public String getDisambiguation(){
		return disambiguation;
	}

	public String getId(){
		return id;
	}

	public String getSortName(){
		return sortName;
	}
}