package ayman.dexterlab.com.sudangas;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StationActivity extends AppCompatActivity {
    private Bundle extras;
    private TextView editTextName;
    private TextView editTextLocation;
    private TextView editTextTanker;
    private TextView editTextBenzene;
    private TextView editTextGasoline;
    private TextView TextLine;
    private TextView editTextNotes;
    FloatingActionButton editButton;


    String result;
    private DataOutputStream printout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);
        editTextName = findViewById(R.id.station_name);
        editTextLocation = findViewById(R.id.station_location);
        editTextTanker = findViewById(R.id.station_tanker);
        editTextBenzene = findViewById(R.id.station_benzene);
        editTextGasoline = findViewById(R.id.station_gasoline);
        TextLine = findViewById(R.id.station_line);
        editTextNotes= findViewById(R.id.station_note);
        editButton = findViewById(R.id.edit);
        extras =getIntent().getExtras();

        /*Toast.makeText(this, "Station name : "+extras.getString("name"), Toast.LENGTH_LONG).show();
        Toast.makeText(this, "Station latitude : "+extras.getDouble("lat"), Toast.LENGTH_LONG).show();
        Toast.makeText(this, "Station longitude : "+extras.getDouble("lng"), Toast.LENGTH_LONG).show();
        Toast.makeText(this, "Station location : "+extras.getString("location"), Toast.LENGTH_LONG).show();
*/
        editTextName.setText(extras.getString("name"));
        editTextLocation.setText(extras.getString("location"));
        new getStationTask().execute(getString(R.string.URL)+"/getstation.php");


    }

    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }


        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("StaticFieldLeak")
    class getStationTask extends AsyncTask<String, Integer, String> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(StationActivity.this);
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

                jsonParam.put("lat", extras.getDouble("lat"));
                jsonParam.put("lng", extras.getDouble("lng"));

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
//            Toast.makeText(StationActivity.this, "the value of S : "+s, Toast.LENGTH_LONG).show();
            try{if (s != null) {

                final JSONObject object = new JSONObject(s);

                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(isNetworkAvailable()) {
                            Intent intent ;
                            if(object.has("Station")) {
                                intent = new Intent(StationActivity.this,UpdateStationActivity.class);
                            }else{
                                intent = new Intent(StationActivity.this,EditStationActivity.class);
                            }

                            intent.putExtra("name", extras.getString("name"));
                            intent.putExtra("lat", extras.getDouble("lat"));
                            intent.putExtra("lng",extras.getDouble("lng"));
                            intent.putExtra("location", extras.getString("location"));

                            startActivity(intent);
                        }else{
//                    Snackbar.make(view, "Your offline Please connect to INTERNET !!", Snackbar.LENGTH_LONG)
//                            .setAction("Action", (OnClickListener) new Intent(Settings.ACTION_WIFI_SETTINGS)).show();
                            Snackbar snackbar = Snackbar
                                    .make(v,"Your offline !!",Snackbar.LENGTH_INDEFINITE)
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

                JSONArray postsArray = object.getJSONArray("Station");

                Log.d(getPackageCodePath(), "the station data : "+object.getJSONArray("Station"));
//                List<Car_Information> inf = new ArrayList<>();


                JSONObject currentObject = postsArray.getJSONObject(0);
                String name = currentObject.getString("name");
                String location = currentObject.getString("location");
                String tankerTime = currentObject.getString("tanker_time");
                int benzene = currentObject.getInt("benzene");
                int gasoline = currentObject.getInt("gasoline");
                int line_length = currentObject.getInt("line_length");
                String notes = currentObject.getString("notes");


                editTextName.setText(name);
                editTextLocation.setText(location);
                editTextTanker.setText(tankerTime);
                if (benzene == 1) {
                    editTextBenzene.setText(R.string.available);

                }else{
                    editTextBenzene.setText(R.string.not_available);
                }
                if (gasoline == 1) {
                    editTextGasoline.setText(R.string.available);

                }else{
                    editTextGasoline.setText(R.string.not_available);
                }

                TextLine.setText(String.valueOf(line_length));
                editTextNotes.setText(notes);


            }else {
//                Toast.makeText(RegisterationActivity.this, "the status : "+s, Toast.LENGTH_LONG).show();

                Toast.makeText(getBaseContext(), "Something went wrong !!", Toast.LENGTH_LONG).show();


               /* editTextTanker.setText("NULL");
                editTextBenzene.setText("NULL");
                editTextGasoline.setText("NULL");
                editTextLine.setText(""NULL);
                editTextNotes.setText("NULL");*/

            }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            if (dialog.isShowing()) {
                dialog.dismiss();
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
}
