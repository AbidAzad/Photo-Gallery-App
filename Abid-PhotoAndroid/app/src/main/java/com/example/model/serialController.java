package com.example.model;

import android.os.Environment;

import java.util.*;


import com.example.util.*;

import java.io.*;


public class serialController implements Serializable{

	File albumFile;
	public serialController(File a){
		albumFile = a;
	}

	public void update(ArrayList<Album> album) throws IOException{
		ObjectOutputStream a = new ObjectOutputStream(new FileOutputStream(albumFile));
		a.writeObject(album);
		a.close();
	}

	public ArrayList<Album> data() throws IOException, ClassNotFoundException{
		if(!albumFile.exists())
			albumFile.createNewFile();
		ObjectInputStream a = new ObjectInputStream(new FileInputStream(albumFile));
		ArrayList<Album> data = (ArrayList<Album>) a.readObject();
		a.close();
		return data;
	}
	
}
