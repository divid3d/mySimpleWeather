package com.example.divided.mysimpleweather;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OpenWeatherMaps {
    private RequestQueue requestQueue;
    private CurrentWeatherListener currentWeatherListener;
    private HourWeatherListener hourWeatherListener;
    private Context context;

    OpenWeatherMaps(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    public void setOnCurrentWeatherListener(CurrentWeatherListener listener) {
        this.currentWeatherListener = listener;
    }

    public void setHourWeatherListener(HourWeatherListener listener) {
        this.hourWeatherListener = listener;
    }

    public void getCurrentWeather(double currentLatitude, double currentLongitude) {
        final String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + currentLatitude + "&lon=" + currentLongitude + "&APPID=57299ad11a0f406887595e20843eb967";
        makeCurrentWeatherRequest(new Weather(), url);
    }

    public void getCurrentWeather(String cityName) {
        final String url = "http://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&APPID=57299ad11a0f406887595e20843eb967";
        makeCurrentWeatherRequest(new Weather(), url);
    }


    public void getHourWeather(final String day, double currentLatitude, double currentLongitude, boolean today) {
        final String url = "http://api.openweathermap.org/data/2.5/forecast?lat=" + currentLatitude + "&lon=" + currentLongitude + "&APPID=57299ad11a0f406887595e20843eb967";
        final ArrayList<Weather> dailyWeather = new ArrayList<>();
        makeDailyWeatherRequest(day, url, dailyWeather, today);

    }

    public void getHourWeather(final String day, String cityName, boolean today) {
        final String url = "http://api.openweathermap.org/data/2.5/forecast?q=" + cityName + "&APPID=57299ad11a0f406887595e20843eb967";
        final ArrayList<Weather> dailyWeather = new ArrayList<>();
        makeDailyWeatherRequest(day, url, dailyWeather, today);
    }

    private void makeCurrentWeatherRequest(final Weather currentWeather, String url) {
        JsonObjectRequest request = new JsonObjectRequest(url, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            currentWeather.setCity(response.getString("name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            currentWeather.setIconCode(response.getJSONArray("weather").getJSONObject(0).getString("icon"));
                            currentWeather.setDescription(response.getJSONArray("weather").getJSONObject(0).getString("description"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        try {
                            currentWeather.setCurrentTemp(response.getJSONObject("main").getDouble("temp") - 273.15);
                            currentWeather.setMinTemp(response.getJSONObject("main").getDouble("temp_min") - 273.15);
                            currentWeather.setMaxTemp(response.getJSONObject("main").getDouble("temp_max") - 273.15);
                            currentWeather.setPressure(response.getJSONObject("main").getDouble("pressure"));
                            currentWeather.setHumidity(response.getJSONObject("main").getDouble("humidity"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            currentWeather.setCountry(response.getJSONObject("sys").getString("country"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            currentWeather.setSunrise(response.getJSONObject("sys").getLong("sunrise"));
                            currentWeather.setSunset(response.getJSONObject("sys").getLong("sunset"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            currentWeather.setWindSpeed(response.getJSONObject("wind").getDouble("speed"));
                            currentWeather.setWindDirection(response.getJSONObject("wind").getDouble("deg"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (currentWeatherListener != null) {
                            currentWeatherListener.getCurrentWeather(currentWeather);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });
        requestQueue.add(request);
    }

    private void makeDailyWeatherRequest(final String day, String url, final ArrayList<Weather> dailyWeather, final boolean today) {
        JsonObjectRequest request = new JsonObjectRequest(url, new JSONObject(),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray data;

                        try {
                            data = response.getJSONArray("list");
                            for (int i = 0; i < data.length(); i++) {

                                Weather weatherObject = new Weather();
                                try {
                                    weatherObject.setIconCode(data.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon"));
                                    weatherObject.setDescription(data.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("description"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    weatherObject.setCurrentTemp(data.getJSONObject(i).getJSONObject("main").getDouble("temp") - 273.15);
                                    weatherObject.setMinTemp(data.getJSONObject(i).getJSONObject("main").getDouble("temp_min") - 273.15);
                                    weatherObject.setMaxTemp(data.getJSONObject(i).getJSONObject("main").getDouble("temp_max") - 273.15);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                try {
                                    final String time = data.getJSONObject(i).getString("dt_txt");
                                    weatherObject.setHour(time.substring(time.length() - 8, time.length() - 3));
                                    weatherObject.setDate(time.substring(0, 10));

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if (today) {
                                    if (weatherObject.getDate().equals(day)) {
                                        dailyWeather.add(weatherObject);
                                    }
                                } else {
                                    if (!weatherObject.getDate().equals(day)) {
                                        weatherObject.setHour(weatherObject.getDate() + " " + weatherObject.getHour());
                                        dailyWeather.add(weatherObject);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (hourWeatherListener != null) {
                            hourWeatherListener.getHourWeather(dailyWeather);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(request);
    }
}
