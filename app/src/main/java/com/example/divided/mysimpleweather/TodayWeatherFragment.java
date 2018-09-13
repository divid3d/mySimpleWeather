package com.example.divided.mysimpleweather;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Objects;


public class TodayWeatherFragment extends Fragment {

    ArrayList<Weather> dailyWeather = new ArrayList<>();
    RecyclerView recyclerView;
    OpenWeatherMaps myApiManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_layout, container, false);

        recyclerView = rootView.findViewById(R.id.rv_hourWeather);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setVerticalScrollBarEnabled(true);
        final MyAdapter myAdapter = new MyAdapter(dailyWeather);
        recyclerView.setAdapter(myAdapter);
        myApiManager = new OpenWeatherMaps(getContext());
        myApiManager.setHourWeatherListener(new HourWeatherListener() {
            @Override
            public void getHourWeather(ArrayList<Weather> hourWeather) {
                dailyWeather.clear();
                dailyWeather.addAll(hourWeather);
                myAdapter.notifyDataSetChanged();
                recyclerView.getLayoutManager().scrollToPosition(0);
                recyclerView.scheduleLayoutAnimation();
            }
        });

        ((MainActivity) Objects.requireNonNull(getActivity())).setActivityListenerFragmentOne(new ListenFromActivity() {
            @Override
            public void getWeatherByCity(String cityName, String date, boolean today) {
                myApiManager.getHourWeather(date, cityName, today);
            }

            @Override
            public void getWeatherByCordinates(double longitude, double latitude, String date, boolean today) {
                myApiManager.getHourWeather(date, latitude, longitude, today);
            }
        });
        return rootView;
    }

}
