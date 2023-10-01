package com.cyberia.radio.musicbrainzresponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Area{

	@SerializedName("iso-3166-1-codes")
	private List<String> iso31661Codes;

	@SerializedName("name")
	private String name;

	@SerializedName("id")
	private String id;

	@SerializedName("sort-name")
	private String sortName;

	public List<String> getIso31661Codes(){
		return iso31661Codes;
	}

	public String getName(){
		return name;
	}

	public String getId(){
		return id;
	}

	public String getSortName(){
		return sortName;
	}
}