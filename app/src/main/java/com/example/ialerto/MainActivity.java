package com.example.ialerto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ialerto.employee.prereg.Register;
import com.example.ialerto.resident.alert.AlertFragment;
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

public class MainActivity extends AppCompatActivity {
    public static String LOGINLOGOUTPREF_NAME = "loginlogoutpref";
    public static String PROFILEPREF_NAME = "profilespref";
    private TextView tv_error;
    private TextInputLayout etl_email,etl_password;
    private TextInputEditText et_email,et_password;
    public static SharedPreferences loginlogout_pref,profileinfo_pref;
    private static Context context;
    static JSONObject user;
    static JSONArray relatives;

    TextView tv_verify;

    static String[] accounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        tv_error = findViewById(R.id.tv_error_id);
        etl_email = findViewById(R.id.etl_email_id);
        etl_password = findViewById(R.id.etl_password_id);

        et_email = findViewById(R.id.et_email_id);
        et_password = findViewById(R.id.et_password_id);

//        et_email.setText("one@employee.com");
//        et_password.setText("secret");

        tv_verify = findViewById(R.id.tv_verify_id);
        tv_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,VerifyAccount.class);
                startActivity(intent);
                finish();
            }
        });

//        et_email.setText("admin@admin.com");
//        et_password.setText("secret");

