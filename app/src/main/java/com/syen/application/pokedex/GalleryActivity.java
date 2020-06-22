package com.syen.application.pokedex;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// I know the cast of pokemonArray is definitely correct, so I suppress it instead
@SuppressWarnings("unchecked")
public class GalleryActivity extends AppCompatActivity{
    private RecyclerViewAdapter adapter;
    private final int NUMBER_OF_COLUMNS = 3;
    private GridLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private List<Pokemon> pokemonArray;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pokemon_gallery);

        // Getting the pokemon list from LoadQuery activity
        Bundle bundleExtra = getIntent().getBundleExtra("bundledP");
        // Make sure the bundle I get is not zero
        assert bundleExtra != null;
        pokemonArray = (ArrayList<Pokemon>) bundleExtra.getSerializable("pokemonArray");
        // Setting up adapter, layout manager and recycler view
        adapter = new RecyclerViewAdapter(this, pokemonArray);
        layoutManager = new GridLayoutManager(this, NUMBER_OF_COLUMNS);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


    }
    // Now, this will create the option menu, which includes my search view
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Basically the following 4 lines get the searchview from the menu
        // and make it a searchview (originally a menu item)
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_menu);
        SearchView searchView = (SearchView) menuItem.getActionView();
        // Then I enable this query function on the searchView
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // This doesn't matter because results are updated in real time
            // So enter is pressed or not won't make a difference
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // Get the query text and pass it to the filter in adapter
            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
}
