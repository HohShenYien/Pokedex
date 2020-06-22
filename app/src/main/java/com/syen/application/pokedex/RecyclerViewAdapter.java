package com.syen.application.pokedex;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.pokedexViewHolder>
            implements Filterable {

    // About to load the pokemon
    // pokemonArray contains all the pokemons loaded with specific id's
    private List<Pokemon> pokemonArray;
    // filteredArray contains pokemons that are filtered from name
    // curArray is the arrays going to be displayed
    private List<Pokemon> curArray;
    private Context context;

    // The list will be passed in as argument when calling the adapter
    public RecyclerViewAdapter (Context context, List<Pokemon> inputArray){
        super();
        this.context = context;
        this.pokemonArray = inputArray;
        // Before filtering, the current array is the entire array
        curArray = pokemonArray;
    }

    // Now it's for filtering the pokemon!
    @Override
    public Filter getFilter() {
        // Calling the filter methods
        return filterPokemon;
    }
    // Performs the filter
    private Filter filterPokemon = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // The filtered items
            List<Pokemon> filteredArray = new ArrayList<>();

            // If nothing is typed then all are return
            if (constraint == null || constraint.length() == 0){
                filteredArray.addAll(pokemonArray);
            } else{
                // Convert everything to lowercase for easier manipulation
                String filterPattern = constraint.toString().toLowerCase(Locale.getDefault());
                // Check if the pattern is contained in the name for every pokemon
                for (Pokemon item: pokemonArray){
                    if (item.getName().toLowerCase(Locale.getDefault()).contains(filterPattern)){
                        filteredArray.add(item);
                    }
                }
            }
            // Then return the results in for of filterResults
            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredArray;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // Then I'll set the Array to be displayed as filtered array
            curArray = (List<Pokemon>) results.values;
            // And tell the system that dateSet has changes
            notifyDataSetChanged();
        }
    };


    // A class for every viewHolder within (the individual items in the
    // recycler view)
    public static class pokedexViewHolder extends RecyclerView.ViewHolder{
        // Getting the views
        public ImageView pokemonImg;
        public TextView idTxt, nameTxt;
        private RelativeLayout containerView;
        pokedexViewHolder(View view){
            super(view);

            // Getting the elements
            pokemonImg = view.findViewById(R.id.pokemonImg);
            idTxt = view.findViewById(R.id.idTxt);
            nameTxt = view.findViewById(R.id.pokemonNameTxt);
            containerView = view.findViewById(R.id.grid_item);

            // Once a specific grid item is clicked :
            containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // getting the id as identifier for loading the info
                    Pokemon pokemon = (Pokemon) containerView.getTag();
                    Intent intent = new Intent(v.getContext(), LoadQuery.class);
                    // sending the information along with activation code
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("pokemon", pokemon);
                    Log.d("recyclerView", pokemon.getName());
                    intent.putExtra("pokemonBundle", bundle);
                    intent.putExtra("nextActivityCode",LoadQuery.INFO_ACTIVITY_CODE);
                    // Start the activty
                    v.getContext().startActivity(intent);
                }
            });

        }
    }

    // Now, here expands the layout into item
    @NonNull
    @Override
    public pokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);

        return new pokedexViewHolder(view);
    }

    // Then setting up every viewHolder when around the screen area
    @Override
    public void onBindViewHolder(@NonNull pokedexViewHolder holder, int position) {
        Pokemon currentPokemon = curArray.get(position);
        holder.nameTxt.setText(currentPokemon.getName());
        // Then I formatted the string so that the id is nicer with 0's in front
        holder.idTxt.setText(String.format(Locale.getDefault(),"#%03d",currentPokemon.getId()));
        holder.pokemonImg.setImageBitmap(currentPokemon.getImage(context));
        holder.containerView.setTag(currentPokemon);
    }

    @Override
    public int getItemCount() {
        return curArray.size();
    }

}
