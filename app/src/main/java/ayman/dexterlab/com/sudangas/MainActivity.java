package ayman.dexterlab.com.sudangas;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    boolean doubleBackToExitPressedOnce = false;

    LocationManager mLocationManager;
    private GoogleMap mMap;
    LatLng userLatLng;
    private FusedLocationProviderClient mFusedLocationClient;

    LatLng sdn = new LatLng(15.5007, 32.5599);
    String finalAddress;
    private String result;
    FloatingActionButton serachButton;

    private TextView textViewLine;
    private CheckBox checkBoxBenzene;
    private CheckBox checkBoxGasoline;
    private Button buttonOK;
    private Button buttonCancel;
    int benzeneVal,gasolineVal = 0;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        serachButton = findViewById(R.id.fab);
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.station_search);
        dialog.setCancelable(false);

        textViewLine = dialog.findViewById(R.id.station_line);
        checkBoxBenzene = dialog.findViewById(R.id.checkbox_benzene);
        checkBoxGasoline = dialog.findViewById(R.id.checkbox_gasoline);
        buttonOK = dialog.findViewById(R.id.ok);
        buttonCancel = dialog.findViewById(R.id.cancel);

        serachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
        checkBoxBenzene.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    benzeneVal = 1;
                }
                else
                    benzeneVal = 0;
            }
        });

        checkBoxGasoline.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //is chkIos checked?
                if (((CheckBox) v).isChecked()) {
                    gasolineVal = 1;
                }
                else
                    gasolineVal = 0;
            }
        });

        buttonOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNetworkAvailable()) {
                    if (textViewLine.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "please fill the line length ", Toast.LENGTH_SHORT).show();
                    } else {
//                        Toast.makeText(getApplicationContext(), "Benzene Value "+benzeneVal, Toast.LENGTH_SHORT).show();
//                        Toast.makeText(getApplicationContext(), "Gasoline Value "+gasolineVal, Toast.LENGTH_SHORT).show();
//                        Toast.makeText(getApplicationContext(), "All the fields filled !!", Toast.LENGTH_SHORT).show();

                        new searchStationsTask().execute(getString(R.string.URL)+"/searchstation.php");
                        dialog.dismiss();                        /*android.net.Uri.Builder builder = new android.net.Uri.Builder();
                        builder.scheme("http")
                                .authority(getResources().getString(R.string.onURL))
                                .appendPath(getResources().getString(R.string.app_name))
                                .appendPath(getResources().getString(R.string.app_name))
                                .appendPath("updatecar.php");
                        String myUrl = builder.build().toString();
                        new updateCarTask().execute(myUrl);*/
                    }
                }else{
//                    Snackbar.make(view, "Your offline Please connect to INTERNET !!", Snackbar.LENGTH_LONG)
//                            .setAction("Action", (OnClickListener) new Intent(Settings.ACTION_WIFI_SETTINGS)).show();
                    Snackbar snackbar = Snackbar
                            .make(view,"Your offline !!",Snackbar.LENGTH_INDEFINITE)
                            .setAction("open settings", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    startActivity( new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                                }
                            });
                    snackbar.show();

                }
            }

        });

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        mapFragment.getMapAsync(this);
        new GetLocation().invoke();
        StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        if (userLatLng != null) {
            googlePlacesUrl.append("location=" + userLatLng.latitude + "," + userLatLng.longitude);
            Toast.makeText(this, "The User Location : "+userLatLng, Toast.LENGTH_LONG).show();

        }else{
        googlePlacesUrl.append("location=" + sdn.latitude + "," + sdn.longitude);
        }
        googlePlacesUrl.append("&radius=" + 500000);
        googlePlacesUrl.append("&types=" + "gas_station");
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + getString(R.string.google_maps_key));
        Log.e(getPackageName(),"The URL : "+googlePlacesUrl);
        new getAllStationsTask().execute(googlePlacesUrl.toString());
        new getStationsTask().execute(getString(R.string.URL)+"/getallstations.php");

    }

    public void settingsrequest() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        //**************************
        builder.setAlwaysShow(true); //this is the key ingredient
        //**************************

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                                10f, new android.location.LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
//                                        Toast.makeText(getBaseContext(), "Your current location is : " + location, Toast.LENGTH_LONG).show();
//                                        userLatLng = new LatLng(location.getLatitude(),location.getLongitude());
//                                        createMarker(userLatLng.latitude,userLatLng.longitude,"","");
                                        userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
                                        mMap.animateCamera(CameraUpdateFactory.newLatLng(userLatLng));
                                        mMap.setMinZoomPreference(7);
                                        // Toast.makeText(MainActivity.this, "The Last Known Location : " + location, Toast.LENGTH_LONG).show();

                                        Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.getDefault()); //it is Geocoder
