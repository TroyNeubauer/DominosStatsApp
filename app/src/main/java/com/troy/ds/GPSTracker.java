package com.troy.ds;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;


import com.esotericsoftware.kryo.io.Output;
import com.troy.core.Core;
import com.troy.core.PointData;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static com.troy.ds.MainActivity.TAG;

public class GPSTracker extends Service implements LocationListener {

	// flag for GPS status
	private ArrayList<String> providers = new ArrayList<>();

	Location location; // location

	// Declaring a Location Manager
	protected LocationManager locationManager;

	private volatile boolean running = true;
	private Thread trackerThread;

	public GPSTracker()
	{
	}



	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		return START_REDELIVER_INTENT;
	}


	@Override
	public void onCreate()
	{
	}

	@Override
	public void onDestroy()
	{
		cleanup();
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
			Log.i(TAG + GPSTracker.class.getName(), "Location updated from: " + newLocation.getProvider());
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
				Looper.prepare();

				while (binder.getActivity() == null)
				{
					try
					{
						Thread.sleep(10);
					} catch (InterruptedException e) {

					}
				}
				Log.i(TAG + GPSTracker.class.getName(), "Service got MainActivity instance");
				final MainActivity activity = binder.getActivity();

				if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
				{
					Log.e(TAG, "Cannot run without fine location permissions!");
					showSettingsAlert(activity);
					stopSelf();
				}

				locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
				if (locationManager == null)
				{
					Toast.makeText(activity, "Failed to get location service provider!", Toast.LENGTH_LONG).show();
					Log.e(TAG + GPSTracker.class.getName(), "Failed to get location service provider!");
					stopSelf();
				}

				// getting GPS status
				boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

				// getting network status
				boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
				// First get location from Network Provider
				if (isNetworkEnabled)
				{
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 1.0f, GPSTracker.this);
					providers.add(LocationManager.NETWORK_PROVIDER);
					Log.d(MainActivity.TAG, "Network");
					location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null)
					{
						Toast.makeText(activity, "Failed to get initial wifi location", Toast.LENGTH_LONG).show();
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled)
				{
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 1.0f, GPSTracker.this);
					providers.add(LocationManager.GPS_PROVIDER);
					Log.d(MainActivity.TAG, "GPS Enabled");
					location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if (location != null)
					{
						Toast.makeText(activity, "Failed to get initial wifi location", Toast.LENGTH_LONG).show();
					}

				}
				Log.i(TAG, "GPStracker initalization complete. Using provider " + providers.toString());
				while(running)
				{
					Log.i(TAG, "Looping...");
					Looper.loop();
				}
				Looper.myLooper().quitSafely();
				Log.i(TAG, "Loop end");
				locationManager.requestLocationUpdates(provider, 50, 1, GPSTracker.this, Looper.getMainLooper());
				location = locationManager.getLastKnownLocation(provider);

				trackerThread = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						DateTimeFormatter formatter = DateTimeFormat.fullDateTime();
						try
						{
							File outFile = new File( SAVE_DIR, "Pos-" + formatter.print(DateTime.now()));
							Output out = new Output(new FileOutputStream(outFile));
							Log.i(TAG, "Opened GPS log file");

							int pointCount = 0;
							while (running)
							{
								pointCount++;
								PointData data = new PointData();
								data.time = DateTime.now();
								data.lat = getLatitude();
								data.lng = getLongitude();
								Core.KRYO.get().writeObject(out, data);

								if (pointCount % 30 == 0)
								{
									out.flush();

								}

								final int updateFrequency = 5 * 1000;

								//Break up the wait into small waits of 10ms so quitting never takes long
								int time = 0;
								while (time < updateFrequency && running)
								{
									try
									{
										Thread.sleep(10);
										time += 10;
									}
									catch (InterruptedException e)
									{
										throw new RuntimeException(e);
									}
								}
							}
							out.flush();
							Log.i(TAG + GPSTracker.class.getName(), "Saved GPS log file");

						}
						catch (IOException e)
						{
							throw new RuntimeException(e);
						}

						Log.i(TAG + GPSTracker.class.getName(), "GPS log thread exiting");
					}

				});
				trackerThread.start();

				Log.i(TAG + GPSTracker.class.getName(), "GPStracker initalization complete");
			}
		}, "GPS Tracker Init Thread").start();

		return binder;
	}


	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "Service unbound");
		return false;
	}

	private void cleanup()
	{
		running = false;
		locationManager.removeUpdates(this);
		try
		{
			if (trackerThread != null) trackerThread.join();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
		Log.i(TAG + GPSTracker.class.getName(), "Exiting service");
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
