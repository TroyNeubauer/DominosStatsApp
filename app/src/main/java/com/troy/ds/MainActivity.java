package com.troy.ds;

import android.Manifest;
import android.content.pm.PackageManager;
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

	private GPSTracker gps;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		gps = new GPSTracker(this);

		if (!gps.canGetLocation()) {
			gps.showSettingsAlert();
		}

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

	public static final int GPS_REQ_CODE = 0;
	public static final String TAG = "dominos";

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

		if (gps.canGetLocation()) {
			((EditText) findViewById(R.id.latitude)).setText(String.format(Locale.getDefault(), "%f", location.getLongitude()));
			((EditText) findViewById(R.id.longitude)).setText(String.format(Locale.getDefault(), "%f", location.getLatitude()));
		}
		else
		{


		}
		catch (NullPointerException e)
		{
			Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
		}
		catch (SecurityException e)
		{

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

	public void startClicked(View view)
	{
	}


}
