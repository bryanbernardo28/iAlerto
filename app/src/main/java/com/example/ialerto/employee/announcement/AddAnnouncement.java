package com.example.ialerto.employee.announcement;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.ialerto.Dashboard;
import com.example.ialerto.MyConfig;
import com.example.ialerto.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddAnnouncement extends AppCompatActivity {
    TextInputLayout etl_title,etl_details,etl_evacuation,etl_barangay;
    static TextInputEditText et_title,et_details,et_evacuation,et_barangay;
    static ArrayList<String> checked_barangays;
    static boolean[] checkedBarangayItems = {};
    static String[] barangays;

    static ArrayList<String> checked_evacuations;
    static boolean[] checkedEvacuationItems = {};
    static String[] evacuations;

    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_announcement);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        checked_barangays = new ArrayList<>();
        checked_evacuations = new ArrayList<>();
        new getBarangay().execute();

        etl_title = findViewById(R.id.etl_title_id);
        etl_details = findViewById(R.id.etl_details_id);
        etl_evacuation = findViewById(R.id.etl_evacuation_id);
        etl_barangay = findViewById(R.id.etl_barangay_id);

        et_title = findViewById(R.id.et_title_id);
        et_details = findViewById(R.id.et_details_id);
        et_evacuation = findViewById(R.id.et_evacuation_id);
        et_evacuation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment evacuationDialog = new evacuationDialog();
                evacuationDialog.show(getSupportFragmentManager(),"Evacuation Dialog");
            }
        });
        et_barangay = findViewById(R.id.et_barangay_id);
        et_barangay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment barangayDialog = new barangayDialog();
                barangayDialog.show(getSupportFragmentManager(), "Barangay Dialog");
            }
        });
    }

    class getBarangay extends AsyncTask<String,Void,String> {
        ProgressDialog pd;
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/barangay")
                    .build();
            try {
                Response response = getstudents.newCall(request).execute();
                Log.d("check", String.valueOf(response.code()));
                if (response.isSuccessful()){
                    return response.body().string();
                }
                else{
                    return null;
                }
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(AddAnnouncement.this);
            pd.setMessage("Fetching Data...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(s);
                Log.d("checkBarangay", String.valueOf(jsonObject));
                JSONArray barangaysArray = jsonObject.getJSONArray("barangays");
                int barangay_length = barangaysArray.length();
                if (barangay_length > 0){
                    setBarangaysData(barangaysArray);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setBarangaysData(JSONArray barangaysArray){
        int barangay_length = barangaysArray.length();
        barangays  = new String[barangay_length];
        checkedBarangayItems = new boolean[barangay_length];
        for (int i = 0; i < barangay_length; i++){
            try {
                barangays[i] = barangaysArray.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setEvacuationsData(JSONArray evacuationsArray){
        int evacuation_length = evacuationsArray.length();
        evacuations  = new String[evacuation_length];
        checkedEvacuationItems = new boolean[evacuation_length];
        for (int i = 0; i < evacuation_length; i++){
            try {
                evacuations[i] = evacuationsArray.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class barangayDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            Log.d("checkBarangay",barangays.toString());
            builder.setTitle("Choose Barangay")
                    .setMultiChoiceItems(barangays, checkedBarangayItems, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (isChecked) {
                                checked_barangays.add(barangays[which]);
                            }
                            else if (checked_barangays.contains(barangays[which])) {
                                int index = checked_barangays.indexOf(barangays[which]);
                                checked_barangays.remove(index);
                            }

                            AlertDialog barangayDialog = (AlertDialog) getDialog();
                            if (!checked_barangays.isEmpty()){
                                barangayDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                            else{
                                barangayDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                            }
                        }
                    })
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            et_barangay.setText(TextUtils.join(",",checked_barangays));
                            JSONArray jsonArrayBarangay = new JSONArray(checked_barangays);
                            new getEvacuations(jsonArrayBarangay).execute();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog barangayDialog = builder.create();
            barangayDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = barangayDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (checked_barangays.isEmpty()){
                        b.setEnabled(false);
                    }
                    else{
                        b.setEnabled(true);
                    }
                }
            });

            return barangayDialog;
        }
    }



    static class getEvacuations extends AsyncTask<String,Void,String> {
        ProgressDialog pd;
        JSONArray barangays;

        public getEvacuations(JSONArray barangays) {
            this.barangays = barangays;
        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getEvacuationCenter = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("barangays", String.valueOf(barangays))
                    .build();


            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/evacuation/post/getEvacuationByBarangay")
                    .post(requestBody)
                    .build();
            try {
                Response response = getEvacuationCenter.newCall(request).execute();
                Log.d("check", String.valueOf(response.code()));
                if (response.isSuccessful()){
                    return response.body().string();
                }
                else{
                    return null;
                }
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(context);
            pd.setMessage("Fetching Data...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(s);
                Log.d("checkEvac", String.valueOf(jsonObject));
                JSONArray evacuationsArray = jsonObject.getJSONArray("evacuations");
                int evacuation_length = evacuationsArray.length();
                if (evacuation_length > 0){
                    setEvacuationsData(evacuationsArray);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class evacuationDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Choose Evacuation")
                    .setMultiChoiceItems(evacuations, checkedEvacuationItems, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            if (isChecked) {
                                checked_evacuations.add(evacuations[which]);
                            }
                            else if (checked_evacuations.contains(evacuations[which])) {
                                int index = checked_evacuations.indexOf(evacuations[which]);
                                checked_evacuations.remove(index);
                            }

                            AlertDialog barangayDialog = (AlertDialog) getDialog();
                            if (!checked_evacuations.isEmpty()){
                                barangayDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                            }
                            else{
                                barangayDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                            }
                        }
                    })
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d("check", String.valueOf(checked_evacuations));
                            et_evacuation.setText(TextUtils.join(",",checked_evacuations));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog evacuationDialog = builder.create();
            evacuationDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = evacuationDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    if (checked_evacuations.isEmpty()){
                        b.setEnabled(false);
                    }
                    else{
                        b.setEnabled(true);
                    }
                }
            });

            return evacuationDialog;
        }
    }


    public void cancel(View v){
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","announcement");
        startActivity(goback);
        finish();
    }

    public void save(View v){
//        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        new save_announcement().execute();
//        Toast.makeText(this, "Barangay : " + checked_barangays, Toast.LENGTH_SHORT).show();
    }


    class save_announcement extends AsyncTask<String,Void,String> {
        ProgressDialog pd;
        String title = et_title.getText().toString();
        String details = et_details.getText().toString();
        JSONArray jsonArrayBarangays = new JSONArray(checked_barangays);
        JSONArray jsonArrayEvacuations = new JSONArray(checked_evacuations);
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("title",title)
                    .addFormDataPart("details",details)
                    .addFormDataPart("evacuations", String.valueOf(jsonArrayEvacuations))
                    .addFormDataPart("barangays", String.valueOf(jsonArrayBarangays))
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/announcements")
                    .post(requestBody)
                    .build();
            try {
                Response response = getstudents.newCall(request).execute();
                Log.d("check", String.valueOf(response.code()));
                if (response.isSuccessful()){
                    return response.body().string();
                }
                else{
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(AddAnnouncement.this);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");
                if (response){

                    Intent success = new Intent(AddAnnouncement.this,Dashboard.class);
                    success.putExtra("from_activity","announcement");
                    startActivity(success);
                    finish();
                    etl_title.setError(null);
                    etl_details.setError(null);
                    Toast.makeText(AddAnnouncement.this,"Announcement has been added successfully.",Toast.LENGTH_LONG).show();
                }
                else{
                    JSONObject error = jsonObject.getJSONObject("response");
                    if (error.has("title")){
                        JSONArray name = error.getJSONArray("title");
                        etl_title.setError(name.get(0).toString());
                    }

                    if (error.has("details")){
                        JSONArray email = error.getJSONArray("details");
                        etl_details.setError(email.get(0).toString());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }


    @Override
    public void onBackPressed() {
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","announcement");
        startActivity(goback);
        finish();
    }


    @Override
    public boolean onSupportNavigateUp() {
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","announcement");
        startActivity(goback);
        finish();
        return true;
    }
}