//                                        StringBuilder builder = new StringBuilder();
                                        try {
                                            List<Address> address = geoCoder.getFromLocation(userLatLng.latitude, userLatLng.longitude, 1);


                                            finalAddress = address.get(0).getLocality(); //This is the complete address.
//                                             Toast.makeText(getBaseContext(),"The city location : "+finalAddress,Toast.LENGTH_LONG).show();
                                            mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your location : ").snippet(finalAddress));


                                        } catch (IOException e) {
                                        } catch (NullPointerException e) {
                                        }
                                    }

                                    @Override
                                    public void onStatusChanged(String provider, int status, Bundle extras) {
//                                        Toast.makeText(getBaseContext(), "Your StatusChanged: ", Toast.LENGTH_LONG).show();

                                    }

                                    @Override
                                    public void onProviderEnabled(String provider) {
//                                        Toast.makeText(getBaseContext(), "Your ProviderEnabled ", Toast.LENGTH_LONG).show();

                                    }

                                    @Override
                                    public void onProviderDisabled(String provider) {
//                                        Toast.makeText(getBaseContext(), "Your ProviderDisabled ", Toast.LENGTH_LONG).show();

                                    }
                                });
//                        Toast.makeText(MainActivity.this, "location : "+state, Toast.LENGTH_SHORT).show();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sudan = new LatLng(15.5007, 32.5599);
//        mMap.addMarker(new MarkerOptions().position(sudan).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sudan));
    }


    @SuppressLint("StaticFieldLeak")
    class getStationsTask extends AsyncTask<String, Integer, String> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("Wait ");   dialog.setMessage("Connecting .. ");
            dialog.setCancelable(false);
            dialog.show();
