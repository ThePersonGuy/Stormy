package stormy.com.stormy;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName(); // returns the SimpleName

    private CurrentWeather mCurrentWeather;
    private BroadcastReceiver broadcastReceiver;
    private GeoLocation mGeoLocation;

    private String coordinates;


    @BindView(R.id.timeLabel) TextView mTimeLabel;  //using butterKnife library for quick declare and assignment
    @BindView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @BindView(R.id.humidityValue) TextView mHumidityValue;
    @BindView(R.id.precipValue) TextView mPrecipValue;
    @BindView(R.id.summaryLabel) TextView mSummaryLabel;
    @BindView(R.id.iconImageView) ImageView mIconImageView;
    @BindView(R.id.refreshImageView) ImageView mRefreshImageView;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    @BindView(R.id.coordinates) TextView mCoord;



    @BindView(R.id.locationLabel) TextView mLocation;




    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {



                    String mCoordinates = (String) intent.getExtras().get("coordinates");
                    mCoord.setText(mCoordinates);




                }
            };
        }

        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null){
            unregisterReceiver(broadcastReceiver);
        }

        Intent i = new Intent(getApplicationContext(),GPS.class);
        stopService(i);
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ButterKnife.bind(this); //only need one line to bind all declaration (pretty useful)

        Intent i =new Intent(getApplicationContext(),GPS.class);
        startService(i);

        if(!runtime_permissions())




        mProgressBar.setVisibility(View.INVISIBLE);

       // mTemperatureLabel = (TextView) findViewById(R.id.temperatureLabel);



            mRefreshImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mCoord.getText().length() > 5) {


                    coordinates = mCoord.getText().toString();
                    getForecast(coordinates);
                    }else {
                        Toast.makeText(MainActivity.this, "Please Wait ...", Toast.LENGTH_SHORT).show();
                    }


                }
            });

           /* getForecast(coordinates);
            Log.d(TAG, "Main UI code is running!"); */

        /* while (mCoord.getText().length() < 5) {
            if (mCoord.getText().length() > 5){
                coordinates = mCoord.getText().toString();
                getForecast(coordinates);
            }

        } */


    }





    //https://maps.googleapis.com/maps/api/geocode/json?latlng=33.884302,-118.180547&key=AIzaSyC7ly1mw6kFPzbKwYoGZNsqbb8V0j_dfUI

    private void getForecast(String Coord) {

        String geoKey = "AIzaSyC7ly1mw6kFPzbKwYoGZNsqbb8V0j_dfUI";

        String geoUrl = "https://maps.googleapis.com/maps/api/geocode/json?latlng="+ Coord +"&key=" + geoKey;



        String apiDarkSkyKey = "687ece5e9ad63ed39a9247ba684411ce";

        String forecastUrl = "https://api.darksky.net/forecast/" + apiDarkSkyKey +
                "/" + Coord;
        if (isNetworkAvailable()) { //if isNetworkAvailable returns true, run code below

            toggleRefresh();


            OkHttpClient client = new OkHttpClient();



            Request requestDarksky = new Request.Builder()
                    .url(forecastUrl)
                    .build();


            Call callDarkSky = client.newCall(requestDarksky);




            callDarkSky.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {


                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });

                    try {

                        //if text is not input within body(), no text appears

                        String weatherJsonData = response.body().string();
                        Log.v(TAG, weatherJsonData );
                        if (response.isSuccessful()) {

                          mCurrentWeather = getCurrentWeatherDetails(weatherJsonData);
                            runOnUiThread(new Runnable() { // this method runs the bracketed code on the main thread
                                @Override
                                public void run() {

                                    updateDisplay1();  //this code runs in the background, must use runOnUiThread to put it on main thread
                                    // anything run in the user interface is part of the main thread
                                }
                            });


                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught:", e);
                    }
                    catch (JSONException e){
                        Log.e(TAG, "Exception caught:", e);

                    }

                }


            });

           Request requestGeoLocation = new Request.Builder()
                    .url(geoUrl)
                    .build();

            Call callGeoLocation = client.newCall(requestGeoLocation);


            callGeoLocation.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           // toggleRefresh();
                        }
                    });
                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //toggleRefresh();
                        }
                    });

                    try {

                        //if text is not input within body(), no text appears

                        String geoJsonData = response.body().string();
                        Log.v(TAG, geoJsonData );
                        if (response.isSuccessful()) {

                            mGeoLocation = getCurrentGeoDetails(geoJsonData);
                            runOnUiThread(new Runnable() { // this method runs the bracketed code on the main thread
                                @Override
                                public void run() {

                                    updateDisplay2();  //this code runs in the background, must use runOnUiThread to put it on main thread
                                    // anything run in the user interface is part of the main thread
                                }
                            });


                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught:", e);
                    }
                    catch (JSONException e){
                        Log.e(TAG, "Exception caught:", e);

                    }

                }


            });
            //toggleRefresh();
        }
        else {
            Toast.makeText(this, R.string.network_unavailable_message, Toast.LENGTH_LONG).show();
        }
    }

    private void toggleRefresh() {

        if (mProgressBar.getVisibility() == View.INVISIBLE) {

            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }
        else{
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay1() {
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText("At "+ mCurrentWeather.getFormattedTime()+" the temperature is");
        mHumidityValue.setText(mCurrentWeather.getHumidity()+""); // +"" turns code to string
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary() + "");


        Drawable drawable = ContextCompat.getDrawable(this, mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);

    }

    private void updateDisplay2() {

        mLocation.setText(mGeoLocation.getCity() + "," + mGeoLocation.getState());


    }


    private CurrentWeather getCurrentWeatherDetails(String JsonData) throws JSONException {
        JSONObject forecast = new JSONObject(JsonData);
       String timezone = forecast.getString("timezone"); //getString returns value
       Log.i(TAG,"From JSON:" + timezone);

        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setHumidity(currently.getDouble("humidity"));
        currentWeather.setTime(currently.getLong("time"));
        currentWeather.setIcon(currently.getString("icon"));
        currentWeather.setPrecipChance(currently.getDouble("precipProbability"));
        currentWeather.setSummary(currently.getString("summary"));
        currentWeather.setTemperature(currently.getDouble("temperature"));
        currentWeather.setTimeZone(timezone);

        Log.d(TAG, currentWeather.getFormattedTime());




        return currentWeather;

    }

    private GeoLocation getCurrentGeoDetails (String jsonData) throws JSONException {


        JSONObject geoLoc = new JSONObject(jsonData);

        JSONArray resultsArray = geoLoc.getJSONArray("results");


        JSONObject results = resultsArray.getJSONObject(0);

        JSONArray addressArray = results.getJSONArray("address_components");

        JSONObject addressObj3 = addressArray.getJSONObject(3);
        JSONObject addressObj5 = addressArray.getJSONObject(5);



        String mCity = addressObj3.getString("long_name");
        String mState = addressObj5.getString("long_name");


        GeoLocation geoLocation = new GeoLocation();

        geoLocation.setCity(mCity);
        geoLocation.setState(mState);




        return geoLocation;
    }

    private boolean isNetworkAvailable() { //this code block checks if network is working properly
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE); // casted into an object
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
            if (networkInfo != null && networkInfo.isConnected()){ // short hand, if the active network exists and is connected
            isAvailable = true;

        }
        return isAvailable;
    }

    private void alertUserAboutError() {

        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(),"error_dialog");


    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
               // enable_buttons();
            }else {
                runtime_permissions();
            }
        }
    }


}

