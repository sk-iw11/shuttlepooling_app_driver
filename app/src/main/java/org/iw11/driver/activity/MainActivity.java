package org.iw11.driver.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;


import org.iw11.driver.BackgroundGPS;
import org.iw11.driver.R;
import org.iw11.driver.network.TokenManager;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Location mLocation;
    private com.google.android.gms.location.LocationListener listener;
    private Button button_connect;
    //Button btn_start;
    private static final int REQUEST_PERMISSIONS = 100;
    boolean boolean_permission;
    //TextView tv_latitude, tv_longitude, tv_address,tv_area,tv_locality;
    SharedPreferences mPref;
    SharedPreferences.Editor medit;
    Double latitude,longitude;
    Geocoder geocoder;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenManager = new TokenManager(getApplicationContext());

        geocoder = new Geocoder(this, Locale.getDefault());
        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        medit = mPref.edit();

        button_connect=findViewById(R.id.button_navigator);
        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "http://maps.google.com/maps/dir/?api=1&saddr=55.698128,37.359803&daddr=55.690135,37.348110+to:55.684283,37.341396&travelmode=driving";
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                intent.putExtra("token", tokenManager.getToken());
                startActivity(intent);
            }
        });

/*
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (boolean_permission) {

                    if (mPref.getString("service", "").matches("")) {
                        medit.putString("service", "service").commit();

                        Intent intent = new Intent(getApplicationContext(), BackgroundGPS.class);
                        startService(intent);

                    } else {
                        Toast.makeText(getApplicationContext(), "Service is already running", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enable the gps", Toast.LENGTH_SHORT).show();
                }

            }
        });*/

        Button button_disconnect=findViewById(R.id.button_connect);
        button_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (boolean_permission) {

                    Intent intent = new Intent(getApplicationContext(), BackgroundGPS.class);
                    startService(intent);

                } else {
                    Toast.makeText(getApplicationContext(), "Please enable the gps", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fn_permission();
    }

    private void fn_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {


            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION

                        },
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;

                } else {
                    Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            latitude = Double.valueOf(intent.getStringExtra("latutide"));
            longitude = Double.valueOf(intent.getStringExtra("longitude"));
            String msg = "Updated Location: " +
                    (latitude) + "," +
                    (longitude);
            //mLatitudeTextView.setText(String.valueOf(location.getLatitude()));
            //mLongitudeTextView.setText(String.valueOf(location.getLongitude() ));
            //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            // You can now create a LatLng Object for use with maps
            Log.i(TAG, msg);
            LatLng latLng = new LatLng(latitude, longitude);
            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                //String cityName = addresses.get(0).getAddressLine(0);
                //String stateName = addresses.get(0).getAddressLine(1);
                //String countryName = addresses.get(0).getAddressLine(2);

                //tv_area.setText(addresses.get(0).getAdminArea());
                //tv_locality.setText(stateName);
                //tv_address.setText(countryName);


            } catch (IOException e1) {
                e1.printStackTrace();
            }


            //tv_latitude.setText(latitude+"");
            //tv_longitude.setText(longitude+"");
            //tv_address.getText();


        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(BackgroundGPS.str_receiver));

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }


}

/*

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = "MainActivity";
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private Button button_connect;

    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 2 * 1000;  // 10 secs
private long FASTEST_INTERVAL = 2000; // 2 sec


    private LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //mLatitudeTextView = (TextView) findViewById((R.id.latitude_textview));
        //mLongitudeTextView = (TextView) findViewById((R.id.longitude_textview));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        checkLocation(); //check whether location service is enable or not in your  phone

        button_connect=findViewById(R.id.button_connect);
        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = "http://maps.google.com/maps/dir/?api=1&saddr=55.698128,37.359803&daddr=55.690135,37.348110+to:55.684283,37.341396&travelmode=driving";
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        } else {
            Log.i(TAG, "Permissions granted!");

            startLocationUpdates();

            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if(mLocation == null){
                startLocationUpdates();
            }
            if (mLocation != null) {

                // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
                //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
            } else {
                Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permissions granted!");

            startLocationUpdates();

            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if(mLocation == null){
                startLocationUpdates();
            }
            if (mLocation != null) {

                // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
                //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
            } else {
                Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.i(TAG, "Permissions not granted!");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        //mLatitudeTextView.setText(String.valueOf(location.getLatitude()));
        //mLongitudeTextView.setText(String.valueOf(location.getLongitude() ));
        //Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        Log.i(TAG, msg);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}

*/