package com.syen.application.pokedex;

import android.content.Context;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "Caught")
public class CaughtEntity {

    @PrimaryKey
    @ColumnInfo(name = "id")
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    public Pokemon returnPokemon(Context context){
        return new Pokemon(this.id, this.name);
    }
}
