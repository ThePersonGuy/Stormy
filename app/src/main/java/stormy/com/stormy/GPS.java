package stormy.com.stormy;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

/**
 * Created by filipp on 6/16/2016.
 */
public class GPS extends Service {

    private LocationListener listener;
    private LocationManager locationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                String Long = Double.toString(location.getLongitude());
                String Lat = Double.toString(location.getLatitude());


                Intent i = new Intent("location_update");
                i.putExtra("coordinates",Lat +","+ Long);
                sendBroadcast(i);

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,3000,0,listener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }
}



