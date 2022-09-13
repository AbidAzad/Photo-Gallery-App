package com.example.photoandroid60;
import com.example.model.serialController;
import com.example.photoandroid60.databinding.ActivityMainBinding;
import com.example.util.Album;
import com.example.util.Photo;
import com.example.util.Tag;
import com.google.android.material.navigation.NavigationView;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.BlendMode;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.*;
import android.view.*;
import android.widget.*;
import android.content.*;

import java.io.IOException;
import java.util.ArrayList;
import java.io.*;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    ListView al;
    File albumFile = new File("/data/data/com.example.photoandroid60/files/albums.ser");
    serialController cereal;
    ArrayList<Album> data = new ArrayList<>();

    ArrayList<String> albumNames = new ArrayList<String>();
    ArrayAdapter<String> arr;   //TODO: tf could arr be??
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setup view
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigation.setOnItemSelectedListener(this::onOptionsItemSelected);

        if(!albumFile.exists()) {
            Context context = this;
            File file = new File(context.getFilesDir(), "albums.ser");
            try {
                file.createNewFile();
            } catch (IOException e) {

            }
        }
        cereal = new serialController(albumFile);

        try {
            data = cereal.data();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        for(Album x : data){
            albumNames.add(x.getName());
        }

        //setContentView(R.layout.activity_main); not needed anymore
        al = findViewById(R.id.list);

        arr = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, albumNames);
        al.setAdapter(arr);

        al.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("text",al.getItemAtPosition(i).toString() );
                Album selectedData = null;
                for(Album x: data){
                    if(x.getName().equals(al.getItemAtPosition(i).toString())){
                        selectedData=x;
                    }

                }


                Intent myIntent = new Intent(MainActivity.this, albumScreen.class).putExtra("selectedAlbum", selectedData);
                startActivityForResult(myIntent, 1); //TODO: fix deprecated method use

            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            this.data = cereal.data();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        refresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        switch (id)
        {
            case R.id.add:
                LayoutInflater li = LayoutInflater.from(this);
                View promptsView = li.inflate(R.layout.nameprompt, null);
                final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
                AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog_AppCompat_PhotoAndroidAlert))
                        .setView(promptsView)
                        .create();

                Log.d("Test", "onCreate: Are you running?");
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button OK = dialog.findViewById(R.id.buttonSuccess);
                        OK.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                // TODO Do something
                                Log.d("Test", "onCreate: Are you running?");
                                if(userInput.getText().toString().isEmpty() || userInput.getText().toString().trim().isEmpty()){
                                    userInput.setError("Please enter an album name.");
                                    return;
                                }
                                Album newAlbum = new Album(userInput.getText().toString());
                                for(String x : albumNames){
                                    if(x.equals(newAlbum.getName())){
                                        userInput.setError("Album name already exists");
                                        return;
                                    }
                                }
                                System.out.println("did this run?");
                                data.add(newAlbum);
                                refresh();
                                dialog.dismiss();
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
            case R.id.search:
                li = LayoutInflater.from(this);
                promptsView = li.inflate(R.layout.search, null);
                final AutoCompleteTextView userInput1 = (AutoCompleteTextView) promptsView.findViewById(R.id.userInput1);
                final AutoCompleteTextView userInput2 = (AutoCompleteTextView) promptsView.findViewById(R.id.userInput2);
                ArrayList<String> autoFillResults = new ArrayList<>();
                for(Album x : data){
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




                        Button OK = dialog.findViewById(R.id.buttonSuccess);
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

                                        for(Album x : data){
                                            for(Photo y : x.getPhotos()){
                                                for(Tag z : y.getTags()){
                                                    if(z.toString().toLowerCase().equals(tagChoices1.getSelectedItem().toString().toLowerCase()+": \t\t"+userInput1.getText().toString().toLowerCase())){
                                                        searchResults.addPhoto(y.getTags(), y.getFilePath());
                                                        break;
                                                    }
                                                }
                                            }
                                        }

                                        Intent myIntent = new Intent(MainActivity.this, albumScreen.class).putExtra("selectedAlbum", searchResults).putExtra("search", true);
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

                                        for(Album x : data){
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

                                        Intent myIntent = new Intent(MainActivity.this, albumScreen.class).putExtra("selectedAlbum", searchResults).putExtra("search", true);
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
        return true;
    }

    public void refresh(){
        albumNames.clear();
        try {
            cereal.update(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(Album x: data){
            if(!albumNames.contains(x.getName())){
               albumNames.add(x.getName());
            }
        }
        arr.notifyDataSetChanged(); //TODO: change this somehow

    }


}