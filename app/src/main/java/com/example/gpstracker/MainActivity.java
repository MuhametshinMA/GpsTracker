package com.example.gpstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocListenerInterface{

    private TextView tvRestDistance, tvTotal, tvVelocity;
    private ProgressBar progressBar;
    private Location lastLocation;
    private LocationManager locationManager;
    private LocListener locListener;
    private int distance;
    private int totalDistance;
    private int restDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    public void OnLocationChanged(Location loc) {
        updateDistance(loc);
    }
    private void init() {
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        locListener = new LocListener();
        locListener.setLocListenerInterface(this);
        tvRestDistance = findViewById(R.id.tvRestDistance);
        tvTotal = findViewById(R.id.tvTotal);
        tvVelocity = findViewById(R.id.tvVelocity);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(1000);
        checkPermissions();
    }
    public void onClickDistance(View view) {
        showDialog();
    }

    private void updateDistance(Location loc) {
        if (loc.hasSpeed() && lastLocation != null) {
            if (totalDistance < distance) {
                totalDistance += lastLocation.distanceTo(loc);
            }
            if (restDistance > 0) {
                restDistance -= lastLocation.distanceTo(loc);
            }
            progressBar.setProgress(totalDistance);
        }
        lastLocation = loc;
        tvRestDistance.setText(String.valueOf(restDistance));
        tvTotal.setText(String.valueOf(totalDistance));
        tvVelocity.setText(String.valueOf(loc.getSpeed()));
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_title);
        ConstraintLayout cl = (ConstraintLayout) getLayoutInflater().inflate(R.layout.dialog_layout, null);
        builder.setView(cl);
        builder.setPositiveButton(R.string.dialog_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog alertDialog = (AlertDialog) dialogInterface;
                EditText editText = alertDialog.findViewById(R.id.etDistance);
                if (editText != null) {
                    if (!editText.getText().toString().equals("")) {
                        setDistance(editText.getText().toString());
                    }
                }
            }
        });
        builder.show();
    }

    public void setDistance(String distance) {
        progressBar.setMax(Integer.parseInt(distance));
        restDistance = Integer.parseInt(distance);
        this.distance = Integer.parseInt(distance);
        tvRestDistance.setText(distance);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults[0] == RESULT_OK) {
            checkPermissions();
        } else {
            Toast.makeText(this, "No GPS permissions", Toast.LENGTH_LONG).show();
        }
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION},
                    100);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    2,
                    1,
                    locListener);
        }
    }
}