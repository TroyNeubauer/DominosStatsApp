package com.troy.ds;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{

	private GPSTracker gps;
	public boolean running = true;

	public static final File SAVE_DIR = new File(Environment.getExternalStorageDirectory(), "Dominos App");

	public static final int ALL_REQ_CODE = 8000;

	public static final String TAG = "dominos";

	private void requestPermissions(String... perms)
	{
		boolean request = false;
		for (String perm : perms)
		{
			if (ContextCompat.checkSelfPermission(getBaseContext(), perm) != PackageManager.PERMISSION_GRANTED)
			{
				request = true;
				break;
			}
		}
		if (request)
		{
			ActivityCompat.requestPermissions(this, perms, ALL_REQ_CODE);
		}

	}


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

		if (!SAVE_DIR.exists())
		{
			if (!SAVE_DIR.mkdirs())
			{
				throw new RuntimeException("Failed to create dir " + SAVE_DIR.toString());
			}
		}
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Log.i(TAG, "On destroy called");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();


		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if (requestCode != ALL_REQ_CODE) throw new RuntimeException("Unknown req code: " + requestCode);
		for (int i = 0; i < permissions.length; i++)
		{
			if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
			{
				Toast.makeText(this, "You must accept the " + permissions[i] +" permission", Toast.LENGTH_LONG).show();
				Log.i(TAG, "Could not get " + permissions[i] + "permission. Quitting application");
				stopService(new Intent(getBaseContext(), GPSTracker.class));
				finish();
			}

		}

	}


	public void gpsFill(View view)
	{
		if (gps == null)
		{
			showSessionNotActiveMessage();
		}
		else if (gps.hasLocation())
		{
			((EditText) findViewById(R.id.latitude)).setText(String.format(Locale.getDefault(), "%f", gps.getLatitude()));
			((EditText) findViewById(R.id.longitude)).setText(String.format(Locale.getDefault(), "%f", gps.getLongitude()));
		}
		else
		{
			Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
		}
	}

	public void changeStatusClicked(View view)
	{
		if (gps == null)
		{
			startService();
		}
		else
		{
			stopService();
		}

	}

	private void startService()
	{
		Log.i(TAG, "Starting service");

		bindService(new Intent(getBaseContext(), GPSTracker.class), new ServiceConnection()
		{
			@Override
			public void onServiceConnected(ComponentName name, IBinder service)
			{
				if (!(service instanceof GPSTracker.GPSBinder)) throw new RuntimeException("Service needs to return an instance of GPSBinder");
				GPSTracker.GPSBinder gpsBinder = (GPSTracker.GPSBinder) service;
				gpsBinder.setActivity(MainActivity.this);
				Log.i(TAG, "Service connected");

				gps = (gpsBinder).getObj();
				((TextView) findViewById(R.id.status)).setText("Session Active");
				((Button) findViewById(R.id.change_session_status)).setText("Stop");
			}

			@Override
			public void onServiceDisconnected(ComponentName name)
			{
				stopService();
			}
		}, BIND_AUTO_CREATE);

	}

	private void stopService()
	{
		Log.i(TAG, "Stopping service");
		stopService(new Intent(getBaseContext(), GPSTracker.class));
		((TextView) findViewById(R.id.status)).setText("Session Not Active");
		((Button) findViewById(R.id.change_session_status)).setText("Start");
		gps = null;

	}

	private void showSessionNotActiveMessage()
	{
		Toast.makeText(this, "Session is not active. Click \"Start\" first", Toast.LENGTH_LONG).show();
	}

}
