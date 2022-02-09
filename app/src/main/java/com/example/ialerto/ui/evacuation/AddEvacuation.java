package com.example.ialerto.ui.evacuation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
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

public class AddEvacuation extends AppCompatActivity {
    static TextInputLayout etl_name,etl_capacity,etl_address,etl_barangay;
    static TextInputEditText et_name,et_capacity,et_address,et_barangay;
    static RadioGroup rg_isavailable;
    static String[] barangays;
    static int is_available = 1;
    static ArrayList<String> checked_barangays;
    static boolean[] checkedItems = {};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_evacuation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        checked_barangays = new ArrayList<>();
        new getBarangay().execute();

        etl_name = findViewById(R.id.etl_name_id);
        etl_capacity = findViewById(R.id.etl_capacity_id);
        etl_address = findViewById(R.id.etl_address_id);
        etl_barangay = findViewById(R.id.etl_barangay_id);

        et_name = findViewById(R.id.et_name_id);
        et_capacity = findViewById(R.id.et_capacity_id);
        et_address = findViewById(R.id.et_address_id);
        et_barangay = findViewById(R.id.et_barangay_id);

        et_barangay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment barangayDialog = new barangayDialog();
                barangayDialog.show(getSupportFragmentManager(), "Barangay Dialog");
            }
        });

        rg_isavailable = findViewById(R.id.rg_isavailable_id);
        rg_isavailable.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_yes_id:
                        is_available = 1;
                        break;
                    case R.id.rb_no_id:
                        is_available = 0;
                        break;
                }
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
            pd = new ProgressDialog(AddEvacuation.this);
            pd.setMessage("Fetching Barangays...");
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
                JSONArray barangaysArray = jsonObject.getJSONArray("barangays");
                int barangay_length = barangaysArray.length();
                if (barangay_length != 0){
                    barangays  = new String[barangay_length];
                    checkedItems = new boolean[barangay_length];
                    for (int i = 0; i < barangay_length; i++){
                        barangays[i] = barangaysArray.getString(i);
                    }
                }
                else{
                    Toast.makeText(AddEvacuation.this, "No Barangay Fetched.", Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class barangayDialog extends DialogFragment{
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Choose Barangay")
                    .setMultiChoiceItems(barangays, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
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
                            Log.d("check", String.valueOf(checked_barangays));
                            et_barangay.setText(TextUtils.join(",",checked_barangays));
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

    public void submit(View v){
        new submitEvacuation().execute();
    }

    class submitEvacuation extends AsyncTask<String,Void,String> {
        ProgressDialog pd;
        String name = et_name.getText().toString();
        String capacity = et_capacity.getText().toString();
        String address = et_address.getText().toString();
        JSONArray jsonArrayBarangay = new JSONArray(checked_barangays);
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("name",name)
                    .addFormDataPart("capacity",capacity)
                    .addFormDataPart("address",address)
                    .addFormDataPart("barangay", String.valueOf(jsonArrayBarangay))
                    .addFormDataPart("is_avail" , String.valueOf(is_available))
                    .build();

            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/evacuation")
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
            } catch (
                    IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(AddEvacuation.this);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                etl_name.setError(null);
                etl_capacity.setError(null);
                etl_address.setError(null);
                etl_barangay.setError(null);

                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");

                if (response){
//                    JSONObject jsonObject1 = jsonObject.getJSONObject("response");
//                    Log.d("check", String.valueOf(jsonObject1));
                    Intent i = new Intent(AddEvacuation.this, Dashboard.class);
                    i.putExtra("from_activity", "evacuation");
                    startActivity(i);
                    finish();

                }
                else{
                    JSONObject error = jsonObject.getJSONObject("response");
                    if (error.has("name")){
                        JSONArray name = error.getJSONArray("name");
                        etl_name.setError(name.get(0).toString());
                    }

                    if (error.has("capacity")){
                        JSONArray capacity = error.getJSONArray("capacity");
                        etl_capacity.setError(capacity.get(0).toString());
                    }

                    if (error.has("address")){
                        JSONArray address = error.getJSONArray("address");
                        etl_address.setError(address.get(0).toString());
                    }

                    if (error.has("barangay")){
                        JSONArray barangay = error.getJSONArray("barangay");
                        etl_barangay.setError(barangay.get(0).toString());
                    }

                    Toast.makeText(AddEvacuation.this,"Please fill out all required fields.",Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AddEvacuation.this, Dashboard.class);
        intent.putExtra("from_activity","evacuation");
        startActivity(intent);
        finish();
    }
}