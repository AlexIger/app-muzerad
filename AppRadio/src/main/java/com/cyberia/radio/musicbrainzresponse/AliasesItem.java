package com.cyberia.radio.musicbrainzresponse;

import com.google.gson.annotations.SerializedName;

public class AliasesItem{

	@SerializedName("begin-date")
	private Object beginDate;

	@SerializedName("end-date")
	private Object endDate;

	@SerializedName("name")
	private String name;

	@SerializedName("sort-name")
	private String sortName;

	@SerializedName("locale")
	private String locale;

	@SerializedName("type")
	private String type;

	@SerializedName("primary")
	private boolean primary;

	@SerializedName("type-id")
	private String typeId;

	public Object getBeginDate(){
		return beginDate;
	}

	public Object getEndDate(){
		return endDate;
	}

	public String getName(){
		return name;
	}

	public String getSortName(){
		return sortName;
	}

	public String getLocale(){
		return locale;
	}

	public String getType(){
		return type;
	}

	public boolean isPrimary(){
		return primary;
	}

	public String getTypeId(){
		return typeId;
	}
}