package com.example.ialerto.ui.alerts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.ialerto.MainActivity;
import com.example.ialerto.MyConfig;
import com.example.ialerto.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AlertsFragment extends Fragment {
    RecyclerView rv_alerts;
    RecyclerView.Adapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    String url = MyConfig.base_url+"/alert";
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_alerts, container, false);

        SharedPreferences myprofile = getActivity().getSharedPreferences(MainActivity.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        String role = myprofile.getString("role","");
        String id = myprofile.getString("id","");
        if (role.equals("resident") || role.equals("relative")){
            url = MyConfig.base_url+"/alert/get/my_alerts/"+id;
        }

        rv_alerts = view.findViewById(R.id.rv_alerts_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_alerts.setLayoutManager(layoutManager);
        rv_alerts.setHasFixedSize(true);
        rv_alerts.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        rv_alerts.setAdapter(adapter);
        swipeRefreshLayout = view.findViewById(R.id.srl_alerts_id);
        swipeRefreshLayout.setOnRefreshListener(() -> new getAlerts(url).execute());
        swipeRefreshLayout.setRefreshing(true);
        new getAlerts(url).execute();
        return view;
    }

    class getAlerts extends AsyncTask<String,Void,String> {
        String my_url;

        public getAlerts(String my_url) {
            this.my_url = my_url;
        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
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
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray announcementArray = jsonObject.getJSONArray("data");
                ArrayList<AlertsInfo> alertsInfoArrayList = new ArrayList<>();
                AlertsInfo alertsInfo;

                for (int i = 0; i < announcementArray.length(); i++){
                    jsonObject = announcementArray.getJSONObject(i);
                    String id = jsonObject.getString("alert_id");
                    String title = jsonObject.getString("name");
//                    String first_name = jsonObject.getString("first_name");
//                    String middle_name = jsonObject.getString("middle_name");
//                    String last_name = jsonObject.getString("last_name");
//                    String title = first_name + " " + middle_name + " " +last_name;
//                    title = title.trim().replaceAll(" ", " ");
                    String contact_number = jsonObject.getString("contact_number");
                    String address = jsonObject.getString("address");
                    double latitude = jsonObject.getDouble("latitude");
                    double longitude = jsonObject.getDouble("longitude");
                    String type = jsonObject.getString("type");
                    String created_at = jsonObject.getString("created_at");

                    alertsInfo = new AlertsInfo(id,title,contact_number,address,type,created_at,longitude,latitude);
                    alertsInfoArrayList.add(alertsInfo);
                }
                adapter = new alertsAdapter(getContext(),alertsInfoArrayList);
                rv_alerts.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class alertsAdapter extends RecyclerView.Adapter<alertsAdapter.ViewHolder>{
        private ArrayList<AlertsInfo> alertsInfos;
        private LayoutInflater mInflater;

        public alertsAdapter(Context context, ArrayList<AlertsInfo> alertsInfos) {
            this.mInflater = LayoutInflater.from(context);
            this.alertsInfos = alertsInfos;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.alerts_row_recyclerview,parent,false);
            int height = parent.getMeasuredHeight() / 4;
            view.setMinimumHeight(height);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String alert_id = alertsInfos.get(position).getId();
            final String title = String.valueOf(alertsInfos.get(position).getName());
            String contact_number = alertsInfos.get(position).getContact_number();
            final String address = alertsInfos.get(position).getAddress();
            String type = alertsInfos.get(position).getType();
            String string_date = alertsInfos.get(position).getDate();
            double latitude = alertsInfos.get(position).getLatitude();
            double longitude = alertsInfos.get(position).getLongitude();

            holder.tv_name.setText(title);
            holder.tv_details.setText(address);
            holder.tv_date.setText(string_date);


            holder.cl_alerts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent view_alert = new Intent(getActivity(), AlertsView.class);
                    view_alert.putExtra("id",alert_id);
                    view_alert.putExtra("title",title);
                    view_alert.putExtra("contact_number",contact_number);
                    view_alert.putExtra("address",address);
                    view_alert.putExtra("type",type);
                    view_alert.putExtra("latitude",latitude);
                    view_alert.putExtra("longitude",longitude);
                    view_alert.putExtra("date",string_date);
                    startActivity(view_alert);
                    getActivity().finish();
//                    Intent chat = new Intent(getActivity(), AlertChat.class);
//                    chat.putExtra("alert_id",alert_id);
//                    startActivity(chat);
//                    getActivity().finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            return alertsInfos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_name,tv_details,tv_date;
            ConstraintLayout cl_alerts;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_name = itemView.findViewById(R.id.tv_subject_id);
                tv_details = itemView.findViewById(R.id.tv_content_id);
                tv_date = itemView.findViewById(R.id.tv_date_id);
                cl_alerts = itemView.findViewById(R.id.cl_alerts_id);
            }
        }
    }
}
