package com.cyberia.radio.musicbrainzresponse;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReleaseGroup{

	@SerializedName("primary-type-id")
	private String primaryTypeId;

	@SerializedName("primary-type")
	private String primaryType;

	@SerializedName("secondary-types")
	private List<String> secondaryTypes;

	@SerializedName("type-id")
	private String typeId;

	@SerializedName("secondary-type-ids")
	private List<String> secondaryTypeIds;

	@SerializedName("id")
	private String id;

	@SerializedName("title")
	private String title;

	public String getPrimaryTypeId(){
		return primaryTypeId;
	}

	public String getPrimaryType(){
		return primaryType;
	}

	public List<String> getSecondaryTypes(){
		return secondaryTypes;
	}

	public String getTypeId(){
		return typeId;
	}

	public List<String> getSecondaryTypeIds(){
		return secondaryTypeIds;
	}

	public String getId(){
		return id;
	}

	public String getTitle(){
		return title;
	}
}