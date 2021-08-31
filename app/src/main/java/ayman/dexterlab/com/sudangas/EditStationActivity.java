package ayman.dexterlab.com.sudangas;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class EditStationActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextLocation;
    private EditText editTextTanker;
    private EditText editTextLine;
    private EditText editTextNote;
    private CheckBox checkBoxBenzene;
    private CheckBox checkBoxGasoline;
    private Button buttonSave;

    private Bundle extras;

    private String result;
    private DataOutputStream printout;

    int benzeneVal,gasolineVal = 0;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_station);

        editTextName = findViewById(R.id.station_name);
        editTextLocation = findViewById(R.id.station_location);
        editTextTanker = findViewById(R.id.tanker_time);
        editTextLine = findViewById(R.id.station_line);
        editTextNote = findViewById(R.id.station_notes);
        checkBoxBenzene = findViewById(R.id.checkbox_benzene);
        checkBoxGasoline = findViewById(R.id.checkbox_gasoline);
        buttonSave = findViewById(R.id.add);

        extras = getIntent().getExtras();

        editTextName.setText(extras.getString("name"));
        editTextLocation.setText(extras.getString("location"));
//        Toast.makeText(this, "Activity name : "+"EditStation", Toast.LENGTH_LONG).show();

//        Toast.makeText(this, "Station name : "+extras.getString("name"), Toast.LENGTH_LONG).show();
//        Toast.makeText(this, "Station latitude : "+extras.getDouble("lat"), Toast.LENGTH_LONG).show();
//        Toast.makeText(this, "Station longitude : "+extras.getDouble("lng"), Toast.LENGTH_LONG).show();
//        Toast.makeText(this, "Station location : "+extras.getString("location"), Toast.LENGTH_LONG).show();
        editTextTanker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextTanker.setText("");
                tiemPicker(editTextTanker);
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

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isNetworkAvailable()) {
                    if (editTextName.getText().toString().equals("")
                            ||editTextTanker.getText().toString().equals("") || editTextLocation.getText().toString().equals("")
                            || editTextLine.getText().toString().equals("")|| editTextNote.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), "please fill all the fields", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Benzene Value "+benzeneVal, Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "Gasoline Value "+gasolineVal, Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "All the fields filled !!", Toast.LENGTH_SHORT).show();

                        new addStationTask().execute(getString(R.string.URL)+"/addstation.php");
                        /*android.net.Uri.Builder builder = new android.net.Uri.Builder();
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


    }

    private void tiemPicker(EditText view) {
        // Get Current Time
        final Calendar c = Calendar.getInstance();
        final int[] mHour = {c.get(Calendar.HOUR_OF_DAY)};
        int mMinute = c.get(Calendar.MINUTE);
        final EditText timeView = view;
        // Launch Time Picker Dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String all;
                        mHour[0] = hourOfDay;
                        if (hourOfDay > c.get(Calendar.HOUR_OF_DAY)) {
                            if(minute<10){
                                all = timeView.getText().toString()+hourOfDay+":"+"0"+minute;
                            }else{
                                all = timeView.getText().toString()+hourOfDay+":"+minute;
                            }
                            timeView.setText(all);
                        }else{
                            Toast.makeText(EditStationActivity.this, "INVALID TIME !!", Toast.LENGTH_SHORT).show();
                        }


                    }
                }, mHour[0], mMinute, false);
        timePickerDialog.show();
    }

    @SuppressLint("StaticFieldLeak")
    class addStationTask extends AsyncTask<String, Integer, String> {
        ProgressDialog dialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(EditStationActivity.this);
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
                jsonParam.put("name",extras.getString("name"));
                jsonParam.put("tanker_time", editTextTanker.getText().toString());
                jsonParam.put("benzene",benzeneVal);
                jsonParam.put("gasoline",gasolineVal);
                jsonParam.put("line_length",editTextLine.getText().toString());
                jsonParam.put("notes",editTextNote.getText().toString());
                jsonParam.put("lat",extras.getDouble("lat"));
                jsonParam.put("lng",extras.getDouble("lng"));
                jsonParam.put("location",editTextLocation.getText().toString());
                // Send POST output.
                printout = new DataOutputStream(urlConnection.getOutputStream());
//                    byte[] utf8 = jsonParam.toString().getBytes("UTF-8");
                printout.write(jsonParam.toString().getBytes("UTF-8"));
//                    printout.writeBytes(utf8.toString());
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
            try{if (s != null) {

                final JSONObject jObj = new JSONObject(s);

                int status = jObj.getInt("status");
                if (status == 1) {
                    try {
                        Toast.makeText(getBaseContext(), jObj.getString("massage").toString(), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    Intent intent = new Intent(EditStationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                if(status == 2){
                    try {
                        Toast.makeText(getBaseContext(), jObj.getString("massage").toString(), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                else {
                    try {
                        Toast.makeText(getBaseContext(), jObj.getString("massage").toString(), Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else {
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
