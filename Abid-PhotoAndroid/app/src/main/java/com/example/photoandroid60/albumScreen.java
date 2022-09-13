package com.example.photoandroid60;
import static androidx.core.content.res.TypedArrayUtils.getResourceId;

import com.example.model.serialController;
import com.example.photoandroid60.databinding.ActivityAlbumScreenBinding;
import com.example.photoandroid60.databinding.ActivityMainBinding;
import com.example.util.Album;
import com.example.util.Photo;
import com.example.util.Tag;

import androidx.appcompat.app.ActionBar;

import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.Menu;
import android.app.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.content.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;



public class albumScreen extends AppCompatActivity {
    static final int REQUEST_IMAGE_GET = 1;
    ActivityAlbumScreenBinding binding;
    File albumFile = new File("/data/data/com.example.photoandroid60/files/albums.ser");
    serialController cereal = new serialController(albumFile);
    GridView gridview;
    ArrayList<Album> albumData;
    Album selectedData;
    ImageAdapter myImgAdapter;
    Boolean search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAlbumScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigation.setOnItemSelectedListener(this::onOptionsItemSelected);
        //super.onCreate(savedInstanceState);
        try {
            albumData = cereal.data();
            Log.d("Serialized test", ""+albumData.size());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        selectedData =(Album) getIntent().getExtras().get("selectedAlbum");
        //setContentView(R.layout.activity_album_screen);
        getSupportActionBar().setTitle(selectedData.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //setContentView(R.layout.activity_album_screen);

        search = (Boolean) getIntent().getExtras().get("search");
        myImgAdapter = new ImageAdapter(this, selectedData.getPhotos());
        gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(myImgAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String uriPath = myImgAdapter.getItem(i).getFilePath();
                Log.d("uri", uriPath);
                Intent myIntent = new Intent(albumScreen.this, photoslideshow.class).putExtra("selectedPhoto", myImgAdapter.getItem(i)).putExtra("containerAlbum", selectedData).putExtra("position", i).putExtra("search", search);
                startActivityForResult(myIntent, 2);

            }
        });




    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;

            case R.id.add:
                selectImage();
                return true;

