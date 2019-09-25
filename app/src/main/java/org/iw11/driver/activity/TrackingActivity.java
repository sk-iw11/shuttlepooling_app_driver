package org.iw11.driver.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.iw11.driver.BackgroundGPS;
import org.iw11.driver.R;
import org.iw11.driver.network.TokenManager;

public class TrackingActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSIONS = 100;

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tokenManager = new TokenManager(getApplicationContext());
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

}
