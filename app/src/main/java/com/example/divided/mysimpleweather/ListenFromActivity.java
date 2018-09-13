package com.example.divided.mysimpleweather;

public interface ListenFromActivity {
    void getWeatherByCity(String cityName, String date, boolean today);

    void getWeatherByCordinates(double longitude, double latitude, String date, boolean today);
}
