package org.iw11.driver.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.iw11.driver.BackgroundGPS;
import org.iw11.driver.R;
import org.iw11.driver.network.RestServiceFactory;
import org.iw11.driver.network.TokenManager;
import org.iw11.driver.network.model.BusRoute;
import org.iw11.driver.network.model.BusStation;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackingActivity extends AppCompatActivity {

    private static final String TAG = TrackingActivity.class.getName();

    private static final int REQUEST_LOCATION_PERMISSIONS = 100;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private Handler handler = new Handler();

    private TokenManager tokenManager;

    private TextView activityCaption;
    private TextView routeView;
    private Button openMapsButton;
    private Button completeRideButton;

    private BusRoute currentRoute = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tokenManager = new TokenManager(getApplicationContext());

        openMapsButton = findViewById(R.id.tracking_open_button);
        openMapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMap();
            }
        });

        completeRideButton = findViewById(R.id.tracking_complete_button);
        completeRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postRouteComplete();
            }
        });

        activityCaption = findViewById(R.id.tracking_caption);
        routeView = findViewById(R.id.tracking_route);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tracking_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        checkPermissions();
        setCurrentRoute(null);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                pollRoute();
            }
        }, 1, 2, TimeUnit.SECONDS);
    }

    @Override
    public void onStop() {
        super.onStop();
        executor.shutdownNow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startServices();
                } else {
                    Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void logout() {
        executor.shutdownNow();

        tokenManager.setToken(null);
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);

        Intent intentService = new Intent(getApplicationContext(), BackgroundGPS.class);
        stopService(intentService);
    }

    private void checkPermissions() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if (!(ActivityCompat.shouldShowRequestPermissionRationale(
                    TrackingActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION))) {
                ActivityCompat.requestPermissions(
                        TrackingActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSIONS);
            }
        } else {
            startServices();
        }
    }

    private void startServices() {
        Intent intent = new Intent(getApplicationContext(), BackgroundGPS.class);
        intent.putExtra("token", tokenManager.getToken());
        startService(intent);
    }

    private void setCurrentRoute(BusRoute route) {
        if (route != null && currentRoute == route)
            return;

        this.currentRoute = route;
        if (route == null) {
            openMapsButton.setEnabled(false);
            openMapsButton.setVisibility(View.INVISIBLE);
            completeRideButton.setEnabled(false);
            completeRideButton.setVisibility(View.INVISIBLE);
            routeView.setText("");
            routeView.setVisibility(View.INVISIBLE);
            activityCaption.setText("Waiting for the route...");
        } else {
            openMapsButton.setEnabled(true);
            openMapsButton.setVisibility(View.VISIBLE);
            completeRideButton.setEnabled(true);
            completeRideButton.setVisibility(View.VISIBLE);
            routeView.setVisibility(View.VISIBLE);
            activityCaption.setText("Follow the route");

            StringBuilder builder = new StringBuilder();
            for (BusStation station : route.getStations()) {
                builder.append(station.getName());
                builder.append("\n");
            }
            routeView.setText(builder.toString());
        }
    }

    private void pollRoute() {
        if (currentRoute != null) {
            return;
        }
        Call<BusRoute> call = RestServiceFactory.getApiService().getRoute(tokenManager.getToken());
        call.enqueue(new Callback<BusRoute>() {
            @Override
            public void onResponse(Call<BusRoute> call, Response<BusRoute> response) {
                if (response.code() == 200) {
                    final BusRoute route = response.body();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            setCurrentRoute(route);
                        }
                    });
                    return;
                }
                if (response.code() != 204) {
                    Log.e(TAG,"Error connecting to server, status code: " + response.code());
                    Toast.makeText(getApplicationContext(), "Unable to get route", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<BusRoute> call, Throwable t) {
                Log.e(TAG, "Error connecting to server", t);
                Toast.makeText(getApplicationContext(), "Error connecting to server", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void postRouteComplete() {
        completeRideButton.setEnabled(false);
        Call<Void> call = RestServiceFactory.getApiService().postRouteComplete(tokenManager.getToken());
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.code() != 200) {
                    Log.e(TAG,"Error connecting to server, status code: " + response.code());
                    Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_LONG).show();
                    completeRideButton.setEnabled(true);
                    return;
                }
                setCurrentRoute(null);
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Error connecting to server", t);
                Toast.makeText(getApplicationContext(), "Error connecting to server", Toast.LENGTH_LONG).show();
                completeRideButton.setEnabled(true);
            }
        });
    }

    public void openMap() {
        if (currentRoute == null) {
            return;
        }
        List<BusStation> stations = currentRoute.getStations();

        StringBuilder builder = new StringBuilder();
        builder.append("http://maps.google.com/maps/dir/?api=1&saddr=");
        builder.append(stations.get(0).getLocation().getLatitude());
        builder.append(",");
        builder.append(stations.get(0).getLocation().getLongitude());
        builder.append("&daddr=");
        builder.append(stations.get(1).getLocation().getLatitude());
        builder.append(",");
        builder.append(stations.get(1).getLocation().getLongitude());
        for (int i = 2; i < stations.size(); i++) {
            builder.append("+to:");
            builder.append(stations.get(i).getLocation().getLatitude());
            builder.append(",");
            builder.append(stations.get(i).getLocation().getLongitude());
        }
        builder.append("&travelmode=driving");


        //String uri = "http://maps.google.com/maps/dir/?api=1&saddr=55.697469,37.359772&daddr=55.690140,37.348696+to:55.684135,37.340977&travelmode=driving";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(builder.toString()));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        startActivity(intent);
    }
}
