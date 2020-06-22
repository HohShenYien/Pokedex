package com.syen.application.pokedex;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Pokemon> pokemonArrays;

    public ListViewAdapter(Context context, ArrayList<Pokemon> pokemons){
        this.context = context;
        this.pokemonArrays = pokemons;
    }
    @Override
    public int getCount() {
        return pokemonArrays.size();
    }

    @Override
    public Object getItem(int position) {
        return pokemonArrays.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        convertView = LayoutInflater.from(context).inflate(R.layout.list_row_layout, parent, false);
        viewHolder = new ViewHolder(convertView);

        Pokemon curPokemon = pokemonArrays.get(position);
        viewHolder.evoName.setText(curPokemon.getName());
        viewHolder.containerView.setTag(curPokemon);

        return convertView;

    }

    private class ViewHolder {
        TextView evoName;
        LinearLayout containerView;
        public ViewHolder(View view){
            evoName = view.findViewById(R.id.evoTxt);
            containerView = view.findViewById(R.id.list_row);

            containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pokemon curPokemon = (Pokemon) containerView.getTag();
                    Intent intent = new Intent(v.getContext(), LoadQuery.class);
                    // sending the information along with activation code
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("pokemon", curPokemon);
                    intent.putExtra("pokemonBundle", bundle);
                    intent.putExtra("nextActivityCode",LoadQuery.INFO_ACTIVITY_CODE);
                    // Start the activty
                    v.getContext().startActivity(intent);
                }
            });
        }
    }
}
