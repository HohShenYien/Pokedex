package com.syen.application.pokedex;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// This is a class that shows loading screen whenever loading
// Because it takes time for a request to be completed
// Update: After doing the second loading section for infoActivity
// I realised this is a brilliant idea to get a new class for loading
public class LoadQuery extends AppCompatActivity{
    // For section 1
    private int size = 100, startInd = 0;
    private List<Pokemon> pokemons  = new ArrayList<>();
    // For section 2
    // Because two requests will run simultaneously, so this finished array
    // will act as indicator
    private boolean[] finished;
    // The object to pass on to infoActivity
    private JSONObject jsonObject;
    // Just declaring, this will be used in load info
    private JSONObject loadedObj;
    // This list helps me to ensure that every volley process has ended before move to next activity
    private List<Boolean> startVolleyEnd;
    private final int JSON_OBJ_SIZE = 18;

    Pokemon curPokemon;
    // These two strings can be declared later, but since already declared then nvm.....
    private String urlEvo;
    private String urlFrom;
    // this code will cause startNewActivity to go to gallery activity
    public static final int GALLERY_ACTIVITY_CODE = 10;
    public static final int INFO_ACTIVITY_CODE = 20;

    private Map<String, String> locationMap = new HashMap<String, String>();

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.progress_bar);

        int nextActivityCode = getIntent().getIntExtra("nextActivityCode",GALLERY_ACTIVITY_CODE);

        if (nextActivityCode == GALLERY_ACTIVITY_CODE) {
            // Loads the information from previous activity
            int generation = getIntent().getIntExtra("generations", 0);

            if (generation == 0) {
                // If zero, get the values from EditText
                size = getIntent().getIntExtra("numbersOfPokemon", 100);
            } else {
                // If not, I'll get it from preset numbers in res folder
                int[] generationArray = getResources().getIntArray(R.array.generations_pokemon);
                startInd = 0;
                for (int i = 0; i < generation; i++) {
                    startInd += generationArray[i];
                }
                size = generationArray[generation];
            }

            loadPokemon(startInd, size);
        // Code for second section
        } else if (nextActivityCode == INFO_ACTIVITY_CODE) {
            // Start by loading the pokemon and declaring the jsonObject needed
            finished = new boolean[]{false, false};
            jsonObject = new JSONObject();
            startVolleyEnd = new ArrayList<>();
            loadLocation();
            Bundle bundle = getIntent().getBundleExtra("pokemonBundle");
            curPokemon = (Pokemon) bundle.getSerializable("pokemon");
            // In case bug :
            if (curPokemon != null){
                // Start the loading process
                loadInfo(curPokemon);
            }

        }


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    // This method calls Volley request method to obtain information about the pokemons
    // from online json file
    private void loadPokemon( final int startInd, int size){
        RequestQueue requestQueue;
        // Start a new request queue
        requestQueue = Volley.newRequestQueue(this);
        // The url, which I obtained from pokeapi.co website
        String url = "https://pokeapi.co/api/v2/pokemon?limit=" + size + "&&offset=" + startInd;

        // Define a new request
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Getting the array of results
                    JSONArray results = response.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        // Getting object at every position
                        JSONObject jsonObject = results.getJSONObject(i);
                        // Getting the items from online, which are id and name
                        String name = jsonObject.getString("name");
                        int id = startInd + 1 + i;
                        Pokemon newPokemon = new Pokemon(id, name);
                        pokemons.add(newPokemon);
                    }

                } catch (JSONException e) {
                    Log.e("JSON error", e.toString());
                }
                // When the request is finished, I will bundle the array and
                // call the method startNewActivity
                Bundle bundle = new Bundle();
                bundle.putSerializable("pokemonArray", (Serializable) pokemons);
                startNewActivity(GALLERY_ACTIVITY_CODE, bundle);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JSON error",error.toString());
            }
        }
        );
        requestQueue.add(request);
    }

    // This enables me to reuse the loadQuery class by setting multiple destination for the next activity
    private void startNewActivity(int code , Bundle bundle){

        if (code == GALLERY_ACTIVITY_CODE) {
            Intent intent = new Intent(this, GalleryActivity.class);
            intent.putExtra("bundledP", bundle);
            startActivity(intent);

        } else if (code == INFO_ACTIVITY_CODE){
            Intent intent = new Intent(this, InfoActivity.class);
            intent.putExtra("bundleInfo", bundle);
            startActivity(intent);
        }
    }
    // This one gets all the information using 2 request, 1 to get information in url1
    // and the other from url2
    private void loadInfo(Pokemon pokemon){

        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);
        String url1 = "https://pokeapi.co/api/v2/pokemon/" + pokemon.getId();
        String url2 = "https://pokeapi.co/api/v2/pokemon-species/" + pokemon.getId();
        try {
            // A Pokemon name and id is passed
            jsonObject.put("pokemonName", pokemon.getName());
            jsonObject.put("id",pokemon.getId());
        } catch (JSONException e){
            Log.e("JSONObj", e.toString());
        }

        // First request
        JsonObjectRequest request1= new JsonObjectRequest(Request.Method.GET, url1, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    // The array of stats and types
                    jsonObject.put("stats", response.getJSONArray("stats"));
                    jsonObject.put("types", response.getJSONArray("types"));
                    // The strings of weight and height
                    jsonObject.put("weight", response.getInt("weight"));
                    jsonObject.put("height", response.getInt("height"));
                    // Finished the first request
                    finished[0] = true;
                }catch (JSONException e){
                    Log.e("JSONObj",e.toString());
                }
                // Make sure both processes are finished before proceeding to third request
                if (finished[0] && finished[1]){
                    loadEvo(urlFrom, urlEvo);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JSONquery", error.toString());
            }
        });

        // Second request
        JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.GET, url2, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    // the String of flavor_text
                    jsonObject.put("description", response.getJSONArray("flavor_text_entries")
                                    .getJSONObject(0).getString("flavor_text"));
                    // Getting the genera, making sure that it is in english
                    JSONArray jsonArray = response.getJSONArray("genera");
                    for (int i = 0; i < jsonArray.length(); i++){
                        if (jsonArray.getJSONObject(i)
                                .getJSONObject("language")
                                .getString("name").equalsIgnoreCase("en")){
                            // The string of genera
                            jsonObject.put("genera", jsonArray.getJSONObject(i).
                                    getString("genus"));
                            break;

                        }

                    }
                    // String of generation, for example: generation-i
                    jsonObject.put("generation", response.getJSONObject("generation").
                            getString("name"));
                    // String of growth rate, for example: medium
                    jsonObject.put("growth_rate", response.getJSONObject("growth_rate").
                            getString("name"));
                    // String of habitat
                    if (response.isNull("habitat")){
                        jsonObject.put("habitat", "not available");
                    } else {
                        jsonObject.put("habitat", locationMap.get(response.getJSONObject("habitat").
                                getString("url")));
                    }
                    // Get 2 more url's for evolution chain and also previous evolution pokemon
                    urlEvo = response.getJSONObject("evolution_chain").getString("url");
                    // Some pokemons aren't evolve from any pokemon
                    if (!response.isNull("evolves_from_species")) {
                        urlFrom = response.getJSONObject("evolves_from_species").
                                getString("url");
                    } else { urlFrom = null;}

                    // int of capture rate and base happiness
                    jsonObject.put("capture_rate", response.getInt("capture_rate"));
                    jsonObject.put("base_happiness", response.getInt("base_happiness"));

                    // Get pokedex color of the pokemon, for styling purpose
                    jsonObject.put("color", response.getJSONObject("color").getString("name"));
                    finished[1] = true;

                }catch(JSONException e){
                    Log.e("JSONObj", e.toString());
                }
                // Same reason as mentioned above
                if (finished[0] && finished[1]){
                    loadEvo(urlFrom, urlEvo);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Adding into requestQueue
        requestQueue.add(request1);
        requestQueue.add(request2);
    }

    // This method utilizes loadIdName method as helper function and loads the possible
    // pokemons that can be evolved to and the pokemon it evolves from
    private void loadEvo(String urlFrom, String urlEvo) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        // First part for originate from
        // if urlFrom is null means the pokemon doesn't come from any pokemon
        if (urlFrom != null) {
            loadIdName(urlFrom, "evolve_from", 1);
        } else{
            try{
                jsonObject.put("evolve_from", new JSONObject());
            } catch (JSONException e) {
                Log.e("JSONPUT", e.toString());
            }
        }

        // Second part for evolve to
        JsonObjectRequest request3 = new JsonObjectRequest(Request.Method.GET, urlEvo, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // This is extra a bit
                JSONArray evolve_to = new JSONArray();
                try {

                    // I'll get the array of evolution first
                    JSONArray evolvesToArray = response.getJSONObject("chain").getJSONArray("evolves_to");
                    jsonObject.put("evolution_1", new JSONArray());
                    loadIdName(
                            response.getJSONObject("chain").
                                    getJSONObject("species").
                                    getString("url"),
                            "evolution_1",
                            2);
                    // If the array has size 0, then jump to next activity
                    if (evolvesToArray.length() == 0){
                        jsonObject.put("evolve_3", new JSONArray());
                        jsonObject.put("evolve_2", new JSONArray());
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("infos", jsonObject.toString());
                        startNewActivity(INFO_ACTIVITY_CODE, bundle);

                    }else {
                        // For every evolution chain:
                        for (int i = 0; i < evolvesToArray.length(); i++) {
                            // I'll get the url to second evolution first
                            String urlNext = evolvesToArray.getJSONObject(i).
                                    getJSONObject("species").
                                    getString("url");
                            // Then check try again the same thing to get the chain to third evolution
                            JSONArray finalEvo = evolvesToArray.getJSONObject(i).
                                    getJSONArray("evolves_to");
                            for (int j = 0; j < finalEvo.length(); j++) {
                                // Get the url and find the pokemons
                                String urlFinal = finalEvo.getJSONObject(j).
                                        getJSONObject("species").
                                        getString("url");
                                if (jsonObject.isNull("evolve_3")) {
                                    jsonObject.put("evolve_3", new JSONArray());
                                }
                                loadIdName(urlFinal, "evolve_3", 2);

                            }
                            if (i == 0) {
                                jsonObject.put("evolve_2", evolve_to);
                            }
                            if (i == evolvesToArray.length() - 1) {
                                // If evolution_3 still empty in the end, just add an extra empty chain
                                if (jsonObject.isNull("evolve_3")) {
                                    jsonObject.put("evolve_3", new JSONArray());
                                }
                                loadIdName(urlNext, "evolve_2", 2);
                            } else {
                                loadIdName(urlNext, "evolve_2", 2);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.e("request3", e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("request3", error.toString());
            }
        });
        requestQueue.add(request3);
    }

    // This is a handy method that deals with jsonObject object and add item to it
    private void loadIdName(String url, final String key, int code){

        final int count = startVolleyEnd.size();
        // Everytime I start a new request, I add a boolean false to the list
        // When I finish, I change it to true and only move to next activity when
        // all requests are completed
        startVolleyEnd.add(false);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        // Code 1 for loading evolves from
        if (code == 1) {
            JsonObjectRequest requestid = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        boolean next = true;
                        loadedObj = new JSONObject();
                        loadedObj.put("id", response.getInt("id"));
                        loadedObj.put("name", response.getString("name"));
                        jsonObject.put(key, loadedObj);
                        startVolleyEnd.set(count, true);
                        for (int i = 0; i < startVolleyEnd.size(); i++){
                            if (!startVolleyEnd.get(i)){
                                next = false;
                            }
                        }
                        if (next && jsonObject.length() == 18) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("infos", jsonObject.toString());
                            Log.d("endedRequest", jsonObject.toString());
                            startNewActivity(INFO_ACTIVITY_CODE, bundle);
                        }
                    } catch (JSONException e) {
                        Log.e("loadId", e.toString());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            requestQueue.add(requestid);
        }

        // Code 2 for evolve to, but essentially same as code 1 only that it takes out a jsonArray, modify it
        // and store it back
        else if (code == 2){
            JsonObjectRequest requestid = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        boolean next = true;
                        // Take the jsonArray out
                        JSONArray jsonArray = jsonObject.getJSONArray(key);
                        loadedObj = new JSONObject();
                        // modify
                        loadedObj.put("id", response.getInt("id"));
                        loadedObj.put("name", response.getString("name"));
                        jsonArray.put(loadedObj);
                        // put it back
                        jsonObject.put(key, jsonArray);
                        // The following if when the last call has occurred

                        startVolleyEnd.set(count, true);
                        for (int i = 0; i < startVolleyEnd.size(); i++){
                            if (!startVolleyEnd.get(i)){
                                next = false;
                            }
                        }
                        if (next && jsonObject.length() == 18) {
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("infos", jsonObject.toString());
                            Log.d("endedRequest", jsonObject.toString());
                            startNewActivity(INFO_ACTIVITY_CODE, bundle);
                        }
                    } catch (JSONException e) {
                        Log.e("loadId", e.toString());
                    }

                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            requestQueue.add(requestid);
        }


    }

    private void loadLocation(){
        String[] locations = {"cave", "forest", "grassland", "mountain",
                "rare", "rough-terrain", "sea", "urban", "waters-edge"};
        String[] locationsUrl = {"https://pokeapi.co/api/v2/pokemon-habitat/1/", "https://pokeapi.co/api/v2/pokemon-habitat/2/",
                "https://pokeapi.co/api/v2/pokemon-habitat/3/", "https://pokeapi.co/api/v2/pokemon-habitat/4/",
                "https://pokeapi.co/api/v2/pokemon-habitat/5/", "https://pokeapi.co/api/v2/pokemon-habitat/6/",
                "https://pokeapi.co/api/v2/pokemon-habitat/7/", "https://pokeapi.co/api/v2/pokemon-habitat/8/",
                "https://pokeapi.co/api/v2/pokemon-habitat/9/"};

        for (int i = 0; i < locations.length; i++){
            locationMap.put(locationsUrl[i], locations[i]);
        }
    }
}


