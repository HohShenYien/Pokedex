package com.syen.application.pokedex;

import android.annotation.SuppressLint;
import android.graphics.Color;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// A bunch of views to be modified and edit
public class InfoActivity extends AppCompatActivity {
    private TextView nameTxt, generaTxt, infoIdTxt, descriptionTxt;
    private TextView type1Txt, type2Txt;
    private ListView evolve_1, evolve_2, evolve_3, evolve_from;
    private TextView[] statsTxt;
    private TextView height, weight, habitat, growth_rate, generationTxt, capture_rate, base_happiness;
    private ImageView infoImg;
    private Pokemon curPokemon;
    private JsonObject jsonObject;
    private Map<String, String> primaryColorMap = new HashMap<>();
    private Map<String, String> secondaryColorMap = new HashMap<>();
    private Map<String, String> typeColorMap = new HashMap<>();
    private Map<String, String> typeStrokeColorMap = new HashMap<>();
    private ScrollView scrollView;
    private LinearLayout[] linear = new LinearLayout[6];
    private boolean caughtState;
    private Menu menu;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.info_activity);

        // Getting the views correct
        getView();

        // Then I get my jsonObject
        Bundle bundle = getIntent().getBundleExtra("bundleInfo");

        // Then I get my pokemon

        jsonObject = JsonParser.parseString(bundle.getString("infos")).getAsJsonObject();
        Log.d("CheckJson", jsonObject.toString());
        String pokemonName = jsonObject.get("pokemonName").getAsString();
        int pokemonId = jsonObject.get("id").getAsInt();
        curPokemon = new Pokemon(pokemonId, pokemonName);

        try {
            loadColor();
            loadIntoView();
        } catch (JSONException e) {
            Log.e("JSONError", e.toString());
        }

    }

    // Gets all the view from the layout
    private void getView(){
        nameTxt = findViewById(R.id.nameTxt);
        generaTxt = findViewById(R.id.generaTxt);
        infoIdTxt = findViewById(R.id.infoIdTxt);
        descriptionTxt = findViewById(R.id.descriptionTxt);
        type1Txt = findViewById(R.id.type1Txt);
        type2Txt = findViewById(R.id.type2Txt);
        evolve_from = findViewById(R.id.evolve_from);
        evolve_1 = findViewById(R.id.evolve_1);
        evolve_2 = findViewById(R.id.evolve_2);
        evolve_3 = findViewById(R.id.evolve_3);
        statsTxt = new TextView[5];
        statsTxt[0] = findViewById(R.id.stats1Txt);
        statsTxt[1] = findViewById(R.id.stats2Txt);
        statsTxt[2] = findViewById(R.id.stats3Txt);
        statsTxt[3] = findViewById(R.id.stats4Txt);
        statsTxt[4] = findViewById(R.id.stats5Txt);
        height = findViewById(R.id.height);
        weight = findViewById(R.id.weight);
        habitat = findViewById(R.id.habitat);
        growth_rate = findViewById(R.id.growth_rate);
        generationTxt = findViewById(R.id.generation);
        capture_rate = findViewById(R.id.capture_rate);
        base_happiness = findViewById(R.id.base_happiness);
        infoImg = findViewById(R.id.infoImg);
        scrollView = findViewById(R.id.scrollView);
        linear[0] = findViewById(R.id.linear1);
        linear[1] = findViewById(R.id.linear2);
        linear[2] = findViewById(R.id.linear3);
        linear[3] = findViewById(R.id.linear4);
        linear[4] = findViewById(R.id.linear5);
        linear[5] = findViewById(R.id.linear6);
    }

    // Load data from jsonObject to view
    @SuppressLint("SetTextI18n")
    private void loadIntoView() throws JSONException {
        Log.d("finally", curPokemon.getName());
        nameTxt.setText(curPokemon.getName());
        infoImg.setImageBitmap(curPokemon.getImage(getApplicationContext()));
        infoIdTxt.setText(String.format("#%03d",curPokemon.getId()));
        generaTxt.setText(jsonObject.get("genera").getAsString());
        descriptionTxt.setText(jsonObject.get("description").getAsString());
        JsonArray typeArray = jsonObject.get("types").getAsJsonArray();
        for (int i = 0; i < typeArray.size(); i++){
            String type = typeArray.get(i).getAsJsonObject().
                    get("type").getAsJsonObject().
                    get("name").getAsString();
            GradientDrawable gradientDrawable = createDrwawableBackground(
                    typeColorMap.get(type), 6, typeStrokeColorMap.get(type)
            );
            if (i == 0){
                type1Txt.setText(type);
                type1Txt.setBackground(gradientDrawable);

            } else if (i == 1){
                type2Txt.setText(type);
                type2Txt.setVisibility(TextView.VISIBLE);
                type2Txt.setBackground(gradientDrawable);
            }
        }
        JsonArray statsArray = jsonObject.get("stats").getAsJsonArray();
        for (int i = 0; i < 5; i++){
            JsonObject tmp = statsArray.get(i).getAsJsonObject();
            String value = tmp.get("base_stat").getAsString();
            String stat = tmp.get("stat").getAsJsonObject().get("name").getAsString();
            stat = stat.substring(0, 1).toUpperCase() + stat.substring(1);
            statsTxt[i].setText(stat + ": " + value);
        }
        JsonObject evoFrom = jsonObject.get("evolve_from").getAsJsonObject();
        if (evoFrom.size() == 0){
            List<String> list = new ArrayList<>();
            list.add("None");
            ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, R.layout.simple_row_layout, list);
            evolve_from.setAdapter(arrayAdapter);
        } else{
            String nextName = evoFrom.get("name").getAsString();
            int id = evoFrom.get("id").getAsInt();
            Pokemon newPokemon = new Pokemon(id, nextName);
            ArrayList<Pokemon> newArray = new ArrayList<>();
            newArray.add(newPokemon);
            ListViewAdapter listViewAdapter = new ListViewAdapter(this, newArray);
            evolve_from.setAdapter(listViewAdapter);

        }
        height.setText("Height: " + jsonObject.get("height").getAsString());
        weight.setText("Weight: " + jsonObject.get("weight").getAsString());
        base_happiness.setText("Base happiness: " + jsonObject.get("base_happiness").getAsString());
        capture_rate.setText("Capture rate: " + jsonObject.get("capture_rate").getAsString());
        generationTxt.setText(jsonObject.get("generation").getAsString());
        growth_rate.setText("Growth rate: " + jsonObject.get("growth_rate").getAsString());
        habitat.setText("Habitat: "+ jsonObject.get("habitat").getAsString());

        String color = jsonObject.get("color").getAsString();
        GradientDrawable gradientDrawable = createDrwawableBackground(primaryColorMap.get(color), 0, null);
        scrollView.setBackground(gradientDrawable);
        gradientDrawable = createDrwawableBackground(secondaryColorMap.get(color), 0, null);
        for (int i = 0;i < linear.length; i++){
            linear[i].setBackground(gradientDrawable);
        }

        JsonArray first = jsonObject.get("evolution_1").getAsJsonArray();
        JsonArray second = jsonObject.get("evolve_2").getAsJsonArray();
        JsonArray third = jsonObject.get("evolve_3").getAsJsonArray();

        ArrayList<Pokemon> tempList = new ArrayList<>();
        if (first.size() != 0) {
            String PokemonName = first.get(0).getAsJsonObject().get("name").getAsString();
            int id = first.get(0).getAsJsonObject().get("id").getAsInt();
            Pokemon newPokemon = new Pokemon(id, PokemonName);
            tempList.add(newPokemon);
            ListViewAdapter adapter = new ListViewAdapter(this, tempList);
            evolve_1.setAdapter(adapter);
        }

        if (second.size() == 0){
            ArrayList<String> arrayList = new ArrayList<>() ;
            arrayList.add("None");
            ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, R.layout.simple_row_layout, arrayList);
            evolve_2.setAdapter(arrayAdapter);
        } else {
            ArrayList<Pokemon> arrayList = new ArrayList<>() ;
            for (int i = 0; i < second.size(); i++){
                String PokemonName = second.get(i).getAsJsonObject().get("name").getAsString();
                int id = second.get(i).getAsJsonObject().get("id").getAsInt();
                Pokemon newPokemon = new Pokemon(id, PokemonName);
                arrayList.add(newPokemon);
            }
            ListViewAdapter adapter = new ListViewAdapter(this, arrayList);
            evolve_2.setAdapter(adapter);
        }
        if (third.size() == 0){
            ArrayList<String> arrayList = new ArrayList<>() ;
            arrayList.add("None");
            ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, R.layout.simple_row_layout, arrayList);
            evolve_3.setAdapter(arrayAdapter);
        } else {
            ArrayList<Pokemon> arrayList = new ArrayList<>() ;
            for (int i = 0; i < third.size(); i++){
                String PokemonName = third.get(i).getAsJsonObject().get("name").getAsString();
                int id = third.get(i).getAsJsonObject().get("id").getAsInt();
                Pokemon newPokemon = new Pokemon(id, PokemonName);
                arrayList.add(newPokemon);
            }
            ListViewAdapter adapter = new ListViewAdapter(this, arrayList);
            evolve_3.setAdapter(adapter);
        }


    }

    private void loadColor(){
        String[] colorArray = getResources().getStringArray(R.array.pokedexColorCode);
        String[] secondaryColorArray = getResources().getStringArray(R.array.pokedexColorCodeSecondary);
        String color[] = {"red", "blue", "yellow", "green", "black", "brown", "purple"
                            , "gray", "white", "pink"};
        for (int i = 0; i < color.length; i++) {
            primaryColorMap.put(color[i], colorArray[i]);
            secondaryColorMap.put(color[i], secondaryColorArray[i]);
        }
        colorArray = getResources().getStringArray(R.array.typeColor);
        secondaryColorArray = getResources().getStringArray(R.array.typeStrokeColor);
        String types[] = {"normal", "fire", "fighting", "water", "flying", "grass", "poison", "electric"
                            , "ground", "psychic", "rock", "ice", "bug", "dragon", "ghost",
                            "dark", "steel", "fairy"};
        for (int i = 0; i < types.length; i++) {
            typeColorMap.put(types[i], colorArray[i]);
            typeStrokeColorMap.put(types[i], secondaryColorArray[i]);
        }
    }

    private GradientDrawable createDrwawableBackground(String colorCode, int strokeWidth, String strokeColorCode){

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(20);
        gradientDrawable.setColor(Color.parseColor(colorCode));
        if (strokeWidth > 0){
            gradientDrawable.setCornerRadius(30);
            gradientDrawable.setStroke(strokeWidth, Color.parseColor(strokeColorCode));
        }

        return gradientDrawable;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.catch_menu, menu);
        this.menu = menu;
        if (MainActivity.database.caughtDAO().isCaught(curPokemon.getId()) == 1){
            caughtState = true;
        } else{
            caughtState = false;
        }
        if (caughtState == true){
            changeMenuTitle();
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void changeMenuTitle(){
        MenuItem item = menu.findItem(R.id.catch_menu);
        if (item.getTitle().equals("Catch")){
            item.setTitle("Release");
        } else if (item.getTitle().equals("Release")){
            item.setTitle("Catch");
        }
    }

    public void onMenuItemClick(MenuItem item){
        if (caughtState == true){
            MainActivity.database.caughtDAO().removePokemon(curPokemon.getId());
            caughtState = !caughtState;
        }
        else if(caughtState == false){
            MainActivity.database.caughtDAO().inserPokemon(curPokemon.getName(), curPokemon.getId());
            caughtState = !caughtState;
        }
        changeMenuTitle();
    }

}
