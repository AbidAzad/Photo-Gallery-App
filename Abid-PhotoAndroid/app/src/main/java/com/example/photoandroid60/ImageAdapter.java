package com.example.photoandroid60;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.util.Photo;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    ArrayList<Photo> albumPhotos = new ArrayList<Photo>();
    public ImageAdapter(Context c, ArrayList<Photo> albumPhotos) {
        mContext = c; this.albumPhotos = albumPhotos;
    }

    public int getCount() {
        return albumPhotos.size();
    }

    public Photo getItem(int position) {
        return albumPhotos.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(230, 230));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(0, 5, 0, 5);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageURI(Uri.parse(albumPhotos.get(position).getFilePath()));
        Log.d("test", "did this run?");
        return imageView;
    }

}