//        et_email.setText("employee@employee.com");
//        et_password.setText("secret");

        loginlogout_pref = getSharedPreferences(LOGINLOGOUTPREF_NAME, Context.MODE_PRIVATE);
        profileinfo_pref = getSharedPreferences(PROFILEPREF_NAME,Context.MODE_PRIVATE);


        boolean loggedin = loginlogout_pref.getBoolean("loggedin",false);
        Log.d("check", String.valueOf(loggedin));
        if (loggedin){
            Intent godashboard = new Intent(MainActivity.this, Dashboard.class);
            startActivity(godashboard);
            finish();
        }


    }


    class loginSubmit extends AsyncTask<String,Void,String>{
        ProgressDialog pd;
        String email = et_email.getText().toString() ;
        String password = et_password.getText().toString();
        @Override
        protected String doInBackground(String... strings) {
            Log.d("check",email);
            Log.d("check",password);

            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("email",email)
                    .addFormDataPart("password",password)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/login_request/login_submit")
                    .post(requestBody)
                    .build();
            try {
                Response response = getstudents.newCall(request).execute();
                Log.d("check", String.valueOf(response.code()));
//                Log.d("check", response.body().string());
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
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                Log.d("checkRes", s);
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");
                if (response){
                    user = jsonObject.getJSONObject("response");


                    if (user.has("relatives")){
                        String user_email = user.getString("email");
                        String user_id = user.getString("user_id");
                        relatives = user.getJSONArray("relatives");
                        user.remove("relatives");
                        relatives.put(user);
                        int relatives_length = relatives.length();
                        accounts = new String[relatives_length];
                        for (int i = 0;i < relatives_length; i++){
                            user = relatives.getJSONObject(i);
                            user.put("email",user_email);
                            user.put("id",user_id);
                            String name = user.getString("name");
                            accounts[i] = name;
                        }
                        ChooseAccountFragment accountFragment = new ChooseAccountFragment();
                        accountFragment.show(getSupportFragmentManager(), "My Dialog");
                    }
                    else{
                        loginlogout_pref = getSharedPreferences(LOGINLOGOUTPREF_NAME,Context.MODE_PRIVATE);
                        SharedPreferences.Editor loginlogout_pref_editor = loginlogout_pref.edit();
                        loginlogout_pref_editor.putBoolean("loggedin",true);
                        loginlogout_pref_editor.apply();


                        String id = user.getString("id");
                        String name = user.getString("name");
                        String first_name = user.getString("first_name");
                        String middle_name = user.getString("middle_name");
                        String last_name = user.getString("last_name");
                        String email = user.getString("email");
                        String role = user.getString("role");


                        profileinfo_pref = getSharedPreferences(PROFILEPREF_NAME,Context.MODE_PRIVATE);
                        SharedPreferences.Editor profileinfo_pref_editor = profileinfo_pref.edit();
                        profileinfo_pref_editor.putString("id",id);
                        profileinfo_pref_editor.putString("first_name",first_name);
                        profileinfo_pref_editor.putString("middle_name",middle_name);
                        profileinfo_pref_editor.putString("last_name",last_name);
                        profileinfo_pref_editor.putString("email",email);
                        profileinfo_pref_editor.putString("role",role);
                        profileinfo_pref_editor.apply();
                        Intent goDashboard = new Intent(MainActivity.this,Dashboard.class);
                        startActivity(goDashboard);
                        finish();
                    }


                    tv_error.setText("");

//                    Intent goDashboard = new Intent(MainActivity.this,Dashboard.class);
//                    startActivity(goDashboard);
//                    finish();

                }
                else{
                    String error = jsonObject.getString("response");
                    tv_error.setVisibility(TextView.VISIBLE);
                    tv_error.setText(error);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }

    public static class ChooseAccountFragment extends DialogFragment {
        ArrayList selectedItems;
        String selectedAccount = null;
        int account_index = -1;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            selectedItems = new ArrayList();  // Where we track the selected items
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//            String[] disasters = getActivity().getResources().getStringArray(R.array.disaster_array);
            // Set the dialog title
            builder.setTitle("Choose Account");
            builder.setSingleChoiceItems(accounts, -1, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    account_index = which;
                    selectedAccount = accounts[which];
                    AlertDialog alertDialog = (AlertDialog) getDialog();
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }).setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        int relatives_length = relatives.length();
                        for (int i = 0;i < relatives_length; i++){
                            user = relatives.getJSONObject(i);
                            if (user.getString("name").equals(selectedAccount)){
                                break;
                            }
                        }
                        Log.d("check",selectedAccount);
                        Log.d("check", String.valueOf(user));
                        loginlogout_pref = context.getSharedPreferences(LOGINLOGOUTPREF_NAME,Context.MODE_PRIVATE);
                        SharedPreferences.Editor loginlogout_pref_editor = loginlogout_pref.edit();
                        loginlogout_pref_editor.putBoolean("loggedin",true);
                        loginlogout_pref_editor.apply();


                        String id = user.getString("id");
                        String name = user.getString("name");
                        String first_name = user.getString("first_name");
                        String middle_name = user.getString("middle_name");
                        String last_name = user.getString("last_name");
                        String email = user.getString("email");
                        String role = user.has("role") ? user.getString("role") : "relative" ;


                        profileinfo_pref = context.getSharedPreferences(PROFILEPREF_NAME,Context.MODE_PRIVATE);
                        SharedPreferences.Editor profileinfo_pref_editor = profileinfo_pref.edit();
                        profileinfo_pref_editor.putString("id",id);
                        profileinfo_pref_editor.putString("first_name",first_name);
                        profileinfo_pref_editor.putString("middle_name",middle_name);
                        profileinfo_pref_editor.putString("last_name",last_name);
                        profileinfo_pref_editor.putString("email",email);
                        profileinfo_pref_editor.putString("role",role);
                        profileinfo_pref_editor.apply();

                        Intent goDashboard = new Intent(context,Dashboard.class);
                        startActivity(goDashboard);
                        getActivity().finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

            AlertDialog accountDialog = builder.create();
            accountDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = accountDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setEnabled(false);
                }
            });

            return accountDialog;
        }
    }


    public void goLogin(View v){
        tv_error.setVisibility(TextView.INVISIBLE);
        new loginSubmit().execute();
    }

    public void goRegister(View v){
        Intent register = new Intent(this, Register.class);
        startActivity(register);
        finish();
    }

}
