package com.example.ialerto;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

public class VerifyAccount extends AppCompatActivity {
    TextInputLayout etl_email,etl_code;
    TextInputEditText et_email,et_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etl_email = findViewById(R.id.etl_email_id);
        etl_code = findViewById(R.id.etl_code_id);

        et_email = findViewById(R.id.et_email_id);
        et_code = findViewById(R.id.et_code_id);

    }

    public void resend(View v){
        new resend_code().execute();
    }

    public void submit(View v){
        new verify_account().execute();
    }

    class resend_code extends AsyncTask<String,Void,String> {
        ProgressDialog pd;
        String email = et_email.getText().toString();

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("email",email)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/register/email/resend_code")
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
            pd = new ProgressDialog(VerifyAccount.this);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                etl_email.setError(null);
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");

                if (response){
                    Toast.makeText(VerifyAccount.this, "Your verification code has been sent to your associated account.", Toast.LENGTH_LONG).show();
                }
                else{
                    JSONObject error = jsonObject.getJSONObject("response");
                    if (error.has("email")){
                        JSONArray email = error.getJSONArray("email");
                        etl_email.setError(email.get(0).toString());
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }


    class verify_account extends AsyncTask<String,Void,String> {
        ProgressDialog pd;
        String email = et_email.getText().toString();
        String verification_code = et_code.getText().toString();

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("email",email)
                    .addFormDataPart("verification_code",verification_code)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/register/email/verify_account")
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
            pd = new ProgressDialog(VerifyAccount.this);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                etl_email.setError(null);
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");

                if (response){
                    Intent i = new Intent(VerifyAccount.this,MainActivity.class);
                    startActivity(i);
                    finish();
                    Toast.makeText(VerifyAccount.this, "Your account has been verified.", Toast.LENGTH_SHORT).show();
                }
                else{
                    JSONObject error = jsonObject.getJSONObject("response");
                    if (error.has("email")){
                        JSONArray email = error.getJSONArray("email");
                        etl_email.setError(email.get(0).toString());
                    }

                    if (error.has("verification_code")){
                        JSONArray verification_code = error.getJSONArray("verification_code");
                        etl_code.setError(verification_code.get(0).toString());
                    }

                    if (error.has("error")){
                        JSONArray error_array = error.getJSONArray("error");
                        Toast.makeText(VerifyAccount.this,error_array.get(0).toString(), Toast.LENGTH_SHORT).show();
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
        Intent goback = new Intent(this, MainActivity.class);
        startActivity(goback);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}