            case R.id.rename:
                LayoutInflater li = LayoutInflater.from(this);
                View promptsView = li.inflate(R.layout.nameprompt, null);
                final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setView(promptsView)
                        .create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        Button OK = dialog.findViewById(R.id.buttonSuccess);
                        OK.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                // TODO Do something
                                Log.d("Test", "onCreate: Are you running?");
                                if(userInput.getText().toString().isEmpty()){
                                    userInput.setError("Please enter an album name.");
                                    return;
                                }
                                for(Album x : albumData){
                                    if(!x.equals(selectedData) && x.getName().equals(userInput.getText().toString())){
                                        userInput.setError("Album name already exists");
                                        return;
                                    }
                                }
                                String oldName = selectedData.getName();
                                selectedData.setName(userInput.getText().toString());
                                refresh(oldName);
                                getSupportActionBar().setTitle(selectedData.getName());
                                dialog.dismiss();
                                return;
                            }
                        });

                        Button CANCEL = dialog.findViewById(R.id.buttonCancel);
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
            case R.id.delete:
                for (int i = 0; i < albumData.size(); i++) {
                    if (albumData.get(i).getName().equals(selectedData.getName())) {
                        albumData.remove(i);
                        try {
                            cereal.update(albumData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                this.finish();
                return true;
            case R.id.search:
                li = LayoutInflater.from(this);
                promptsView = li.inflate(R.layout.search, null);
                final AutoCompleteTextView userInput1 = (AutoCompleteTextView) promptsView.findViewById(R.id.userInput1);
                final AutoCompleteTextView userInput2 = (AutoCompleteTextView) promptsView.findViewById(R.id.userInput2);
                ArrayList<String> autoFillResults = new ArrayList<>();
                for(Album x : albumData){
                    for(Photo y: x.getPhotos()){
                        for(Tag z: y.getTags()){
                            if(!autoFillResults.contains(z.getValue().toLowerCase())){
                                autoFillResults.add(z.getValue().toLowerCase());
                            }
                        }
                    }
                }

                ArrayAdapter<String> adapter =
                        new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autoFillResults);

                userInput1.setAdapter(adapter);
                userInput2.setAdapter(adapter);

                ArrayList<String> options= new ArrayList<>();
                options.add("Person");
                options.add("Location");

                ArrayList<String> options2 = new ArrayList<>();
                options2.add("");
                options2.add("AND");
                options2.add("OR");
                final TextView tv = (TextView) promptsView.findViewById(R.id.textView2);

                final Spinner tagChoices1 = (Spinner) promptsView.findViewById(R.id.tagChoice1);
                final Spinner tagChoices2 = (Spinner) promptsView.findViewById(R.id.tagChoice2);
                final Spinner tagChoices3 = (Spinner) promptsView.findViewById(R.id.tagChoice3);

                final ArrayAdapter<String> adp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);
                final ArrayAdapter<String> adp2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options2);

                tagChoices1.setAdapter(adp);
                tagChoices2.setAdapter(adp2);
                tagChoices3.setAdapter(adp);

                dialog = new AlertDialog.Builder(this)
                        .setView(promptsView)
                        .create();
                Log.d("Test", "onCreate: Are you running?");
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        tagChoices2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                if(!tagChoices2.getSelectedItem().toString().isEmpty()){
                                    tagChoices3.setVisibility(View.VISIBLE);
                                    userInput2.setVisibility(View.VISIBLE);
                                    tv.setVisibility(View.VISIBLE);
                                }
                                else{
                                    tagChoices3.setVisibility(View.GONE);
                                    userInput2.setVisibility(View.GONE);
                                    tv.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parentView) {
                                // your code here
                            }

                        });

                        userInput1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean b) {
                                if(!b){
                                    Log.d("test", "is this runnig?");
                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }
                            }
                        });

                        userInput2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean b) {
                                if(!b){
                                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }
                            }
                        });




                        Button OK = dialog.findViewById(R.id.buttonSuccess);;
                        OK.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {

                                if(tagChoices2.getSelectedItem().toString().isEmpty()){
                                    if(userInput1.getText().toString().isEmpty() ||userInput1.getText().toString().trim().isEmpty() ){
                                        userInput1.setError("Please enter an album name.");
                                        return;
                                    }
                                    else{
                                        Album searchResults = new Album("Search Results");

                                        for(Album x : albumData){
                                            for(Photo y : x.getPhotos()){
                                                for(Tag z : y.getTags()){
                                                    if(z.toString().toLowerCase().equals(tagChoices1.getSelectedItem().toString().toLowerCase()+": \t\t"+userInput1.getText().toString().toLowerCase())){
                                                        searchResults.addPhoto(y.getTags(), y.getFilePath());
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        Intent myIntent = new Intent(albumScreen.this, albumScreen.class).putExtra("selectedAlbum", searchResults).putExtra("search", true);
                                        startActivity(myIntent);

                                        dialog.dismiss();
                                    }
                                }

                                else{
                                    if(userInput1.getText().toString().isEmpty() ||userInput1.getText().toString().trim().isEmpty() ){
                                        userInput1.setError("Please enter an album name.");
                                        if(userInput2.getText().toString().isEmpty() ||userInput1.getText().toString().trim().isEmpty()){
                                            userInput2.setError("Please enter an album name.");
                                        }
                                        return;
                                    }
                                    else if(userInput2.getText().toString().isEmpty() ||userInput1.getText().toString().trim().isEmpty()){
                                        userInput2.setError("Please enter an album name.");
                                        return;
                                    }

                                    else{
                                        Album searchResults = new Album("Search Results");

                                        for(Album x : albumData){
                                            for(Photo y : x.getPhotos()){
                                                boolean found = false;
                                                boolean found2 = false;
                                                for(Tag z : y.getTags()){

                                                    if(tagChoices2.getSelectedItem().toString().equals("AND")) {

                                                        if (z.toString().toLowerCase().equals(tagChoices1.getSelectedItem().toString().toLowerCase() + ": \t\t" + userInput1.getText().toString().toLowerCase())) {
                                                            found = true;
                                                        }
                                                        if (z.toString().toLowerCase().equals(tagChoices3.getSelectedItem().toString().toLowerCase() + ": \t\t" + userInput2.getText().toString())) {
                                                            found2 = true;
                                                        }

                                                        if(found && found2){
                                                            searchResults.addPhoto(y.getTags(), y.getFilePath());
                                                        }
                                                    }

                                                    if(tagChoices2.getSelectedItem().toString().equals("OR")){
                                                        if (z.toString().toLowerCase().equals(tagChoices1.getSelectedItem().toString().toLowerCase() + ": \t\t" + userInput1.getText().toString().toLowerCase())
                                                                || z.toString().toLowerCase().equals(tagChoices3.getSelectedItem().toString().toLowerCase() + ": \t\t" + userInput2.getText().toString().toLowerCase())){
                                                            searchResults.addPhoto(y.getTags(), y.getFilePath());
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Intent myIntent = new Intent(albumScreen.this, albumScreen.class).putExtra("selectedAlbum", searchResults).putExtra("search", true);
                                        startActivity(myIntent);
                                        dialog.dismiss();
                                    }
                                }

                            }
                        });

                        Button CANCEL = dialog.findViewById(R.id.buttonCancel);
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


        }
        return super.onOptionsItemSelected(item);
    }

    public void selectImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            boolean found = false;
            Uri fullPhotoUri = data.getData();
            Log.d("test", fullPhotoUri.toString());
            for(Photo x : selectedData.getPhotos()){
                if(x.getFilePath().equals((fullPhotoUri.toString()))){
                    found = true;
                }
            }
            if(!found)
                selectedData.addPhoto(new ArrayList<Tag>(), fullPhotoUri.toString());
            refresh(null);
            Log.d("Test", ""+selectedData.getPhotoCount());
            myImgAdapter.notifyDataSetChanged();
            gridview.setAdapter(myImgAdapter);

        }

        if(requestCode == 2){

            cereal = new serialController(albumFile);
            try {
                albumData = cereal.data();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            refresh("Test");
            myImgAdapter.notifyDataSetChanged();
            gridview.setAdapter(myImgAdapter);
        }
    }


    public void refresh(String oldName){

        if(oldName == null) {
            for (int i = 0; i < albumData.size(); i++) {
                if (albumData.get(i).getName().equals(selectedData.getName())) {
                    albumData.remove(i);
                    albumData.add(i, selectedData);
                    try {
                        cereal.update(albumData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        else if(oldName.equals("Test")){
            cereal = new serialController(albumFile);
            try {
                albumData = cereal.data();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < albumData.size(); i++) {
                if (albumData.get(i).getName().equals(selectedData.getName())) {

                    selectedData = albumData.get(i);
                    myImgAdapter = new ImageAdapter(this, selectedData.getPhotos());
                    break;
                }
            }

        }

        else{
            for (int i = 0; i < albumData.size(); i++) {
                if (albumData.get(i).getName().equals(oldName)) {
                    albumData.remove(i);
                    albumData.add(i, selectedData);
                    try {
                        cereal.update(albumData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

    }
}