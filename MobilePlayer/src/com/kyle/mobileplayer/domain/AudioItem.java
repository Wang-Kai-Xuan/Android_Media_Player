package com.kyle.mobileplayer.domain;

import java.io.Serializable;

public class AudioItem implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String title;
	private String duration;
	private String artist;
	private long size;
	private String data;
	
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDuration() {
		return duration;
	}

	@Override
	public String toString() {
		return "AudioItem [title=" + title + ", duration=" + duration
				+ ", artist=" + artist + ", size=" + size + ", data=" + data
				+ "]";
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
}
