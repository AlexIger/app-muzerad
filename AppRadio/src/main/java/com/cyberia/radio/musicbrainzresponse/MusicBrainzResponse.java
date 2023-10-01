package com.cyberia.radio.musicbrainzresponse;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MusicBrainzResponse{

	@SerializedName("offset")
	@Expose
	private int offset;

	@SerializedName("recordings")
	private List<RecordingsItem> recordings;

	@SerializedName("created")
	@Expose
	private String created;

	@SerializedName("count")
	@Expose
	private int count;

	public int getOffset(){
		return offset;
	}

	public List<RecordingsItem> getRecordings(){
		return recordings;
	}

	public String getCreated(){
		return created;
	}

	public int getCount(){
		return count;
	}
}