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

import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



import java.util.Locale;

public class MainActivity extends AppCompatActivity
{

	private GPSTracker gps;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_REQ_CODE);
		}

		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

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

	public static final int GPS_REQ_CODE = 0;
	public static final String TAG = "dominos";

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if (requestCode == GPS_REQ_CODE)
		{
			if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
			{
				Toast.makeText(this, "You must accept the permissions", Toast.LENGTH_SHORT).show();
				Log.i(TAG, "Could not get permissions. Quitting application");
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

		startService(new Intent(getBaseContext(), GPSTracker.class));
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
		}, BIND_ABOVE_CLIENT);

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
