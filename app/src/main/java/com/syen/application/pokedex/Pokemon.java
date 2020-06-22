package com.syen.application.pokedex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

public class Pokemon implements Serializable {
    private int id;
    private String name;
    public Pokemon(int id, String name){
        this.id = id;
        this.name = name;

    }

    public int getId() {
        return id;
    }

    public String getName(){

        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public Bitmap getImage(Context context) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) context.getResources().getDrawable(
                context.getResources().getIdentifier("pokemon_" + Integer.toString(id), "drawable",
                        context.getPackageName()));
        Bitmap image = bitmapDrawable.getBitmap();
        return image;
    }
}
