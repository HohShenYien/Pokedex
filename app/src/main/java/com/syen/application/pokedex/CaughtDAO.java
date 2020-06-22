package com.syen.application.pokedex;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CaughtDAO {
    @Query("INSERT INTO Caught(id, name) VALUES (:id, :name)")
    void inserPokemon(String name, int id);

    @Query("SELECT * FROM Caught")
    List<CaughtEntity> getAll();

    @Query("DELETE FROM Caught WHERE id=:id")
    void removePokemon(int id);

    @Query("SELECT COUNT(id) FROM Caught WHERE id=:id")
    int isCaught(int id);
}
