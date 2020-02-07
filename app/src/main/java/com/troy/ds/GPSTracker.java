package com.troy.ds;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;


import java.util.ArrayList;

import static android.location.Criteria.ACCURACY_HIGH;
import static com.troy.ds.MainActivity.TAG;

public class GPSTracker extends Service implements LocationListener {

	// flag for GPS status
	private ArrayList<String> providers = null;

	Location location; // location

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public GPSTracker()
	{
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return START_STICKY;
	}


	@Override
	public void onCreate()
	{
	}

	@Override
	public void onDestroy()
	{

	}


	public double getLatitude()
	{
		return location.getLatitude();
	}

	public double getLongitude()
	{
		return location.getLongitude();
	}


	public boolean hasLocation()
	{
		return location != null;
	}

	public boolean hasProvider()
	{
		return providers.size() > 0;
	}

	/**
	 * Function to show settings alert dialog
	 * On pressing Settings button will lauch Settings Options
	 */
	public void showSettingsAlert(Context context) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	@Override
	public void onLocationChanged(Location newLocation)
	{
		if (location == null || newLocation.getProvider().equals(LocationManager.GPS_PROVIDER))
		{
			this.location = newLocation;
		}
	}

	@Override
	public void onProviderDisabled(String provider)
	{
	}

	@Override
	public void onProviderEnabled(String provider)
	{
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		final GPSBinder binder = new GPSBinder(this);
		new Thread(new Runnable() {
			@Override
			public void run() {
				//Looper.prepare();
				while (binder.getActivity() == null) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {

					}
				}
				Log.i(TAG, "Service got MainActivity instance");
				MainActivity activity = binder.getActivity();

				if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					Log.e(TAG, "Cannot run without fine location permissions!");
					showSettingsAlert(activity);
					stopSelf();
				}

				locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
				if (locationManager == null) {
					Toast.makeText(activity, "Failed to get location service provider!", Toast.LENGTH_LONG).show();
					Log.e(TAG, "Failed to get location service provider!");
					stopSelf();
				}

				Criteria criteria = new Criteria();
				criteria.setAccuracy(Criteria.ACCURACY_FINE);
				criteria.setHorizontalAccuracy(ACCURACY_HIGH);
				criteria.setAltitudeRequired(false);
				criteria.setBearingRequired(false);
				criteria.setCostAllowed(false);

				String provider = locationManager.getBestProvider(criteria, true);
				if (provider == null) {
					Toast.makeText(activity, "No GPS providers enabled! No GPS, Cellular or passive mode!", Toast.LENGTH_LONG).show();
					Log.e(TAG, "No GPS providers enabled! No GPS, Cellular or passive mode!");
					stopSelf();
				}

				locationManager.requestLocationUpdates(provider, 50, 1, GPSTracker.this, Looper.getMainLooper());
				location = locationManager.getLastKnownLocation(provider);
				Log.i(TAG, "GPStracker initalization complete");
				//Looper.loop();
				//Looper.myLooper().quitSafely();
			}
		}, "GPS Tracker Init Thread").start();

		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "Service unbound");
		return false;
	}

	static class GPSBinder extends Binder
	{
		GPSBinder(GPSTracker obj)
		{
			this.obj = obj;
		}

		private GPSTracker obj;
		private MainActivity activity;

		GPSTracker getObj()
		{
			return obj;
		}

		MainActivity getActivity()
		{
			return activity;
		}

		void setActivity(MainActivity activity)
		{
			this.activity = activity;
		}
	}


}
