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
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.esotericsoftware.kryo.io.Output;
import com.troy.core.Core;
import com.troy.core.DeliveryData;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{

	private GPSTracker gps;
	private Output out;
	public boolean running = true;


	public static final File SAVE_DIR = new File(Environment.getExternalStorageDirectory(), "Dominos App");

	public static final int ALL_REQ_CODE = 8000;

	public static final String TAG = "dominos";

	public static DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.dateTime();

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

		((SeekBar) findViewById(R.id.age)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				((TextView) findViewById(R.id.age_text)).setText("Age " + progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		File outFile = new File(SAVE_DIR, "Deliveries-" + DATE_TIME_FORMATTER.print(DateTime.now()) + ".kryo");
		try {
			out = new Output(new FileOutputStream(outFile));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

	}

	public void onSubmit(View view)
	{
		if (((EditText) findViewById(R.id.latitude)).getText().length() == 0)
		{
			Toast.makeText(this, "You must fill in latitude first!", Toast.LENGTH_SHORT).show();
			return;
		}

		int checkedID = ((RadioGroup) findViewById(R.id.gender)).getCheckedRadioButtonId();
		if (checkedID == -1)
		{
			Toast.makeText(this, "You must check a gender", Toast.LENGTH_SHORT).show();
			return;
		}

		if (((EditText) findViewById(R.id.total_cost)).getText().length() == 0)
		{
			Toast.makeText(this, "You must complete the order total first", Toast.LENGTH_SHORT).show();
			return;
		}

		if (((EditText) findViewById(R.id.tip)).getText().length() == 0)
		{
			Toast.makeText(this, "You must complete the tip box first", Toast.LENGTH_SHORT).show();
			return;
		}

		DeliveryData data = new DeliveryData();
		data.lat = Double.parseDouble(((EditText) findViewById(R.id.latitude)).getText().toString());
		data.lng = Double.parseDouble(((EditText) findViewById(R.id.longitude)).getText().toString());

		data.male = checkedID == R.id.male_check;

		data.tip = Double.parseDouble(((EditText) findViewById(R.id.tip)).getText().toString());
		data.orderTotal = Double.parseDouble(((EditText) findViewById(R.id.total_cost)).getText().toString());

		data.age = ((SeekBar) findViewById(R.id.age)).getProgress();
		data.time = DateTime.now();

		Core.KRYO.get().writeClassAndObject(out, data);
		Toast.makeText(this, "Saved data", Toast.LENGTH_SHORT).show();
		out.flush();

		((EditText) findViewById(R.id.latitude)).getText().clear();
		((EditText) findViewById(R.id.longitude)).getText().clear();

		((RadioGroup) findViewById(R.id.gender)).clearCheck();

		((EditText) findViewById(R.id.tip)).getText().clear();
		((EditText) findViewById(R.id.total_cost)).getText().clear();

		((SeekBar) findViewById(R.id.age)).setProgress(100);
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Log.i(TAG, "On destroy called");

		Log.i(TAG, "Closing file");
		out.close();
		stopService();
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
		finish();
	}

	private void showSessionNotActiveMessage()
	{
		Toast.makeText(this, "Session is not active. Click \"Start\" first", Toast.LENGTH_LONG).show();
	}

}
