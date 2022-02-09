package com.example.ialerto.admin.profile;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ialerto.Dashboard;
import com.example.ialerto.MainActivity;
import com.example.ialerto.MyConfig;
import com.example.ialerto.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePassword extends AppCompatActivity {
    private SharedPreferences myprofile,profileinfo_pref;
    TextInputEditText et_oldpassword,et_newpassword,et_passwordconfirm;
    TextInputLayout etl_oldpassword,etl_newpassword,etl_passwordconfirm;
    TextView tv_error;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myprofile = getSharedPreferences(MainActivity.PROFILEPREF_NAME, Context.MODE_PRIVATE);


        et_oldpassword = findViewById(R.id.et_oldpassword_id);
        et_newpassword = findViewById(R.id.et_newpassword_id);
        et_passwordconfirm = findViewById(R.id.et_passwordconfirm_id);

        etl_oldpassword = findViewById(R.id.etl_oldpassword_id);
        etl_newpassword = findViewById(R.id.etl_newpassword_id);
        etl_passwordconfirm = findViewById(R.id.etl_passwordconfirm_id);

        tv_error = findViewById(R.id.tv_error_id);

    }

    public void cancel(View v){
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","profile");
        startActivity(goback);
        finish();
    }

    public void submit_change_password(View v){
        new change_password().execute();
    }

    class change_password extends AsyncTask<String,Void,String> {
        ProgressDialog pd;
        String id = myprofile.getString("id","");
        String old_password = et_oldpassword.getText().toString();
        String new_password = et_newpassword.getText().toString();
        String confirm_password = et_passwordconfirm.getText().toString();

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("old_password",old_password)
                    .addFormDataPart("password",new_password)
                    .addFormDataPart("password_confirmation",confirm_password)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/update/change_password/"+id)
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
            pd = new ProgressDialog(ChangePassword.this);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {

            etl_oldpassword.setError(null);
            etl_newpassword.setError(null);
            etl_passwordconfirm.setError(null);
            tv_error.setText("");
            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");
                if (response){

                    Intent success = new Intent(ChangePassword.this,Dashboard.class);
                    success.putExtra("from_activity","profile");
                    startActivity(success);
                    finish();
                    Toast.makeText(ChangePassword.this,"Account updated successfully.",Toast.LENGTH_LONG).show();
                }
                else{
                    String error = jsonObject.getString("response");
                    tv_error.setText(error);
                    Log.d("check", error);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent goback = new Intent(this, MainActivity.class);
        goback.putExtra("from_activity","profile");
        startActivity(goback);
        finish();
        return true;
    }


    @Override
    public void onBackPressed() {
        Intent goback = new Intent(this,MainActivity.class);
        goback.putExtra("from_activity","profile");
        startActivity(goback);
        finish();
    }
}
