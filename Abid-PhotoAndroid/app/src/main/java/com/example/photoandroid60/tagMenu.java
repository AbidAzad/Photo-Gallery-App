package com.example.photoandroid60;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.model.serialController;
import com.example.photoandroid60.databinding.ActivityPhotoslideshowBinding;
import com.example.photoandroid60.databinding.ActivityTagMenuBinding;
import com.example.util.Album;
import com.example.util.Photo;
import com.example.util.Tag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class tagMenu extends AppCompatActivity {
    ActivityTagMenuBinding binding;
    ListView tagList;
    File albumFile = new File("/data/data/com.example.photoandroid60/files/albums.ser");
    serialController cereal = new serialController(albumFile);

    ArrayList<Album> albumData;
    Photo photoContainer;
    Album albumContainer;
    ArrayList<Tag> tagMenu = new ArrayList<Tag>();

    ArrayAdapter<String> arr;
    ArrayList<String> tagItems = new ArrayList<String>();

    int selected = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityTagMenuBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.bottomNavigation.setOnItemSelectedListener(this::onOptionsItemSelected);

        photoContainer =(Photo) getIntent().getExtras().get("photoContainer");
        albumContainer = (Album) getIntent().getExtras().get("containerAlbum");

        try {
            albumData = cereal.data();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        tagMenu = photoContainer.getTags();

        for(Tag x : tagMenu){
            tagItems.add(x.toString());
        }

        //setContentView(R.layout.activity_tag_menu);
        getSupportActionBar().setTitle("Tags");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tagList = findViewById(R.id.list);
        tagList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        tagList.setSelector(R.color.teal_200);
        arr = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, tagItems);
        tagList.setAdapter(arr);

        tagList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selected = i;

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.add:
                LayoutInflater li = LayoutInflater.from(this);
                View promptsView = li.inflate(R.layout.tagalert, null);
                final EditText userInput = (EditText) promptsView.findViewById(R.id.userInput);
                final Spinner tagChoices = (Spinner) promptsView.findViewById(R.id.tagChoices);
                ArrayList<String> options = new ArrayList<>();
                options.add("Person");
                options.add("Location");
                final ArrayAdapter<String> adp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);
                tagChoices.setAdapter(adp);
                tagChoices.setPadding(5,5,5,5);
                AlertDialog dialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialog_AppCompat_PhotoAndroidAlert))
                        .setView(promptsView)
                        .setPositiveButton(android.R.string.ok, null) //Set to null. We override the onclick
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
                Log.d("Test", "onCreate: Are you running?");
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        Button OK = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        OK.setOnClickListener(new View.OnClickListener() {


                            @Override
                            public void onClick(View view) {
                                // TODO Do something
                                if(userInput.getText().toString().isEmpty() || userInput.getText().toString().trim().isEmpty()){
                                    userInput.setError("Please enter an album name.");
                                    return;
                                }

                                Tag newTag = new Tag(tagChoices.getSelectedItem().toString(),  userInput.getText().toString());
                                for(String x : tagItems){
                                    if(x.equals(newTag.toString())){
                                        userInput.setError("Album name already exists");
                                        return;
                                    }
                                }
                                tagMenu.add(newTag);
                                tagItems.clear();
                                for(Tag x : tagMenu){
                                    tagItems.add(x.toString());
                                }
                                refresh();
                                arr.notifyDataSetChanged();
                                tagList.setAdapter(arr);
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
            case R.id.delete:
                if(selected == -1){
                    Toast.makeText(getBaseContext(),"No tags selected", Toast.LENGTH_LONG).show();
                    return true;
                }
                for(Tag i : tagMenu){
                    if(i.toString().equals(tagList.getItemAtPosition(selected).toString())){
                        tagMenu.remove(selected);
                        tagItems.clear();
                        for(Tag x : tagMenu){
                            tagItems.add(x.toString());
                        }
                        selected = -1;
                        refresh();
                        break;
                    }
                }
                arr.notifyDataSetChanged();
                tagList.setAdapter(arr);

        }
        return super.onOptionsItemSelected(item);

    }

    public void refresh(){
        photoContainer.setTags(tagMenu);

        for(int i = 0; i < albumData.size(); i++){
            if(albumData.get(i).getName().equals(albumContainer.getName())){
                for(int j = 0; j < albumData.get(i).getPhotos().size(); j++){
                    if(albumData.get(i).getPhotos().get(j).getFilePath().equals(photoContainer.getFilePath())){
                        albumData.get(i).getPhotos().get(j).setTags(tagMenu);

                        try {
                            cereal.update(albumData);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
            }
        }
    }

}