package stormy.com.stormy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
    //private GPS GPSinfo;


    @BindView(R.id.timeLabel) TextView mTimeLabel;  //using butterKnife library for quick declare and assignment
    @BindView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @BindView(R.id.humidityValue) TextView mHumidityValue;
    @BindView(R.id.precipValue) TextView mPrecipValue;
    @BindView(R.id.summaryLabel) TextView mSummaryLabel;
    @BindView(R.id.iconImageView) ImageView mIconImageView;
    @BindView(R.id.refreshImageView) ImageView mRefreshImageView;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      // GPSinfo = new GPS();


        ButterKnife.bind(this); //only need one line to bind all declaration (pretty useful)


        mProgressBar.setVisibility(View.INVISIBLE);

       // mTemperatureLabel = (TextView) findViewById(R.id.temperatureLabel);

        final double latitude = 37.8267;
        final double longitude = -122.4233;

        mRefreshImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){



                getForecast(latitude,longitude);



            }
        });

        getForecast(latitude, longitude);
            Log.d(TAG, "Main UI code is running!");

    }

    private void getForecast(double latitude,double longitude ) {
        String apiKey = "687ece5e9ad63ed39a9247ba684411ce";


        String forecastUrl = "https://api.darksky.net/forecast/" + apiKey +
                "/" + latitude + "," + longitude;
        if (isNetworkAvailable()) { //if isNetworkAvailable returns true, run code below

            toggleRefresh();


            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = client.newCall(request);


            call.enqueue(new Callback() {
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

                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData );
                        if (response.isSuccessful()) {

                          mCurrentWeather = getCurrentDetails(jsonData);
                            runOnUiThread(new Runnable() { // this method runs the bracketed code on the main thread
                                @Override
                                public void run() {

                                    updateDisplay();  //this code runs in the background, must use runOnUiThread to put it on main thread
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

    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getTemperature() + "");
        mTimeLabel.setText("At "+ mCurrentWeather.getFormattedTime()+" the temperature is");
        mHumidityValue.setText(mCurrentWeather.getHumidity()+""); // +"" turns code to string
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getSummary() + "");

        Drawable drawable = ContextCompat.getDrawable(this, mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);

    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
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


}