//            Toast.makeText(LoginActivity.this, "I am Connecting .. ", Toast.LENGTH_LONG).show();
        }
        @Override
        protected String doInBackground(String... params) {
            // task will done in background
            String link = params[0];

            try {
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                JSONObject jsonParam = new JSONObject();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");

                //Create JSONObject here

                /*jsonParam.put("lat", extras.getDouble("lat"));
                jsonParam.put("lng", extras.getDouble("lng"));*/

                // Send POST output.
                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                printout.writeBytes(jsonParam.toString());
                printout.flush();
                printout.close();

                InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(inputStream);
                final StringBuilder txtBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    txtBuilder.append(line);
                }
                result = txtBuilder.toString();

            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            textView.setText(s);
//            Toast.makeText(RegisterationActivity.this, "the value of S : "+s, Toast.LENGTH_LONG).show();
            try {
                if (s != null) {

                    JSONObject object = new JSONObject(s);
                    JSONArray postsArray = object.getJSONArray("Stations");
                    // Toast.makeText(MainActivity.this, "the data : "+object.getJSONArray("Cars"), Toast.LENGTH_LONG).show();
//                    List<Car_Information> inf = new ArrayList<>();

                    for (int i = 0; i < postsArray.length(); i++) {

                        JSONObject currentObject = postsArray.getJSONObject(i);

                        Double lat = currentObject.getDouble("lat");
                        Double lng = currentObject.getDouble("lng");
                        String name = currentObject.getString("name");
                        String location = currentObject.getString("location");
                        // Toast.makeText(getBaseContext(), "the order "+location, Toast.LENGTH_SHORT).show();
//                                        Log.d("The orders : ", order.toString());
                      /*  Log.d(getPackageName(), "LOCATION : " + location);
                        if (location.equals("NULL")) {
                            location = "khartoum";
                        }
                        LatLng sydney = getLocationFromAddress(MainActivity.this, location);*/

                        Drawable drawable = getResources().getDrawable(R.drawable.ic_local_gas_station);
//                        Drawable drawableclicked = getResources().getDrawable(R.drawable.ic_local_gas_station2);
                        BitmapDescriptor bitmapDescriptor = getMarkerIconFromDrawable(drawable);
//                        BitmapDescriptor bitmapDescriptorclicked = getMarkerIconFromDrawable(drawableclicked);


                        createMarker(lat, lng, name, location,bitmapDescriptor);
//                        createMarker(lat, lng, name, location,bitmapDescriptorclicked);

                    }
                } else {
//                Toast.makeText(RegisterationActivity.this, "the status : "+s, Toast.LENGTH_LONG).show();

                    Toast.makeText(getBaseContext(), "Something went wrong !!", Toast.LENGTH_LONG).show();

                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

    }


    @SuppressLint("StaticFieldLeak")
    class getAllStationsTask extends AsyncTask<String, Integer, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("Wait ");
            dialog.setMessage("Connecting .. ");
            dialog.setCancelable(false);

            dialog.show();
//            Toast.makeText(LoginActivity.this, "I am Connecting .. ", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... params) {
            // task will done in background
            String link = params[0];

            try {
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(inputStream);
                final StringBuilder txtBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    txtBuilder.append(line);
                }
                result = txtBuilder.toString();
                result = txtBuilder.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            textView.setText(s);
//            Toast.makeText(RegisterationActivity.this, "the value of S : "+s, Toast.LENGTH_LONG).show();
            try {
                if (s != null) {
                    result = s;
                    JSONObject object = new JSONObject(s);
                    Log.e(getPackageName(),"The result : "+s);

                    JSONArray postsArray = object.getJSONArray("results");
//                     Toast.makeText(MainActivity.this, "the data : "+object.getJSONArray("results"), Toast.LENGTH_LONG).show();
//                    List<Car_Information> inf = new ArrayList<>();

                    for (int i = 0; i < postsArray.length(); i++) {

                        JSONObject currentObject = postsArray.getJSONObject(i);
                        /*String id = currentObject.getString("id");
                        String type = currentObject.getString("type");
                        String number = currentObject.getString("car_number");
                        String price = currentObject.getString("price");
                        String model = currentObject.getString("model");
                        String location = currentObject.getString("location");
                        String avatar = currentObject.getString("image_path");
                        // Toast.makeText(getBaseContext(), "the order "+location, Toast.LENGTH_SHORT).show();
//                                        Log.d("The orders : ", order.toString());
                        Log.d(getPackageName(), "LOCATION : " + location);
                        if (location.equals("NULL")) {
                            location = "khartoum";
                        }*/
//                        Location location = new Location(String.valueOf(currentObject.getJSONObject("geometry").getJSONObject("location")));
//                        Log.e(getPackageName(),"The LOCATION: "+location);
                        Drawable drawable = getResources().getDrawable(R.drawable.ic_local_gas_station1);
                        BitmapDescriptor bitmapDescriptor = getMarkerIconFromDrawable(drawable);
                        JSONObject cords = currentObject.getJSONObject("geometry").getJSONObject("location");
                        LatLng sydney = new LatLng(cords.getDouble("lat"),cords.getDouble("lng"));
                        createMarker(sydney.latitude, sydney.longitude, currentObject.getString("name"), currentObject.getString("vicinity"), bitmapDescriptor );

                    }
                } else {
//                Toast.makeText(RegisterationActivity.this, "the status : "+s, Toast.LENGTH_LONG).show();

                    Toast.makeText(getBaseContext(), "Something went wrong !!", Toast.LENGTH_LONG).show();

                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    class searchStationsTask extends AsyncTask<String, Integer, String> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("Wait ");
            dialog.setMessage("Connecting .. ");
            dialog.setCancelable(false);

            dialog.show();
//            Toast.makeText(LoginActivity.this, "I am Connecting .. ", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... params) {
            // task will done in background
            String link = params[0];

            try {
                URL url = new URL(link);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                JSONObject jsonParam = new JSONObject();
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");

                //Create JSONObject here

                jsonParam.put("line_length", textViewLine.getText());
                jsonParam.put("benzene", benzeneVal);
                jsonParam.put("gasoline", gasolineVal);

                // Send POST output.
                DataOutputStream printout = new DataOutputStream(urlConnection.getOutputStream());
                printout.writeBytes(jsonParam.toString());
                printout.flush();
                printout.close();

                InputStreamReader inputStream = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(inputStream);
                final StringBuilder txtBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    txtBuilder.append(line);
                }
                result = txtBuilder.toString();

            }
            catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
//            textView.setText(s);
//            Toast.makeText(RegisterationActivity.this, "the value of S : "+s, Toast.LENGTH_LONG).show();
            try {
                if (s != null) {

                    JSONObject object = new JSONObject(s);
                    if (object.has("Stations")) {
                        JSONArray postsArray = object.getJSONArray("Stations");
                        // Toast.makeText(MainActivity.this, "the data : "+object.getJSONArray("Cars"), Toast.LENGTH_LONG).show();
//                    List<Car_Information> inf = new ArrayList<>();

                        for (int i = 0; i < postsArray.length(); i++) {

                            JSONObject currentObject = postsArray.getJSONObject(i);

                            Double lat = currentObject.getDouble("lat");
                            Double lng = currentObject.getDouble("lng");
                            String name = currentObject.getString("name");
                            String location = currentObject.getString("location");
                            // Toast.makeText(getBaseContext(), "the order "+location, Toast.LENGTH_SHORT).show();
//                                        Log.d("The orders : ", order.toString());
                      /*  Log.d(getPackageName(), "LOCATION : " + location);
                        if (location.equals("NULL")) {
                            location = "khartoum";
                        }
                        LatLng sydney = getLocationFromAddress(MainActivity.this, location);*/

                            Drawable drawable = getResources().getDrawable(R.drawable.ic_local_gas_station2);
//                        Drawable drawableclicked = getResources().getDrawable(R.drawable.ic_local_gas_station2);
                            BitmapDescriptor bitmapDescriptor = getMarkerIconFromDrawable(drawable);
//                        BitmapDescriptor bitmapDescriptorclicked = getMarkerIconFromDrawable(drawableclicked);


                            createMarker(lat, lng, name, location,bitmapDescriptor);
//                        createMarker(lat, lng, name, location,bitmapDescriptorclicked);

                        }
                    }else{
                        Toast.makeText(getBaseContext(), object.getString("massage").toString(), Toast.LENGTH_LONG).show();

                    }

                } else {
//                Toast.makeText(RegisterationActivity.this, "the status : "+s, Toast.LENGTH_LONG).show();

                    Toast.makeText(getBaseContext(), "Something went wrong !!", Toast.LENGTH_LONG).show();

                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }

    }

    private class GetLocation {
        public void invoke() {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                RequestPermissionHandler mRequestPermissionHandler = new RequestPermissionHandler();
                mRequestPermissionHandler.requestPermission(MainActivity.this, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, 123, new RequestPermissionHandler.RequestPermissionListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess() {
                        mFusedLocationClient.getLastLocation()
                                .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            sdn = new LatLng(12.8628, 30.2176);
                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(sdn));
//                                                mMap.setMinZoomPreference(7);
                                            userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
                                            mMap.animateCamera(CameraUpdateFactory.newLatLng(userLatLng));
                                            mMap.setMinZoomPreference(7);
                                            // Toast.makeText(MainActivity.this, "The Last Known Location : " + location, Toast.LENGTH_LONG).show();

                                            Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.getDefault()); //it is Geocoder
//                                        StringBuilder builder = new StringBuilder();
                                            try {
                                                List<Address> address = geoCoder.getFromLocation(userLatLng.latitude, userLatLng.longitude, 1);


                                                finalAddress = address.get(0).getLocality(); //This is the complete address.
                                                // Toast.makeText(getBaseContext(),"The city location : "+finalAddress,Toast.LENGTH_LONG).show();
                                                mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your location : ").snippet(finalAddress));


                                            } catch (IOException e) {
                                            } catch (NullPointerException e) {
                                            }
                                        } else {
                                            settingsrequest();
//                                            Toast.makeText(MainActivity.this, "Something Went Wrong !!", Toast.LENGTH_LONG).show();
//                                            Toast.makeText(MainActivity.this, "Please open your GPS !!", Toast.LENGTH_LONG).show();
                                            LocationListener mLocationListener = new LocationListener() {
                                                @Override
                                                public void onLocationChanged(final Location location) {
//                                                    Toast.makeText(getBaseContext(), "Your current location is : " + location, Toast.LENGTH_LONG).show();
                                                }
                                            };

                                            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                                            /*final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                                            dialog.setTitle("Wait ");
                                            dialog.setMessage("Getting Location .. ");
                                            dialog.setCancelable(false);
                                            dialog.show();*/
                                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                                                    10f, new android.location.LocationListener() {

                                                        @Override
                                                        public void onLocationChanged(Location location) {

//                                                            Toast.makeText(getBaseContext(), "Your current location is : " + location, Toast.LENGTH_LONG).show();
                                                            userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
                                                            mMap.animateCamera(CameraUpdateFactory.newLatLng(userLatLng));
                                                            mMap.setMinZoomPreference(7);
                                                            // Toast.makeText(MainActivity.this, "The Last Known Location : " + location, Toast.LENGTH_LONG).show();

                                                            Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.getDefault()); //it is Geocoder
//                                        StringBuilder builder = new StringBuilder();
                                                            try {
                                                                List<Address> address = geoCoder.getFromLocation(userLatLng.latitude, userLatLng.longitude, 1);


                                                                finalAddress = address.get(0).getLocality(); //This is the complete address.
                                                                // Toast.makeText(getBaseContext(),"The city location : "+finalAddress,Toast.LENGTH_LONG).show();
                                                                mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your location : ").snippet(finalAddress));
                                                                /*if (dialog.isShowing()) {
                                                                    dialog.dismiss();
                                                                }*/

                                                            } catch (IOException e) {
                                                            } catch (NullPointerException e) {
                                                            }
                                                        }

                                                        @Override
                                                        public void onStatusChanged(String provider, int status, Bundle extras) {
//                                                            Toast.makeText(getBaseContext(), "Your StatusChanged: " , Toast.LENGTH_LONG).show();

                                                        }

                                                        @Override
                                                        public void onProviderEnabled(String provider) {
//                                                            Toast.makeText(getBaseContext(), "Your ProviderEnabled ", Toast.LENGTH_LONG).show();

                                                        }

                                                        @Override
                                                        public void onProviderDisabled(String provider) {
//                                                            Toast.makeText(getBaseContext(), "Your ProviderDisabled ", Toast.LENGTH_LONG).show();

                                                        }
                                                    });


                                        }
                                    }
                                });
