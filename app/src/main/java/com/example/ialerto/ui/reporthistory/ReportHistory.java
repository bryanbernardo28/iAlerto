package com.example.ialerto.ui.reporthistory;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ialerto.MyConfig;
import com.example.ialerto.R;
import com.example.ialerto.ui.alerts.AlertsFragment;
import com.example.ialerto.ui.alerts.AlertsInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ReportHistory extends Fragment {
    RecyclerView rv_reporthistory;
    RecyclerView.Adapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report_history, container, false);

        rv_reporthistory = view.findViewById(R.id.rv_reporthistory_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_reporthistory.setLayoutManager(layoutManager);
        rv_reporthistory.setHasFixedSize(true);
        rv_reporthistory.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        rv_reporthistory.setAdapter(adapter);
        swipeRefreshLayout = view.findViewById(R.id.srl_reporthistory_id);
        swipeRefreshLayout.setOnRefreshListener(() -> new getAlerts().execute());
        swipeRefreshLayout.setRefreshing(true);
        new getAlerts().execute();

        return view;
    }

    class getAlerts extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient get_alerts_history = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/alerts/history")
                    .build();
            try {
                Response response = get_alerts_history.newCall(request).execute();
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
                Log.d("check", String.valueOf(jsonObject));
                JSONArray announcementArray = jsonObject.getJSONArray("data");
                ArrayList<ReportHistoryInfos> reportHistoryInfosArrayList = new ArrayList<>();
                ReportHistoryInfos reportHistoryInfos;

                for (int i = 0; i < announcementArray.length(); i++){
                    jsonObject = announcementArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String alert_user_name = jsonObject.getString("alert_user_name");
                    double longitude = jsonObject.getDouble("longitude");
                    double latitude = jsonObject.getDouble("latitude");
                    String address = jsonObject.getString("address");
                    String type = jsonObject.getString("type");
                    int status = jsonObject.getInt("status");
                    String time_deploy = jsonObject.getString("responded_at");
                    String created_at = jsonObject.getString("created_at");

                    reportHistoryInfos = new ReportHistoryInfos(id,alert_user_name,type,address,created_at,time_deploy,status,longitude,latitude);
                    reportHistoryInfosArrayList.add(reportHistoryInfos);
                }
                adapter = new reportHistoryAdapter(getContext(),reportHistoryInfosArrayList);
                rv_reporthistory.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private class reportHistoryAdapter extends RecyclerView.Adapter<reportHistoryAdapter.ViewHolder>{
        private ArrayList<ReportHistoryInfos> reportHistoryInfos;
        private LayoutInflater mInflater;

        public reportHistoryAdapter(Context context, ArrayList<ReportHistoryInfos> reportHistoryInfos) {
            this.mInflater = LayoutInflater.from(context);
            this.reportHistoryInfos = reportHistoryInfos;
        }

        @NonNull
        @Override
        public reportHistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.reporthistory_row_recyclerview,parent,false);
            int height = parent.getMeasuredHeight() / 4;
            view.setMinimumHeight(height);
            reportHistoryAdapter.ViewHolder vh = new reportHistoryAdapter.ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull reportHistoryAdapter.ViewHolder holder, int position) {
            final String alert_user_name = reportHistoryInfos.get(position).getAlert_user();

            String alert_id = reportHistoryInfos.get(position).getId();
            final String report_type = reportHistoryInfos.get(position).getReport_type();
            final String address = reportHistoryInfos.get(position).getReport_address();
            String string_date = reportHistoryInfos.get(position).getTime_report();
            String responded_date = reportHistoryInfos.get(position).getTime_deploy();
            double latitude = reportHistoryInfos.get(position).getLatitude();
            double longitude = reportHistoryInfos.get(position).getLongitude();
            final boolean responded;

            holder.tv_report_type.setText(report_type);
            holder.tv_address.setText(address);
            holder.tv_date.setText(string_date);

            int status = reportHistoryInfos.get(position).getStatus();
            if (status == 1){
                holder.cl_reporthistory.setBackgroundColor(Color.parseColor("#99ff99"));
                responded = true;
            }
            else{
                holder.cl_reporthistory.setBackgroundColor(Color.parseColor("#ff8566"));
                responded = false;
            }
//

            holder.cl_reporthistory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent view_reporthistory = new Intent(getActivity(), ViewReportHistory.class);
                    view_reporthistory.putExtra("alert_id",alert_id);
                    view_reporthistory.putExtra("report_type",report_type);
                    view_reporthistory.putExtra("alert_user_name",alert_user_name);
                    view_reporthistory.putExtra("report_address",address);
                    view_reporthistory.putExtra("time_report",string_date);
                    view_reporthistory.putExtra("time_deploy",responded_date);
                    view_reporthistory.putExtra("responded",responded);
                    view_reporthistory.putExtra("latitude",latitude);
                    view_reporthistory.putExtra("longitude",longitude);
                    startActivity(view_reporthistory);
                    getActivity().finish();
                }
            });


        }

        @Override
        public int getItemCount() {
            return reportHistoryInfos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_report_type,tv_address,tv_date;
            ConstraintLayout cl_reporthistory;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_report_type = itemView.findViewById(R.id.tv_report_type_id);
                tv_address = itemView.findViewById(R.id.tv_address_id);
                tv_date = itemView.findViewById(R.id.tv_date_id);
                cl_reporthistory = itemView.findViewById(R.id.cl_reporthistory_id);
            }
        }
    }
}
