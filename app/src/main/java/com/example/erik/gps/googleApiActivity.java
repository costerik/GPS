package com.example.erik.gps;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;

public class googleApiActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest mLocationRequest;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected Boolean mRequestingLocationUpdates;

    protected Location mCurrentLocation;
    protected Button mStartUpdatesButton;
    protected Button mStopUpdatesButton;
    protected Button mSavePosition;
    protected TextView mLastUpdateTimeTextView;
    protected TextView mLatitudeTextView;
    protected TextView mLongitudeTextView;
    protected TextView mSpeedTextView;

    protected String mLastUpdateTime;

    private static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_api);
        mStartUpdatesButton = (Button) findViewById(R.id.buttonStart);
        mStopUpdatesButton = (Button) findViewById(R.id.buttonStop);
        mSavePosition=(Button)findViewById(R.id.buttonGuardar);
        mLatitudeTextView = (TextView) findViewById(R.id.editTextLat);
        mLongitudeTextView = (TextView)findViewById(R.id.editTextLong);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.editTextDate);
        mSpeedTextView = (TextView)findViewById(R.id.editTextSpeed);

        mStartUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startUpdatesButtonHandler();
            }
        });

        mStopUpdatesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopUpdatesButtonHandler();
            }
        });

        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        mSavePosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCurrentLocation!=null) {
                    guardarPosition(mCurrentLocation,"gps_2.txt");
                }else{
                    Toast.makeText(getApplicationContext(), "No se ha logrado establecer la conexión", Toast.LENGTH_SHORT).show();
                }
            }
        });



        buildGoogleApiClient();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        Log.d("AndroidSensors","Connecting");

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
        try {
            readFile("gps_2.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("AndroidSensors", "onConnected");
        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI();
        }

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(this.getClass().getSimpleName(), "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
        Log.d("AndroidSensors", "onLocationChanged");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(this.getClass().getSimpleName(), "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    private void guardarPosition(Location l,String fileName){
        try
        {
            File file = getFileStreamPath(fileName);
            String line=String.valueOf(l.getLatitude())+";"+String.valueOf(l.getLongitude())+";"+String.valueOf(l.getSpeed()*3600/1000)+";"+String.valueOf(mLastUpdateTime);
            if(file.exists()){
                Log.d("EXIST", "El archivo existe");
                OutputStreamWriter fout= new OutputStreamWriter(openFileOutput(fileName, Context.MODE_APPEND));
                fout.write(line+"\n");
                fout.close();
            }else {
                Log.d("DIDN'T EXIST", "El archivo no existia");
                OutputStreamWriter fout = new OutputStreamWriter(openFileOutput(fileName, Context.MODE_PRIVATE));
                fout.write(line+"\n");
                fout.close();
            }
        }catch (Exception ex){
            Log.e("Ficheros", "Error al escribir fichero a memoria interna");
        }
    }

    private void readFile(String fileName)throws IOException {
        try {
            InputStream isr = openFileInput(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(isr));
            String line = br.readLine();
            while (line != null) {
                Log.i("PALABRA", line);
                line = br.readLine();
            }
            br.close();
        } catch (Exception ex) {
            Log.e("Ficheros", "Error al leer fichero desde memoria interna");
        }
    }

    private void updateUI() {
        if (mCurrentLocation != null) {
            mLatitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
            mLongitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
            mLastUpdateTimeTextView.setText(mLastUpdateTime);
            mSpeedTextView.setText(((mCurrentLocation.getSpeed()*3600)/1000)+"");
        }
    }

    protected void startLocationUpdates() {
        if (!mGoogleApiClient.isConnected()) {
            Log.d("AndroidSensors", "startLocationUpdates not connected");
            mGoogleApiClient.connect();
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
        } else {
            Log.d("AndroidSensors","startLocationUpdates connected");
            //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        }

    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void setButtonsEnabledState() {
        if (mRequestingLocationUpdates) {
            mStartUpdatesButton.setEnabled(false);
            mStopUpdatesButton.setEnabled(true);
        } else {
            mStartUpdatesButton.setEnabled(true);
            mStopUpdatesButton.setEnabled(false);
        }
    }

    public void stopUpdatesButtonHandler() {
        if (mRequestingLocationUpdates) {
            mRequestingLocationUpdates = false;
            setButtonsEnabledState();
            stopLocationUpdates();
        }
    }

    public void startUpdatesButtonHandler() {
        if (!mRequestingLocationUpdates) {
            mRequestingLocationUpdates = true;
            setButtonsEnabledState();
            startLocationUpdates();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
        Log.d("AndroidSensors", "buildGoogleApiClient");
    }
}
