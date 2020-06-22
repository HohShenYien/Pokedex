package com.syen.application.pokedex;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Database;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    // Default numbers of pokemon showing id
    private int numsOfPokemon = 100;
    // Or if the user actually selects a specific generation of pokemon to show then
    private int pokemonGen = 0;
    // All the xml elements
    private EditText usernameTxt, pokemonNum;
    private Intent intent;
    private ImageView userImg;
    private Button startBtn;
    private Button caughtBtn;
    // Getting username and image
    final int CODE_FOR_PHOTO = 1;
    final String MAINPREFERENCES = "MainPreferences";
    final String CODEFORUSERNAME = "Username";
    public static CaughtDataBase database;


    // Preparing to store user names in shared preference
    // Can only use getSharedPreference after onCreate!
    SharedPreferences sharedPreferences ;

    // The api version is for saving the image into internal storage
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting up the spinner
        Spinner spinner = findViewById(R.id.spinner);
        // Simple adapter for spinner class
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.Generations, R.layout.spinner_row);
        // Setting the layout for inner items
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(this);

        database = Room.databaseBuilder(this, CaughtDataBase.class, "caughtPokemons").
                allowMainThreadQueries().
                build();

        // Setting up the sharedPreference in this activity
        sharedPreferences = getSharedPreferences(MAINPREFERENCES, Context.MODE_PRIVATE);

        // Getting and setting hints for Edit Text
        usernameTxt = findViewById(R.id.user_nameTxt);
        pokemonNum = findViewById(R.id.numPokemonTxt);
        usernameTxt.setHint("username");
        pokemonNum.setHint("The number of pokemons to view");

        // Getting the permission for opening & saving images for the first time
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        // Load username, if any, that is saved
        loadUserName();

        userImg = findViewById(R.id.userImg);
        // Check if image existed, if not then default image will appear
        loadImageFromStorage();

        // Go to the next activity (gallery) when the start button is pressed
        startBtn = findViewById(R.id.startBtn);
        caughtBtn = findViewById(R.id.caughtBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pokemonNum.getText().equals("")) {
                    try {
                        // Sending whatever number is typed in number field
                        numsOfPokemon = Integer.parseInt(pokemonNum.getText().toString());

                        Log.d("numbersPokemon", Integer.toString(numsOfPokemon));
                    }
                    catch (NumberFormatException e){

                    }
                }
                if (numsOfPokemon <= 807 && numsOfPokemon >= 1) {
                    // Start the new activity and go to pokemon gallery
                    // Send the numbers required
                    intent = new Intent(getApplicationContext(), LoadQuery.class);
                    intent.putExtra("generations", pokemonGen);
                    intent.putExtra("numbersOfPokemon", numsOfPokemon);
                    intent.putExtra("nextActivityCode", LoadQuery.GALLERY_ACTIVITY_CODE);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "This version only support up to 807 pokemons and at least 1 pokemon", Toast.LENGTH_LONG).show();
                }
            }
        });
        caughtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<CaughtEntity> list = MainActivity.database.caughtDAO().getAll();
                ArrayList<Pokemon> pokemonList = new ArrayList<>();
                for (int i = 0; i < list.size(); i++){
                    pokemonList.add(list.get(i).returnPokemon(getApplicationContext()));
                }
                Intent intent = new Intent(getApplicationContext(), GalleryActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("pokemonArray", (Serializable) pokemonList);
                intent.putExtra("bundledP", bundle);
                startActivity(intent);
            }
        });
    }
    // Set the pokemonGen to position index once selected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        pokemonGen = position;
    }
    // Else just set it to default, 0
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        pokemonGen = 0;
    }

    // After returning from selecting images in gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Ensure the codes are correct
        if (resultCode == RESULT_OK && data != null && requestCode == CODE_FOR_PHOTO) {
            try {
                // Getting the location of the image
                Uri uri = data.getData();
                // Open the image
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
                // Get the descriptor and converts into bitmap via factory
                FileDescriptor fileDescriptor = pfd.getFileDescriptor();

                Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                // Set the image into the useImg image view
                userImg.setImageBitmap(image);

                // Save the image into internal storage
                saveToInternalStorage(image);

            } catch (FileNotFoundException e) {
                Log.e("OpenError", e.toString());
            }
        }
    }
    // This function able to load image from the gallery, start when the
    // floating button is clicked
    public void load_image(android.view.View v) {
        // Creating a new content for opening documents
        intent = new Intent(Intent.ACTION_PICK);
        // Set the types of documents opened as image
        intent.setType("image/");
        // Start the activity
        startActivityForResult(intent, CODE_FOR_PHOTO);
    }

    private void saveToInternalStorage(Bitmap bitmapImage) {
        // find the file of the current context that is wrapped
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // Create imageDir
        // path to /data/data/pokedex/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Creating the jpg image in the directory
        File mypath = new File(directory, "profile.jpg");

        FileOutputStream fos = null;
        try {
            // Opening an output stream
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            Log.d("savingError", e.toString());
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                Log.e("closingError", e.toString());
            }
        }
    }

    private void loadImageFromStorage() {
        // Same as above, getting the directory
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File dir = cw.getDir("imageDir", Context.MODE_PRIVATE);
        String path = dir.getAbsolutePath();

        try {
            // Open the image file and process into bitmap via factory
            File f = new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            userImg.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            // If can't find means the user haven't save the image yet
            // Ask the user to make save new image
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_LONG).show();
        }

    }

    // Everytime the user finish writing on number of pokemon
    @Override
    public void onPause() {
        // When I get the value of numbers from the EditText pokemonNum
        super.onPause();

        // Here saves the username, if any, is given
        // Might update to only editable after certain button is clicked
        if (usernameTxt.getText() != null){
            String userName = usernameTxt.getText().toString();
            // Saving into sharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(CODEFORUSERNAME, userName);
            editor.apply();

        }
    }

    // Loads any username that is stored
    private void loadUserName(){

        String the_name = sharedPreferences.getString(CODEFORUSERNAME, null);
        if (the_name != null && the_name != ""){
            usernameTxt.setText(the_name);
        }
    }
}
