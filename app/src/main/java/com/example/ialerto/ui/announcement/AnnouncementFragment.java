package com.example.ialerto.ui.announcement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.ialerto.employee.announcement.AddAnnouncement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AnnouncementFragment extends Fragment {
    RecyclerView rv_announcement;
    RecyclerView.Adapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    Button add_announcement;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_announcement, container, false);

        SharedPreferences myprofile = getActivity().getSharedPreferences(MainActivity.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        String role = myprofile.getString("role","");
        add_announcement = root.findViewById(R.id.btn_AddAnnouncement_id);
        if (role.equals("relative") || role.equals("resident") || role.equals("administrator")){
            add_announcement.setVisibility(View.GONE);
        }
        else{
            add_announcement.setOnClickListener(v -> {
                Intent addAnnouncement = new Intent(getActivity(), AddAnnouncement.class);
                startActivity(addAnnouncement);
                getActivity().finish();
            });
        }

        rv_announcement = root.findViewById(R.id.rv_announcement_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_announcement.setLayoutManager(layoutManager);
        rv_announcement.setHasFixedSize(true);
        rv_announcement.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        rv_announcement.setAdapter(adapter);
        swipeRefreshLayout = root.findViewById(R.id.srl_announcement_id);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new getAnnouncement().execute();
            }
        });
        swipeRefreshLayout.setRefreshing(true);
        new getAnnouncement().execute();

        return root;
    }


    class getAnnouncement extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/announcements")
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
                ArrayList<AnnouncementInfo> announcementArrayList = new ArrayList<>();
                AnnouncementInfo announcementInfo;
                int announcement_length = announcementArray.length();

                boolean has_data = true;

                for (int i = 0; i < announcement_length; i++){
                    jsonObject = announcementArray.getJSONObject(i);
                    String title = jsonObject.getString("title");
                    String details = jsonObject.getString("details");
                    String created_at = jsonObject.getString("created_at");
                    JSONArray evacuations = jsonObject.getJSONArray("evacuations");

                    announcementInfo = new AnnouncementInfo(title,details,created_at,evacuations);
                    announcementArrayList.add(announcementInfo);
                }

                if (announcement_length == 0){
                    has_data = false;
                    Toast.makeText(getActivity(),"No Announcement Fetch.",Toast.LENGTH_LONG).show();
                    announcementInfo = new AnnouncementInfo("","No Announcement","");
                    announcementArrayList.add(announcementInfo);
                }


                if (getActivity() != null){
                    adapter = new announcementAdapter(getContext(),announcementArrayList,has_data);
                    rv_announcement.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    class announcementAdapter extends RecyclerView.Adapter<announcementAdapter.ViewHolder>{
        private ArrayList<AnnouncementInfo> announcementInfos;
        private LayoutInflater mInflater;
        boolean has_data;

        public announcementAdapter(Context context,ArrayList<AnnouncementInfo> announcementInfos,boolean has_data) {
            this.announcementInfos = announcementInfos;
            this.mInflater = LayoutInflater.from(context);
            this.has_data = has_data;
        }

        @NonNull
        @Override
        public announcementAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.announcement_row_recyclerview,parent,false);
            int height = parent.getMeasuredHeight() / 4;
            view.setMinimumHeight(height);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            final String title = String.valueOf(announcementInfos.get(position).title);
            final String details = announcementInfos.get(position).detailes;
            String string_date = announcementInfos.get(position).created_at;
            JSONArray evacuations = announcementInfos.get(position).getEvacuations();
            int evac_length = evacuations.length();
            final boolean is_evacuation;

            if (evac_length > 0){
                is_evacuation = true;
            }
            else{
                is_evacuation = false;
            }
            if (!has_data){
                holder.tv_title.setVisibility(View.GONE);
                holder.tv_details.setGravity(Gravity.CENTER);
                holder.tv_details.setTextSize(TypedValue.COMPLEX_UNIT_PX,50);
                holder.tv_details.setText(details);
                Log.d("announcement", String.valueOf(announcementInfos.size()));
            }
            else{
                holder.tv_title.setText(title);
                holder.tv_details.setText(details);
                //2020-01-26 15:05:21
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Manila"));
                String date = null;
                try {
                    date = dateFormat.format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(string_date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                holder.tv_date.setText(date);

                String finalDate = date;
                holder.cl_row.setOnClickListener((View v) -> {
                    Intent view_announcement = new Intent(getActivity(),ViewAnnouncement.class);
                    view_announcement.putExtra("title",title);
                    view_announcement.putExtra("details",details);
                    view_announcement.putExtra("date", finalDate);
                    view_announcement.putExtra("is_evacuation", is_evacuation);
                    view_announcement.putExtra("evacuation", String.valueOf(evacuations));
                    startActivity(view_announcement);
                    getActivity().finish();
                });
            }


        }

        @Override
        public int getItemCount() {
            return announcementInfos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_title,tv_details,tv_date;
            ConstraintLayout cl_row;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_title = itemView.findViewById(R.id.tv_subject_id);
                tv_details = itemView.findViewById(R.id.tv_content_id);
                tv_date = itemView.findViewById(R.id.tv_date_id);
                cl_row = itemView.findViewById(R.id.cl_row_id);
            }
        }
    }

}
