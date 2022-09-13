package com.example.util;

import java.io.*;
import java.util.*;

public class Tag  implements Serializable{
	private String tagType;
    private String value;

	public Tag(String tagType, String Value){
		this.value = Value;
		this.tagType = tagType;
	}
	

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String newTag) {
        this.tagType = newTag;
    }


    public String getValue() {
        return value;
    }

    public void setTagValue(String value) {
        this.value = value;
    }

    public String toString(){
	    return tagType +": \t\t"+value;
    }

}