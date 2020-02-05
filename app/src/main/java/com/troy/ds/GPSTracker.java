package com.troy.ds;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class GPSTracker extends Service implements LocationListener {

	private final Context mContext;
	private final Activity mActivity;

	// flag for GPS status
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// flag for GPS status
	boolean hasLocationProvider = false;
	boolean hasPermissions = false;

	Location location; // location

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 500; // 0.5 seconds

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public GPSTracker(Context context, Activity activity) {
		this.mContext = context;
		this.mActivity = activity;
		initLocationUpdates();
	}

	public void initLocationUpdates()
	{
		locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
		if (locationManager == null)
		{
			Toast.makeText(this, "Failed to get location service provider!", Toast.LENGTH_LONG).show();
			return;
		}

		// getting GPS status
		isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// getting network status
		isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!isGPSEnabled && !isNetworkEnabled)
		{
			Toast.makeText(this, "No GPS providers enabled! No GPS and no Cellular", Toast.LENGTH_LONG).show();
			return;
		}
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

			this.hasPermissions = true;
			// First get location from Network Provider
			if (isNetworkEnabled) {
				this.hasLocationProvider = true;
				locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
				Log.d(MainActivity.TAG, "Network");
				location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (location != null) {
					Toast.makeText(this, "Failed to get initial wifi location", Toast.LENGTH_LONG).show();
				}
			}
			// if GPS Enabled get lat/long using GPS Services
			if (isGPSEnabled) {
				this.hasLocationProvider = true;
				if (location == null) {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d(MainActivity.TAG, "GPS Enabled");
					location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if (location != null) {
						Toast.makeText(this, "Failed to get initial wifi location", Toast.LENGTH_LONG).show();
					}
				}
			}
		}
		else
		{
			ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.GPS_REQ_CODE);
		}

	}

	/**
	 * Stop using GPS listener
	 * Calling this function will stop using GPS in your app
	 */
	public void disableUpdates() {
		if (locationManager != null) {
			locationManager.removeUpdates(GPSTracker.this);
		}
	}

	public double getLatitude() {
		return location.getLatitude();
	}

	public double getLongitude() {
		return location.getLongitude();
	}


	public boolean hasLocation() {
		return location != null;
	}

	public boolean isHasPermissions() {
		return hasPermissions;
	}

	public void setHasLocationProvider(boolean hasLocationProvider) {
		this.hasLocationProvider = hasLocationProvider;
	}

	/**
	 * Function to show settings alert dialog
	 * On pressing Settings button will lauch Settings Options
	 */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location location) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
}