package com.example.erik.gps;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity implements LocationListener,OnMapReadyCallback{
    LocationManager mLocationManager;
    TextView textViewlatitud,textViewlongitud;
    Button buttonStart,buttonStop,buttonApi,buttonGuardar;
    GoogleMap mMap;

    private void startCapture(){
        Boolean enable= mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!enable){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            // Setting Dialog Title
            alertDialog.setTitle("GPS is settings");

            // Setting Dialog Message
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

            // On pressing Settings button
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });

            // on pressing cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog.create().show();
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void stopCapture(){
        if(mLocationManager!=null){
            mLocationManager.removeUpdates(this);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        textViewlatitud=(TextView)findViewById(R.id.val_latitud);
        textViewlongitud=(TextView)findViewById(R.id.val_longitud);
        buttonStart=(Button)findViewById(R.id.btn_start);
        buttonStop=(Button)findViewById(R.id.btn_stop);
        buttonApi=(Button)findViewById(R.id.btn_api);
        buttonGuardar=(Button)findViewById(R.id.btn_guardar);

        SupportMapFragment mapFragment=(SupportMapFragment)getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCapture();
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopCapture();
            }
        });
        buttonApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =new Intent(getApplicationContext(),googleApiActivity.class);
                startActivity(i);
            }
        });

        buttonGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    File file = getFileStreamPath("gps_1.txt");
                    if(file.exists()){
                        Log.d("EXIST", "El archivo existe");
                        OutputStreamWriter fout= new OutputStreamWriter(openFileOutput("gps_1.txt", Context.MODE_APPEND));
                        fout.write("Texto de prueba"+"\n");
                        fout.close();
                    }else {
                        Log.d("DON'T EXIST", "El archivo no existia");
                        OutputStreamWriter fout = new OutputStreamWriter(openFileOutput("gps_1.txt", Context.MODE_PRIVATE));
                        fout.write("Texto de prueba"+"\n");
                        fout.close();
                    }
                }
                catch (Exception ex)
                {
                    Log.e("Ficheros", "Error al escribir fichero a memoria interna");
                }
            }
        });
    }

    private void guardarPosition(Location l){
        try
        {
            OutputStreamWriter fout= new OutputStreamWriter(openFileOutput("gps_1.txt", Context.MODE_PRIVATE));
            String line=String.valueOf(l.getLatitude())+";"+String.valueOf(l.getLongitude());
            fout.write(line);
            fout.close();
        }
        catch (Exception ex)
        {
            Log.e("Ficheros", "Error al escribir fichero a memoria interna");
        }
    }

    public void readFile()throws IOException {
        try {
            InputStream isr = openFileInput("gps_1.txt");
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

    @Override
    protected void onResume(){
        super.onResume();
        try {
            readFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        textViewlatitud.setText(location.getLatitude() + "");
        textViewlongitud.setText(location.getLongitude() + "");
        guardarPosition(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(MainActivity.this,
                "Provider enabled: " + s, Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(MainActivity.this,
                "Provider disabled: " + s, Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        Location location=mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        LatLng lastLocation=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(lastLocation).title("Home?"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLocation));
        /*LatLng sydney=new LatLng(-34,151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    String getProviderName() {
        LocationManager locationManager = (LocationManager) this
                .getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW); // Chose your desired power consumption level.
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // Choose your accuracy requirement.
        String bestProvider=locationManager.getBestProvider(criteria,true);
        // Provide your criteria and flag enabledOnly that tells
        // LocationManager only to return active providers.
        Toast.makeText(this,"this provider"+bestProvider,Toast.LENGTH_SHORT).show();
        Log.d("BEST_PROVIDER",bestProvider);
        return bestProvider;
    }
}
