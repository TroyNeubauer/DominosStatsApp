package com.troy.ds;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
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

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private static final int GPS_REQ_CODE = 0;
	private static final String TAG = "dominos";

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == GPS_REQ_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				gpsFillImpl();
			} else {
				gpsFill(null);
			}
		}

	}


	public void gpsFillImpl()
	{
		LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		try
		{
			if (lm == null) throw new NullPointerException();
			Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location == null) throw new NullPointerException();

			((EditText) findViewById(R.id.latitude)).setText(String.format(Locale.getDefault(), "%f", location.getLongitude()));
			((EditText) findViewById(R.id.longitude)).setText(String.format(Locale.getDefault(), "%f", location.getLatitude()));
		}
		catch (NullPointerException e)
		{
			Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
		}
		catch (SecurityException e)
		{
			Toast.makeText(this, "Permissions failed. Code logic error", Toast.LENGTH_SHORT).show();
		}

	}

	public void gpsFill(View view)
	{
		Log.i(TAG, "Calling gpsfill");
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, GPS_REQ_CODE);
		}
		else
		{
			gpsFillImpl();
		}


	}
}
