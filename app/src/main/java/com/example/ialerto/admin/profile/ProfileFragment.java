package com.example.ialerto.admin.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.ialerto.Dashboard;
import com.example.ialerto.MainActivity;
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

public class ProfileFragment extends Fragment {
    private SharedPreferences myprofile,profileinfo_pref;
    TextInputEditText et_firstname,et_middlename,et_lastname,et_email;
    TextInputLayout etl_firstname,etl_middlename,etl_lastname,etl_email;
    Button btn_update_profile,btn_change_password;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        et_firstname = view.findViewById(R.id.et_firstname_id);
        et_middlename = view.findViewById(R.id.et_middlename_id);
        et_lastname = view.findViewById(R.id.et_lastname_id);
        et_email = view.findViewById(R.id.et_email_id);

        etl_firstname = view.findViewById(R.id.etl_firstname_id);
        etl_middlename = view.findViewById(R.id.etl_middlename_id);
        etl_lastname = view.findViewById(R.id.etl_lastname_id);
        etl_email = view.findViewById(R.id.etl_email_id);


        myprofile = getActivity().getSharedPreferences(MainActivity.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        String name = myprofile.getString("name","");
        String first_name = myprofile.getString("first_name","");
        String middle_name = myprofile.getString("middle_name","");
        String last_name = myprofile.getString("last_name","");
        String email = myprofile.getString("email","");

        et_firstname.setText(first_name);
        et_middlename.setText(middle_name);
        et_lastname.setText(last_name);
        et_email.setText(email);


        btn_update_profile = view.findViewById(R.id.btn_submit_update_id);
        btn_update_profile.setOnClickListener(v -> {
            new change_profile().execute();
        });


        btn_change_password = view.findViewById(R.id.btn_change_password_id);
        btn_change_password.setOnClickListener(v -> {
            Intent change_pass = new Intent(getActivity(), ChangePassword.class);
            startActivity(change_pass);
            getActivity().finish();
        });

        return view;
    }

    class change_profile extends AsyncTask<String,Void,String> {
        ProgressDialog pd;
        String id = myprofile.getString("id","");
        String email = et_email.getText().toString();
        String first_name = et_firstname.getText().toString();
        String middle_name = et_middlename.getText().toString();
        String last_name = et_lastname.getText().toString();
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("first_name",first_name)
                    .addFormDataPart("middle_name",middle_name)
                    .addFormDataPart("last_name",last_name)
                    .addFormDataPart("email",email)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/update/edit/"+id)
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
//            super.onPreExecute();
            pd = new ProgressDialog(getActivity());
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
            Log.d("check", String.valueOf(s));
            etl_firstname.setError(null);
            etl_middlename.setError(null);
            etl_lastname.setError(null);
            etl_email.setError(null);
            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");
                if (response){

                    JSONObject user = jsonObject.getJSONObject("response");


                    String name = user.getString("name");
                    String email = user.getString("email");

                    profileinfo_pref = getActivity().getSharedPreferences(MainActivity.PROFILEPREF_NAME,Context.MODE_PRIVATE);
                    SharedPreferences.Editor profileinfo_pref_editor = profileinfo_pref.edit();
                    profileinfo_pref_editor.putString("name",name);
                    profileinfo_pref_editor.putString("email",email);
                    profileinfo_pref_editor.apply();


                    Intent success = new Intent(getActivity(), Dashboard.class);
                    success.putExtra("from_activity","profile");
                    startActivity(success);
                    getActivity().finish();



                    Toast.makeText(getActivity(),"Account updated successfully.",Toast.LENGTH_LONG).show();
                }
                else{
                    JSONObject error = jsonObject.getJSONObject("response");
                    if (error.has("first_name")){
                        JSONArray first_name = error.getJSONArray("first_name");
                        etl_firstname.setError(first_name.get(0).toString());
                    }

                    if (error.has("middle_name")){
                        JSONArray middle_name = error.getJSONArray("middle_name");
                        etl_middlename.setError(middle_name.get(0).toString());
                    }

                    if (error.has("last_name")){
                        JSONArray last_name = error.getJSONArray("last_name");
                        etl_lastname.setError(last_name.get(0).toString());
                    }

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
}
