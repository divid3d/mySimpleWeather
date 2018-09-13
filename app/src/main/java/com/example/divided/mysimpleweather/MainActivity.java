package com.example.divided.mysimpleweather;


import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {


    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int ALL_PERMISSIONS_RESULT = 101;
    public ListenFromActivity activityListenerFragmentOne;
    public ListenFromActivity activityListinerFragmentTwo;
    Location mLocation;
    TextView latLng;
    GoogleApiClient mGoogleApiClient;
    RequestQueue requestQueue;
    View currentWeatherView;
    private long UPDATE_INTERVAL = 15000;  /* 15 secs */
    private long FASTEST_INTERVAL = 5000; /* 5 secs */
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    private double currentLatitude;
    private double currentLongitude;
    private TextView mCurrentTemp, mDescription, mMaxTemp, mMinTemp, mCity, mCounty, mHumidity, mPressure, mWindSpeed, mDate, mDay, mSunrise, mSunset, mLastUpdate;
    private android.support.v7.widget.Toolbar mTopToolbar;
    private SwipeRefreshLayout mSwipeToRefresh;
    private ImageView mWeatherIcon;
    private ImageView mWindDirection;
    private TabLayout mTabs;
    private ViewPager mViewPager;
    private DrawerLayout mDrawerLayout;
    private OpenWeatherMaps myApiManager;
    private EditText mGoogleSearch;

    public void setActivityListenerFragmentOne(ListenFromActivity activityListener) {
        this.activityListenerFragmentOne = activityListener;
    }

    public void setActivityListenerFragmentTwo(ListenFromActivity activityListener) {
        this.activityListinerFragmentTwo = activityListener;
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        currentWeatherView = findViewById(R.id.includedLayout);
        mCurrentTemp = currentWeatherView.findViewById(R.id.tv_currentTemp);
        mMaxTemp = currentWeatherView.findViewById(R.id.tv_maxTemp);
        mMinTemp = currentWeatherView.findViewById(R.id.tv_minTemp);
        mWindSpeed = currentWeatherView.findViewById(R.id.tv_windSpeed);
        mCounty = currentWeatherView.findViewById(R.id.tv_county);
        mCity = currentWeatherView.findViewById(R.id.tv_city);
        mHumidity = currentWeatherView.findViewById(R.id.tv_humidity);
        mPressure = currentWeatherView.findViewById(R.id.tv_pressure);
        mWeatherIcon = currentWeatherView.findViewById(R.id.iv_weatherIcon);
        mDate = currentWeatherView.findViewById(R.id.tv_date);
        mDay = currentWeatherView.findViewById(R.id.tv_day);
        mWindDirection = currentWeatherView.findViewById(R.id.iv_windDirection);
        mSunrise = currentWeatherView.findViewById(R.id.tv_sunrise);
        mSunset = currentWeatherView.findViewById(R.id.tv_sunset);
        mDescription = currentWeatherView.findViewById(R.id.tv_description);
        mLastUpdate = currentWeatherView.findViewById(R.id.tv_last_update);
        mTabs = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.viewpager);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        currentWeatherView.setVisibility(View.INVISIBLE);
        mTabs.setVisibility(View.INVISIBLE);
        mViewPager.setVisibility(View.INVISIBLE);

        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
                .build();

        PlaceAutocompleteFragment places = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setFilter(typeFilter);

        mGoogleSearch = ((EditText) Objects.requireNonNull(places.getView()).findViewById(R.id.place_autocomplete_search_input));
        mGoogleSearch.setTextColor(Color.WHITE);
        mGoogleSearch.setHintTextColor(getResources().getColor(R.color.fadeOutWhite));
        ((ImageView) places.getView().findViewById(R.id.place_autocomplete_search_button)).setColorFilter(Color.WHITE);
        ((ImageView) places.getView().findViewById(R.id.place_autocomplete_clear_button)).setColorFilter(Color.WHITE);


        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                final double longitude = place.getLatLng().longitude;
                final double latitude = place.getLatLng().latitude;
                myApiManager.getCurrentWeather(latitude, longitude);
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date = dateFormat.format(calendar.getTime());
                if (null != activityListenerFragmentOne) {
                    activityListenerFragmentOne.getWeatherByCordinates(longitude, latitude, date, true);
                }
                if (null != activityListinerFragmentTwo) {
                    activityListinerFragmentTwo.getWeatherByCordinates(longitude, latitude, date, false);
                }
            }

            @Override
            public void onError(Status status) {

                Toast.makeText(getApplicationContext(), status.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        mSwipeToRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeToRefresh.setColorSchemeResources(
                R.color.refresh_progress_1,
                R.color.refresh_progress_2,
                R.color.refresh_progress_3);

        mSwipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myApiManager.getCurrentWeather(currentLatitude, currentLongitude);
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String date = dateFormat.format(calendar.getTime());
                if (null != activityListenerFragmentOne) {
                    activityListenerFragmentOne.getWeatherByCordinates(currentLongitude, currentLatitude, date, true);
                }
                if (null != activityListinerFragmentTwo) {
                    activityListinerFragmentTwo.getWeatherByCordinates(currentLongitude, currentLatitude, date, false);
                }
                mSwipeToRefresh.setRefreshing(false);
            }
        });


        myApiManager = new OpenWeatherMaps(this);
        myApiManager.setOnCurrentWeatherListener(new CurrentWeatherListener() {
            @Override
            public void getCurrentWeather(Weather weather) {
                updateCurrentWeatherView(weather);
            }
        });

        mTopToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(mTopToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        DayWeatherAdapter adapter = new DayWeatherAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
        mTabs.setupWithViewPager(mViewPager);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkPlayServices()) {
            latLng.setText("Please install Google Play services.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);


        if (mLocation != null) {
            currentLongitude = mLocation.getLongitude();
            currentLatitude = mLocation.getLatitude();
        }

        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            currentLongitude = mLocation.getLongitude();
            currentLatitude = mLocation.getLatitude();
        }

    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else
                finish();

            return false;
        }
        return true;
    }

    protected void startLocationUpdates() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Enable Permissions", Toast.LENGTH_LONG).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);


    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED);
            }
        }
        return false;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0).toString())) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_cloud) {
            mGoogleSearch.getText().clear();
            myApiManager.getCurrentWeather(currentLatitude, currentLongitude);
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String date = dateFormat.format(calendar.getTime());
            if (null != activityListenerFragmentOne) {
                activityListenerFragmentOne.getWeatherByCordinates(currentLongitude, currentLatitude, date, true);
            }
            if (null != activityListinerFragmentTwo) {
                activityListinerFragmentTwo.getWeatherByCordinates(currentLongitude, currentLatitude, date, false);
            }
            return true;
        } else if (id == R.id.action_favourite) {
        } else if (id == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void updateCurrentWeatherView(Weather weather) {
        mCity.setText(weather.getCity());
        mCounty.setText(weather.getCountry());
        mCurrentTemp.setText(String.format("%.1f", weather.getCurrentTemp()).replaceAll(",", ".") + "°C");
        mMinTemp.setText(String.format("%.1f", weather.getMinTemp()).replaceAll(",", ".") + "°C");
        mMaxTemp.setText(String.format("%.1f", weather.getMaxTemp()).replaceAll(",", ".") + "°C");
        mPressure.setText("Pressure: " + String.valueOf(weather.getPressure()) + " hPa");
        mHumidity.setText("Humidity: " + String.valueOf(weather.getHumidity()) + " %");
        mWindSpeed.setText("Wind: " + String.valueOf(weather.getWindSpeed()) + " m/s");
        attachIcon(mWeatherIcon, weather.getIconCode());
        mWindDirection.setRotation((-1) * (float) weather.getWindDirection());
        mSunrise.setText("Sunrise: " + DateFormat.format("hh:mm:ss aa", weather.getSunrise() * 1000L).toString());
        mSunset.setText("Sunset: " + DateFormat.format("hh:mm:ss aa", weather.getSunset() * 1000L).toString());
        mDescription.setText(weather.getDescription().substring(0, 1).toUpperCase() + weather.getDescription().substring(1));
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM. yyyy");
        Date date = new Date();
        mLastUpdate.setText(String.format(new SimpleDateFormat("hh:mm aa").format(date).toString()));
        mDate.setText(dateFormat.format(date));
        final String day = dayFormat.format(date);
        mDay.setText(new StringBuilder().append(day.substring(0, 1).toUpperCase()).append(day.substring(1)).toString());
        currentWeatherView.setVisibility(View.VISIBLE);
        mTabs.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.VISIBLE);
        RelativeLayout groupedLayout = (RelativeLayout) findViewById(R.id.groupedLayout);
        groupedLayout.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fall_down));
    }

    private void attachIcon(ImageView mWeatherIcon, String iconCode) {
        iconCode = iconCode.substring(0, iconCode.length() - 1);
        switch (iconCode) {
            case "01":
                mWeatherIcon.setImageResource(R.drawable.art_clear);
                break;
            case "02":
                mWeatherIcon.setImageResource(R.drawable.art_light_clouds);
                break;
            case "03":
                mWeatherIcon.setImageResource(R.drawable.art_clouds);
                break;
            case "04":
                mWeatherIcon.setImageResource(R.drawable.art_clouds);
                break;
            case "09":
                mWeatherIcon.setImageResource(R.drawable.art_rain);
                break;
            case "10":
                mWeatherIcon.setImageResource(R.drawable.art_light_rain);
                break;
            case "11":
                mWeatherIcon.setImageResource(R.drawable.art_storm);
                break;
            case "13":
                mWeatherIcon.setImageResource(R.drawable.art_snow);
                break;
            case "50":
                mWeatherIcon.setImageResource(R.drawable.art_fog);
                break;
        }
    }
}