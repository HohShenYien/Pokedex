package com.syen.application.pokedex;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {CaughtEntity.class}, version =  1)
public abstract class CaughtDataBase extends RoomDatabase {
    public abstract CaughtDAO caughtDAO();
}
