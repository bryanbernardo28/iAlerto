package com.example.ialerto.ui.alerts;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ialerto.Dashboard;
import com.example.ialerto.MainActivity;
import com.example.ialerto.MyConfig;
import com.example.ialerto.R;
import com.example.ialerto.resident.alert.AlertFragment;
import com.example.ialerto.ui.AlertChat;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AlertsView extends AppCompatActivity {
    static TextView tv_title,tv_details,tv_date;
    static String alert_id,title,contact_number,address,type,date,user_id;
    static double latitude,longitude;
    Button btn_respond,btn_chat;
    String role;
    static Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        activity = this;

        SharedPreferences myprofile = getSharedPreferences(MainActivity.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        user_id = myprofile.getString("id","");
        alert_id = getIntent().getStringExtra("id");

        new getChatStatus().execute();

        tv_details = findViewById(R.id.tv_content_id);
        tv_details.setMovementMethod(new ScrollingMovementMethod());
        tv_title = findViewById(R.id.tv_subject_id);
        tv_date = findViewById(R.id.tv_date_id);

        title = getIntent().getStringExtra("title");
        tv_title.setText(title);

        contact_number = getIntent().getStringExtra("contact_number");
        address = getIntent().getStringExtra("address");
        type = getIntent().getStringExtra("type");
        String details = "Address: " + address + "\n";
        details += "Contact Number: " + contact_number + "\n";
        details += "Type : " + type;
        tv_details.setText(details);

        date = getIntent().getStringExtra("date");
        tv_date.setText(date);

        btn_respond = findViewById(R.id.btn_respond_id);
        role = myprofile.getString("role","");

        if (role.equals("relative") || role.equals("resident")){
            btn_respond.setVisibility(View.GONE);
        }
        btn_chat = findViewById(R.id.btn_chat_id);
        btn_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chat = new Intent(AlertsView.this, AlertChat.class);
                chat.putExtra("alert_id",alert_id);
                chat.putExtra("from","alertsview");
                startActivity(chat);
                finish();
            }
        });


    }

    public void respond(View v){
//        latitude = getIntent().getDoubleExtra("latitude",0);
//        longitude = getIntent().getDoubleExtra("longitude",0);
//        Toast.makeText(activity, "Alert ID:" + alert_id+", User ID: "+user_id, Toast.LENGTH_SHORT).show();
        ChooseAutoReplyFragment autoReplyFragment = new ChooseAutoReplyFragment();
        autoReplyFragment.show(getSupportFragmentManager(), "My Auto Reply Message");
    }

    static class respond extends AsyncTask<String,Void,String> {
        ProgressDialog pd;
        String auto_reply;

        public respond(String auto_reply) {
            this.auto_reply = auto_reply;
        }

        @Override
        protected String doInBackground(String... strings) {

            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("status","1")
                    .add("auto_reply",auto_reply)
                    .add("user_id",user_id)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/alert/"+alert_id)
                    .patch(requestBody)
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
            pd = new ProgressDialog(activity);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                Log.d("checkResult", s);
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");
                if (response){
                    JSONObject alert = jsonObject.getJSONObject("response");
                    JSONObject user = alert.getJSONObject("user");

                    latitude = alert.getDouble("latitude");
                    longitude = alert.getDouble("longitude");

                    Intent intent = new Intent(activity,AlertRoute.class);
                    intent.putExtra("latitude",latitude);
                    intent.putExtra("longitude",longitude);
                    activity.startActivity(intent);
                    activity.finish();

                }
                else{
//                    JSONObject res = jsonObject.getJSONObject("response");
//                    String error = res.getJSONArray("status").get(0).toString();
                    String error = "Unable to respond.";
                    Toast.makeText(activity,error,Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }

    class getChatStatus extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            String segment = alert_id+"/"+user_id+"/"+role;
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/alert/chat/conversation_status/"+segment)
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("checkResult", s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean has_chat = jsonObject.getBoolean("has_chat");
                boolean is_empty = jsonObject.getBoolean("is_empty");
                if (is_empty){
                    btn_chat.setEnabled(true);
                }
                else{
                    if (has_chat){
                        btn_chat.setEnabled(true);
                    }
                    else{
                        String user_role = jsonObject.getString("role");
                        if (user_role.equals("resident")){
                            btn_chat.setEnabled(true);
                        }
                        else{
                            btn_chat.setEnabled(false);
                        }
                    }
                }
                Log.d("checkResult" , "Has Chat: " + has_chat);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ChooseAutoReplyFragment extends DialogFragment {
        String selectedAutoReply = null;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            String[] auto_reply_messages = getResources().getStringArray(R.array.auto_reply_messages);
            // Set the dialog title
            builder.setTitle("Choose Respond Type");
            builder.setItems(R.array.auto_reply_messages, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            selectedAutoReply = auto_reply_messages[which];
                            new respond(selectedAutoReply).execute();
                        }
                    });
            return builder.create();
        }
    }


    @Override
    public void onBackPressed() {
        goback();
    }

    @Override
    public boolean onSupportNavigateUp() {
        goback();
        return true;
    }

    public void goback(){
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","alerts");
        startActivity(goback);
        finish();
    }
}
