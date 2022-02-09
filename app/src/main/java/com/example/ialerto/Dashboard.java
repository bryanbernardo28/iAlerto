package com.example.ialerto;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ialerto.ui.evacuation.AddEvacuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Dashboard extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences profilepref = getSharedPreferences(MainActivity.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        String id = profilepref.getString("id","");
//        Toast.makeText(this, "ID: " + id, Toast.LENGTH_SHORT).show();

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String fcm_token = instanceIdResult.getToken();
                Log.d("checkResDashboard",fcm_token);

                new updateToken(id,fcm_token).execute();
            }
        });
        FirebaseMessaging.getInstance().unsubscribeFromTopic("announcement");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("alert");

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_alerts,
                R.id.nav_alert,
                R.id.nav_announcement,
                R.id.nav_evacuation,
                R.id.nav_profile,
                R.id.nav_prereg,
                R.id.nav_reporthistory
        )
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);



        navigationView.getMenu().findItem(R.id.nav_signout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                drawer.closeDrawers();
                new removeToken(id).execute();
                return true;
            }
        });
        SharedPreferences myprofile = getSharedPreferences(MainActivity.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        String role = myprofile.getString("role","");

        if (role.equals("administrator")){
            navigationView.getMenu().findItem(R.id.nav_alerts).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_alert).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_announcement).setVisible(false);
            navController.navigate(R.id.nav_profile);

        }
        else if (role.equals("resident") || role.equals("relative")){
            navigationView.getMenu().findItem(R.id.nav_reporthistory).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_profile).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_prereg).setVisible(false);
            navController.navigate(R.id.nav_alert);
//            FirebaseMessaging.getInstance().subscribeToTopic("announcement");

//            navController.navigate(R.id.nav_profile);
        }
        else if (role.equals("employee") || role.equals("official")){
            navigationView.getMenu().findItem(R.id.nav_alert).setVisible(false);
//            navigationView.getMenu().findItem(R.id.nav_alerts).setVisible(false);
            navigationView.getMenu().findItem(R.id.nav_profile).setVisible(false);
            navController.navigate(R.id.nav_alerts);
//            FirebaseMessaging.getInstance().subscribeToTopic("alert");
//            FirebaseMessaging.getInstance().subscribeToTopic("announcement");
        }

        String from_view = getIntent().getStringExtra("from_activity");
        if (from_view != null){
            if (from_view.equals("announcement") || from_view.equals("announcement_notif")){
                navController.navigate(R.id.nav_announcement);
            }
            else if (from_view.equals("profile")){
                navController.navigate(R.id.nav_profile);
            }
            else if (from_view.equals("reporthistory") || from_view.equals("alert_route") || from_view.equals("reporthistory")){
                navController.navigate(R.id.nav_reporthistory);
            }
            else if (from_view.equals("alerts") || from_view.equals("alert_notif") || from_view.equals("alertsview")){
                navController.navigate(R.id.nav_alerts);
            }
            else if(from_view.equals("evacuation_notif") || from_view.equals("evacuation")){
                navController.navigate(R.id.nav_evacuation);
            }
            else if (from_view.equals("prereg")){
                navController.navigate(R.id.nav_prereg);
            }
        }

        View hView = navigationView.getHeaderView(0);
        TextView tv_name = hView.findViewById(R.id.tv_name_id);
        TextView tv_email = hView.findViewById(R.id.tv_email_id);
        String first_name = myprofile.getString("first_name","");
        String middle_name = myprofile.getString("middle_name","");
        String last_name = myprofile.getString("last_name","");
        String name = first_name + " " + middle_name + " " +last_name;
        name = name.trim().replaceAll(" ", " ");
        String email = myprofile.getString("email","");
        tv_name.setText(name);
        tv_email.setText(email);
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    class updateToken extends AsyncTask<String,Void,String> {
        String id,fcm_token;

        public updateToken(String id, String fcm_token) {
            this.id = id;
            this.fcm_token = fcm_token;
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d("check",id);
            Log.d("check",fcm_token);

            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id",id)
                    .addFormDataPart("fcm_token",fcm_token)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/login/update/update_token")
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
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                Log.d("checkResDashboard", s);
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");
                if (response){
                    String fcm_token = jsonObject.getString("response");

                    SharedPreferences profileinfo_pref = getSharedPreferences(MainActivity.PROFILEPREF_NAME,Context.MODE_PRIVATE);
                    SharedPreferences.Editor profileinfo_pref_editor = profileinfo_pref.edit();
                    profileinfo_pref_editor.putString("fcm_token",fcm_token);
                    profileinfo_pref_editor.apply();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class removeToken extends AsyncTask<String,Void,String> {
        String id;
        ProgressDialog pd;
        public removeToken(String id) {
            this.id = id;
        }

        @Override
        protected String doInBackground(String... strings) {
            Log.d("check",id);

            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("id",id)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/login/update/remove_token")
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
            pd = new ProgressDialog(Dashboard.this);
            pd.setMessage("Logging out...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                Log.d("checkRes", s);
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");
                if (response){
                    String user = jsonObject.getString("response");

                    SharedPreferences loginlogout_pref,profileinfo_pref;
                    loginlogout_pref = getSharedPreferences(MainActivity.LOGINLOGOUTPREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor loginlogoutedit = loginlogout_pref.edit();
                    loginlogoutedit.clear();

                    profileinfo_pref = getSharedPreferences(MainActivity.PROFILEPREF_NAME, Context.MODE_PRIVATE);
                    String role = profileinfo_pref.getString("role","");

                    SharedPreferences.Editor profileinfo_prefedit = profileinfo_pref.edit();
                    profileinfo_prefedit.clear();
                    if (loginlogoutedit.commit()){
                        loginlogoutedit.commit();
                        profileinfo_prefedit.commit();
                        loginlogoutedit.apply();
                        profileinfo_prefedit.apply();

                        Intent logout_intent = new Intent(Dashboard.this,MainActivity.class);
                        startActivity(logout_intent);
                        finish();
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
