package com.example.ialerto.ui.evacuation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ViewEvacuation extends AppCompatActivity {
    static TextView tv_name,tv_barangay,tv_address,tv_evac_capacity,tv_capacity;
    String from_activity;
    static String id;
    static Context context;
    Button btn_update_capacity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_evacuation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;
        id = getIntent().getStringExtra("evac_id");
        String name = getIntent().getStringExtra("evac_name");
        ArrayList<String> barangay_names = getIntent().getStringArrayListExtra("evac_barangay");

        String barangay = TextUtils.join(",",barangay_names);

        String address = getIntent().getStringExtra("evac_address");

        from_activity = getIntent().getStringExtra("from_activity");

        tv_name = findViewById(R.id.tv_evac_name_id);
        tv_barangay = findViewById(R.id.tv_evac_barangay_id);
        tv_address = findViewById(R.id.tv_evac_address_id);
        tv_evac_capacity = findViewById(R.id.tv_evac_capacity_id);
        tv_capacity = findViewById(R.id.tv_capcity_id);
        btn_update_capacity = findViewById(R.id.btn_update_capacity_id);

        SharedPreferences profileinfo_pref = getSharedPreferences(MainActivity.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        String role = profileinfo_pref.getString("role","");
        if (role.equals("resident") || role.equals("administrator") || role.equals("relative")){
            btn_update_capacity.setVisibility(View.GONE);
        }

        tv_name.setText(name);
        tv_barangay.setText(barangay);
        tv_address.setText(address);
        String capacity = getIntent().getStringExtra("evac_capacity");
        tv_evac_capacity.setText(capacity);
//        if (from_activity.equals("evacuation")){
//            String capacity = getIntent().getStringExtra("evac_capacity");
//            tv_evac_capacity.setText(capacity);
//        }
//        else{
//            tv_capacity.setVisibility(View.GONE);
//            tv_evac_capacity.setVisibility(View.GONE);
//        }
    }

    public void update_capacity(View v){
        UpdateCapacityDialog updateCapacity = new UpdateCapacityDialog();
        updateCapacity.show(getSupportFragmentManager(), "Update Capacity");
    }

    public static class UpdateCapacityDialog extends DialogFragment{
        TextInputLayout etl_capacity;
        TextInputEditText et_capacity;
        String capacity;
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater factory = LayoutInflater.from(getActivity());
            final View view = factory.inflate(R.layout.capacity_layout, null);
            etl_capacity = view.findViewById(R.id.etl_capacity_id);
            et_capacity = view.findViewById(R.id.et_capacity_id);
            builder.setTitle("Update Capacity");
            builder.setView(view)
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog updateCapacityDialog = builder.create();
            updateCapacityDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {

                    Button b = updateCapacityDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            capacity = et_capacity.getText().toString();
                            if (capacity.trim().equals("") || capacity.isEmpty() || Integer.parseInt(capacity) <= 0){
//                                Toast.makeText(getActivity(), "Capacity must be greater than 0.", Toast.LENGTH_SHORT).show();
                                etl_capacity.setError("Capacity must be greater than 0.");
                            }
                            else{
                                etl_capacity.setError(null);
                                new updateCapacity(capacity).execute();
                                updateCapacityDialog.dismiss();
                            }
                        }
                    });

                }
            });
            return updateCapacityDialog;
        }
    }

    static class updateCapacity extends AsyncTask<String,Void,String> {
        ProgressDialog pd;
        String capacity;

        public updateCapacity(String capacity) {
            this.capacity = capacity;
        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id",id)
                    .addFormDataPart("capacity",capacity)
                    .build();

            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/evacuation/update_capacity")
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
            pd = new ProgressDialog(context);
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
                JSONObject jsonObject = new JSONObject(s);
                Log.d("check", s);
                boolean response = jsonObject.getBoolean("success");

                if (response){
                    JSONObject evacuation = jsonObject.getJSONObject("response");
                    String capacity = evacuation.getString("capacity");
                    tv_evac_capacity.setText(capacity);
                    Toast.makeText(context, "Update capacity successful", Toast.LENGTH_SHORT).show();

                }
                else{
                    JSONObject error = jsonObject.getJSONObject("response");
                    if (error.has("capacity")){
                        JSONArray capacity = error.getJSONArray("capacity");
                        Toast.makeText(context,capacity.get(0).toString(),Toast.LENGTH_LONG).show();
                    }
//
//                    Toast.makeText(getActivity(),"Please fill out all required fields.",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, Dashboard.class);
        intent.putExtra("from_activity",from_activity);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}