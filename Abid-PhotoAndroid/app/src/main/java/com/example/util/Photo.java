package com.example.util;

import android.net.Uri;

import java.io.*;
import java.net.URI;
import java.util.*;



public class Photo implements Serializable {
	//private HashMap<String, String> tags;
	private ArrayList<Tag> tags;
	private String filePath;


	public Photo(ArrayList<Tag> tags, String filePath) {
		this.filePath = filePath;
		this.tags = tags;
		
	}

	public void addTag(String type, String value) {
		tags.add(new Tag(type, value));
	}

	public String getFilePath() {
		return filePath;
	}
	

	public ArrayList<Tag> getTags() {
		return tags;
	}

	public void setTags(ArrayList<Tag> tags) {
		this.tags = tags;
	}


}