//                        Toast.makeText(MainActivity.this, "request permission success", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed() {
//                        Toast.makeText(MainActivity.this, "request permission failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void createMarker(double latitude, double longitude, final String title, String snippet, BitmapDescriptor bitmapDescriptor) {

        final MarkerOptions options = new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .anchor(0.5f, 0.5f)
                .title(title)
                .snippet(snippet).icon(bitmapDescriptor);
        LatLng marker = new LatLng(latitude,longitude);
        mMap.addMarker(options);
        mMap.setMinZoomPreference(10);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker));
        /*int btn_initPosY = 150;

                switch (newState) {
                    case 2: // SCROLL_STATE_FLING
                        //hide button here
                        btn.animate().translationY(btn.getHeight() + btn_initPosY).setInterpolator(new AccelerateInterpolator(2)).start();
                        break;

                    case 1: // SCROLL_STATE_TOUCH_SCROLL
                        //hide button here
                        btn.animate().translationY(btn.getHeight() + btn_initPosY).setInterpolator(new AccelerateInterpolator(2)).start();
                        break;

                    case 0: // SCROLL_STATE_IDLE
                        //show button here
                        btn.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                        break;

                    default:
                        //show button here
                        btn.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                        break;
                }*/
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (isNetworkAvailable()) {
                    if (marker.getTitle().equals("Your location : ")) {
//                    TODO
                    } else {
                        Intent intent = new Intent(MainActivity.this, StationActivity.class);
                        intent.putExtra("name", marker.getTitle());
                        intent.putExtra("lat", marker.getPosition().latitude);
                        intent.putExtra("lng", marker.getPosition().longitude);
                        intent.putExtra("location", marker.getSnippet());

                        startActivity(intent);
//                        finish();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Your offline !!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
      if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please double tab to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
