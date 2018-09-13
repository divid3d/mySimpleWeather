package com.example.divided.mysimpleweather;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter {

    private ArrayList<Weather> mHourWeather;

    MyAdapter(ArrayList<Weather> mHourWeather) {
        this.mHourWeather = mHourWeather;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.hour_weather_layout, viewGroup, false);

        /*view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // odnajdujemy indeks klikniętego elementu
                int positionToDelete = mRecyclerView.getChildAdapterPosition(v);
                // usuwamy element ze źródła danych
                mArticles.remove(positionToDelete);
                // poniższa metoda w animowany sposób usunie element z listy
                notifyItemRemoved(positionToDelete);
            }
        });*/
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        Weather weather = mHourWeather.get(i);
        ((MyViewHolder) viewHolder).mCurrentTemp.setText((String.format("%.1f", weather.getCurrentTemp())).replaceAll(",", ".") + "°C");
        String minMaxText = String.format("%.1f", weather.getMinTemp()).replaceAll(",", ".") + "°C/" + String.format("%.1f", weather.getMinTemp()).replaceAll(",", ".") + "°C";
        ((MyViewHolder) viewHolder).mMinMaxTemp.setText(minMaxText);
        ((MyViewHolder) viewHolder).mHour.setText(weather.getHour());
        ((MyViewHolder) viewHolder).mDescription.setText(weather.getDescription().substring(0, 1).toUpperCase() + weather.getDescription().substring(1));
        atachIcon(((MyViewHolder) viewHolder).mWeatherIcon, weather.getIconCode());

    }

    @Override
    public int getItemCount() {
        return mHourWeather.size();
    }

    private void atachIcon(ImageView mWeatherIcon, String iconCode) {
        iconCode = iconCode.substring(0, iconCode.length() - 1);
        switch (iconCode) {
            case "01":
                mWeatherIcon.setImageResource(R.drawable.ic_clear);
                break;
            case "02":
                mWeatherIcon.setImageResource(R.drawable.ic_light_clouds);
                break;
            case "03":
                mWeatherIcon.setImageResource(R.drawable.ic_cloudy);
                break;
            case "04":
                mWeatherIcon.setImageResource(R.drawable.ic_cloudy);
                break;
            case "09":
                mWeatherIcon.setImageResource(R.drawable.ic_rain);
                break;
            case "10":
                mWeatherIcon.setImageResource(R.drawable.ic_light_rain);
                break;
            case "11":
                mWeatherIcon.setImageResource(R.drawable.ic_storm);
                break;
            case "13":
                mWeatherIcon.setImageResource(R.drawable.ic_snow);
                break;
            case "50":
                mWeatherIcon.setImageResource(R.drawable.ic_fog);
                break;
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mCurrentTemp;
        TextView mMinMaxTemp;
        TextView mHour;
        TextView mDescription;
        ImageView mWeatherIcon;

        MyViewHolder(View pItem) {
            super(pItem);
            mCurrentTemp = pItem.findViewById(R.id.tv_currentTemp);
            mMinMaxTemp = pItem.findViewById(R.id.tv_minmaxTemp);
            mHour = pItem.findViewById(R.id.hours);
            mDescription = pItem.findViewById(R.id.tv_item_description);
            mWeatherIcon = pItem.findViewById(R.id.imageView);
        }
    }
}