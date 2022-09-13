package com.example.util;

import android.net.Uri;

import java.io.*;

import java.text.SimpleDateFormat;
import java.util.*;


public class Album implements Serializable{

	private String title;
//	private String dateRange;
	private ArrayList<Photo> photos;
	//TODO: Make an arraylist of photos


	public Album(String title) {
		this.title = title;
//		this.dateRange = "N/A";
		this.photos = new ArrayList<Photo>();
	}

	public String getName() {
		return title;
	}

	public void setName(String newName) {
		title = newName;
	}

	public void addPhoto(ArrayList<Tag> tags, String filePath) {
		Photo addition = new Photo(tags, filePath);
		photos.add(addition);
	}
	
	public ArrayList<Photo> getPhotos(){
		return photos;
	}

	public void setPhotos(ArrayList<Photo> gallery){ photos = gallery;}

	public int getPhotoCount(){
		return photos.size();
	}

	
	public boolean containsImage(String filepath) {
		for(Photo photo : photos) {
			if(photo.getFilePath().equals(filepath))
				return true;
		}
		return false;
	}
}
