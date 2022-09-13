package com.example.photoandroid60;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.model.serialController;
import com.example.photoandroid60.databinding.ActivityAlbumScreenBinding;
import com.example.photoandroid60.databinding.ActivityPhotoslideshowBinding;
import com.example.util.Album;
import com.example.util.Photo;
import com.example.util.Tag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class photoslideshow extends AppCompatActivity {

    ActivityPhotoslideshowBinding binding;

    Album container;
    File albumFile = new File("/data/data/com.example.photoandroid60/files/albums.ser");
    serialController cereal = new serialController(albumFile);

    ImageView displayImage;
    Photo displayedPhoto;
    Boolean search;

    ArrayList<Photo> gallery;
    ArrayList<Album> albumData;
    int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPhotoslideshowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigation.setOnItemSelectedListener(this::onOptionsItemSelected);

        try {
            albumData = cereal.data();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        container =(Album) getIntent().getExtras().get("containerAlbum");
        displayedPhoto = (Photo) getIntent().getExtras().get("selectedPhoto");
        currentPosition = (Integer) getIntent().getExtras().get("position");
        search = (Boolean) getIntent().getExtras().get("search");

        gallery = container.getPhotos();
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        displayImage = findViewById(R.id.images);

        displayImage.setImageURI(Uri.parse(displayedPhoto.getFilePath()));
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.back:
                if(currentPosition > 0){
                    displayedPhoto = gallery.get(currentPosition-1);
                    currentPosition--;
                    displayImage.setImageURI(Uri.parse(displayedPhoto.getFilePath()));
                }
                return true;
            case R.id.forward:
                if(currentPosition+1 < gallery.size()){
                    displayedPhoto = gallery.get(currentPosition+1);
                    currentPosition++;
                    displayImage.setImageURI(Uri.parse(displayedPhoto.getFilePath()));
                }
                return true;

            case R.id.delete:
                delete();
                return true;

            case R.id.move:
                ArrayList<String> options = new ArrayList<>();
                for(Album x: albumData){
                    boolean exists = false;
                    for(Photo y : x.getPhotos()){
                        if(y.getFilePath().equals(displayedPhoto.getFilePath())){
                            exists = true;
                        }
                    }
                    if(!exists){
                        options.add(x.getName());
                    }
                }

                if(options.isEmpty()){
                    Toast.makeText(getBaseContext(),"No Available Albums to Move Picture", Toast.LENGTH_LONG).show();
                    return true;
                }
                final ArrayAdapter<String> adp = new ArrayAdapter<String>(photoslideshow.this, android.R.layout.simple_spinner_item, options);
                final Spinner sp = new Spinner(photoslideshow.this);

                sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                sp.setPadding(20, 20, 10, 20);
                sp.setTranslationY(10);

                sp.setAdapter(adp);

                AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog_AppCompat_PhotoAndroidAlert)).setView(sp).setTitle("Select an available album").setPositiveButton(android.R.string.ok, null).setNegativeButton(android.R.string.cancel, null).create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        Button OK = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        OK.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                // TODO Do something
                                Log.d("testttt", sp.getSelectedItem().toString());
                                for(Album x : albumData){
                                    if(x.getName().equals(sp.getSelectedItem().toString())){
                                        Log.d("testttt", "sp.getSelectedItem().toString()");
                                        x.getPhotos().add(displayedPhoto);
                                        break;
                                    }
                                }

                                try {
                                    cereal.update(albumData);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                delete();
                                dialog.dismiss();
                            }
                        });

                        Button CANCEL = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                        CANCEL.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                    }
                });
                dialog.show();
                return true;
            case R.id.photoTags:
                Intent myIntent = new Intent(photoslideshow.this, tagMenu.class).putExtra("photoContainer", displayedPhoto).putExtra("containerAlbum", container);
                startActivityForResult(myIntent, 1);
                return true;





        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        cereal = new serialController(albumFile);
        try {
            albumData = cereal.data();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < albumData.size(); i++) {
            if (albumData.get(i).getName().equals(container.getName())) {

                container = albumData.get(i);
                for(Photo x : container.getPhotos()){
                    if(x.getFilePath().equals(displayedPhoto.getFilePath())){
                        displayedPhoto = x;
                    }
                }
                break;
            }
        }
    }
    public void refresh(){


            for (int i = 0; i < albumData.size(); i++) {
                if (albumData.get(i).getName().equals(container.getName())) {
                    container.setPhotos(gallery);
                    albumData.get(i).setPhotos(gallery);
                    try {
                        cereal.update(albumData);
                        Log.d("TEST", "refresh: did it run? " + albumData.get(i).getPhotoCount());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }



    }

    public void delete(){
        gallery.remove(currentPosition);

        if(gallery.isEmpty()){
            refresh();
            this.finish();
            return;
        }

        else if(currentPosition >= gallery.size()){
            currentPosition--;
        }
        displayedPhoto = gallery.get(currentPosition);
        displayImage.setImageURI(Uri.parse(displayedPhoto.getFilePath()));
        refresh();
    }
}