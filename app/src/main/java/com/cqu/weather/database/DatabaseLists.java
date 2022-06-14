package com.cqu.weather.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DatabaseLists {
    @Query("SELECT * FROM CityWeather ORDER BY position")
    List<CityWeather> getAllCity();

//    @Query("SELECT sum(money) FROM MyDatabase where (year=:y and month=:m and day=:d and not inOut)")
//    long dayIn(int y, int m, int d);

    @Insert
    void insert(CityWeather... cw);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(CityWeather... cw);

    @Delete
    void delete(CityWeather... cw);

    @Query("SELECT COUNT(*) FROM CityWeather WHERE cityName=:s")
    boolean existCity(String s);

}