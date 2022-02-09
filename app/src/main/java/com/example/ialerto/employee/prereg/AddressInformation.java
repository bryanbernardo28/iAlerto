package com.example.ialerto.employee.prereg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.ialerto.MyConfig;
import com.example.ialerto.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AddressInformation extends AppCompatActivity {
    static TextInputLayout etl_province,etl_city,etl_barangay,etl_detailed_address;
    static TextInputEditText et_province,et_city,et_barangay,et_detailed_address;
    static String[] barangay_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_information);

        new get_barangay().execute();


        etl_province = findViewById(R.id.etl_province_id);
        etl_city = findViewById(R.id.etl_city_id);
        etl_barangay = findViewById(R.id.etl_barangay_id);
        etl_detailed_address = findViewById(R.id.etl_detailed_address_id);

        et_province = findViewById(R.id.et_province_id);
        et_city = findViewById(R.id.et_city_id);
        et_barangay = findViewById(R.id.et_barangay_id);
        et_detailed_address = findViewById(R.id.et_detailed_address_id);


        et_barangay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new AddressFragment();
                newFragment.show(getSupportFragmentManager(), "address");
            }
        });
    }

    public void submit(View v){

        boolean has_error = false;
        if (et_province.getText().toString().isEmpty() || et_province.getText().toString() == null){
            etl_province.setError("Province Error");
            has_error = true;
        }
        else{
            etl_province.setError(null);
        }
        if (et_city.getText().toString().isEmpty() || et_city.getText().toString() == null){
            etl_city.setError("City Error");
            has_error = true;
        }
        else{
            etl_city.setError(null);
        }
        if (et_barangay.getText().toString().isEmpty() || et_barangay.getText().toString() == null){
            etl_barangay.setError("Barangay Error");
            has_error = true;
        }
        else{
            etl_barangay.setError(null);
        }
        if (et_detailed_address.getText().toString().isEmpty() || et_detailed_address.getText().toString() == null){
            etl_detailed_address.setError("Address Error \n"+ getString(R.string.register_detailed_address_helper));
            has_error = true;
        }
        else{
            etl_detailed_address.setError(null);

        }

        if (!has_error){
            new address().execute();
        }
    }

    public static class AddressFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Barangay")
                    .setItems(barangay_list, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            et_barangay.setText(barangay_list[which]);
                            Log.d("check","ID: " + which);
                            dialog.dismiss();
                        }
                    });
            return builder.create();
        }
    }

    class get_barangay extends AsyncTask<String,Void,String> {
        ProgressDialog pd;

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient get_barangay = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/register/get/barangay")
                    .build();
            try {
                Response response = get_barangay.newCall(request).execute();
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
            super.onPreExecute();
            pd = new ProgressDialog(AddressInformation.this);
            pd.setMessage("Fetching barangay list...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                pd.dismiss();
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");

                if (response){
                    JSONArray jsonArrayBarangay = jsonObject.getJSONArray("barangays");
                    barangay_list = new String[jsonArrayBarangay.length()];
                    for (int i = 0;i < jsonArrayBarangay.length(); i++){
                        jsonObject = jsonArrayBarangay.getJSONObject(i);
                        String barangay_name = jsonObject.getString("name");
                        barangay_list[i] = barangay_name;
                    }

                }
                else{
                    Toast.makeText(AddressInformation.this,"Failed to get barangay list.",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class address extends AsyncTask<String,Void,String> {
        ProgressDialog pd;
        String province = et_province.getText().toString();
        String city = et_city.getText().toString();
        String barangay = et_barangay.getText().toString();
        String detailed_address = et_detailed_address.getText().toString();

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("province",province)
                    .addFormDataPart("city",city)
                    .addFormDataPart("barangay",barangay)
                    .addFormDataPart("detailed_address",detailed_address)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/register/store/check_address")
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
            super.onPreExecute();
            pd = new ProgressDialog(AddressInformation.this);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                etl_province.setError(null);
                etl_city.setError(null);
                etl_barangay.setError(null);
                etl_detailed_address.setError(null);
                pd.dismiss();
                JSONObject jsonObject = new JSONObject(s);
                Log.d("check", s);
                boolean response = jsonObject.getBoolean("success");

                if (response){
                    JSONObject address_response = jsonObject.getJSONObject("response");
                    String province = et_province.getText().toString();
                    String city = et_city.getText().toString();
                    String barangay = et_barangay.getText().toString();
                    String detailed_address = et_detailed_address.getText().toString();
                    String address = detailed_address + " ";
                    address += ", Barangay "+barangay+ ", " +city+ " " +province;
                    Log.d("checkResult",address);

                    Log.d("checkUser", String.valueOf(address_response));
                    Intent i = new Intent();
                    i.putExtra("address",address);
                    i.putExtra("province",province);
                    i.putExtra("city",city);
                    i.putExtra("barangay",barangay);
                    i.putExtra("detailed_address",detailed_address);
                    setResult(Activity.RESULT_OK, i);
                    finish();

                }
                else{
                    JSONObject error = jsonObject.getJSONObject("response");
                    if (error.has("province")){
                        JSONArray province = error.getJSONArray("province");
                        etl_province.setError(province.get(0).toString());
                    }

                    if (error.has("city")){
                        JSONArray city = error.getJSONArray("city");
                        etl_city.setError(city.get(0).toString());
                    }

                    if (error.has("barangay")){
                        JSONArray barangay = error.getJSONArray("barangay");
                        etl_barangay.setError(barangay.get(0).toString());
                    }

                    if (error.has("detailed_address")){
                        JSONArray detailed_address = error.getJSONArray("detailed_address");
                        etl_detailed_address.setError(detailed_address.get(0).toString());
                    }

                    Toast.makeText(AddressInformation.this,"Please fill out all required fields.",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra("address","");
        setResult(Activity.RESULT_CANCELED,i);
        finish();
    }
}