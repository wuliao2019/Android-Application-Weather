package com.cqu.weather.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CityWeather {
    @PrimaryKey(autoGenerate = true)
    private int id;
    int position;
    String cityName = "洛圣都";
    String info = "--";
    int wid = 0;
    int temperature = 0;
    int humidity = 0;
    String direct = "---";
    String power = "--级";
    int aqi = 0;
    String future;
    String life;
    String date = "----年--月--日";

    public CityWeather() {
    }

    public CityWeather(int t, String cityName) {
        this.position = t;
        this.cityName = cityName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public String getInfo() {
        return info;
    }

    public int getAqi() {
        return aqi;
    }

    public int getHumidity() {
        return humidity;
    }

    public int getPosition() {
        return position;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getWid() {
        return wid == 53 ? 32 : wid;
    }

    public String getDirect() {
        return direct;
    }

    public String getPower() {
        return power;
    }

    public void setAqi(int aqi) {
        this.aqi = aqi;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setDirect(String direct) {
        this.direct = direct;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public void setWid(int wid) {
        this.wid = wid;
    }

    public String getFuture() {
        return future;
    }

    public String getLife() {
        return life;
    }

    public void setFuture(String future) {
        this.future = future;
    }

    public void setLife(String life) {
        this.life = life;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
