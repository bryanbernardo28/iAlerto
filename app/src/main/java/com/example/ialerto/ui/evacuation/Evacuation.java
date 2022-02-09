package com.example.ialerto.ui.evacuation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class Evacuation extends Fragment {
    RecyclerView rv_evacuation;
    RecyclerView.Adapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    Button btn_add_evacuation;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_evacuation, container, false);


        SharedPreferences myprofile = getActivity().getSharedPreferences(MainActivity.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        String role = myprofile.getString("role","");
        btn_add_evacuation = view.findViewById(R.id.btn_add_evacuation_id);
        if (role.equals("relative") || role.equals("resident") || role.equals("administrator")){
            btn_add_evacuation.setVisibility(View.GONE);
        }
        else{
            btn_add_evacuation.setOnClickListener(v -> {
                Intent addEvacuationIntent = new Intent(getActivity(), AddEvacuation.class);
                startActivity(addEvacuationIntent);
                getActivity().finish();
            });
        }

        rv_evacuation = view.findViewById(R.id.rv_evacuation_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rv_evacuation.setLayoutManager(layoutManager);
        rv_evacuation.setHasFixedSize(true);
        rv_evacuation.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        rv_evacuation.setAdapter(adapter);
        swipeRefreshLayout = view.findViewById(R.id.srl_evacuation_id);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new getEvacuation().execute();
            }
        });
        swipeRefreshLayout.setRefreshing(true);
        new getEvacuation().execute();

        return view;
    }


    class getEvacuation extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/evacuation")
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
                Log.d("check", String.valueOf(jsonObject));
                JSONArray evacuationArray = jsonObject.getJSONArray("data");
                ArrayList<EvacuationInfo> evacuationInfoArrayList = new ArrayList<>();
                EvacuationInfo evacuationInfo;
                int announcement_length = evacuationArray.length();

                boolean has_data = true;

                for (int i = 0; i < announcement_length; i++){
                    JSONObject evacuationJSONObject = evacuationArray.getJSONObject(i);
                    String id = evacuationJSONObject.getString("id");
                    String name = evacuationJSONObject.getString("name");
                    String date = evacuationJSONObject.getString("date");
                    String address = evacuationJSONObject.getString("address");
                    String capacity = evacuationJSONObject.getString("capacity");
                    String is_avail = evacuationJSONObject.getString("is_avail");
                    JSONArray barangays = evacuationJSONObject.getJSONArray("barangays");
                    String status = evacuationJSONObject.getString("status");


                    evacuationInfo = new EvacuationInfo(id,name,address,capacity,status,date,is_avail,barangays);
                    evacuationInfoArrayList.add(evacuationInfo);
                }

                if (announcement_length == 0){
                    has_data = false;
                    Toast.makeText(getActivity(),"No Evacuation Center Fetch.",Toast.LENGTH_LONG).show();
                    evacuationInfo = new EvacuationInfo("","No Evacuation","");
                    evacuationInfoArrayList.add(evacuationInfo);
                }


                if (getActivity() != null){
                    adapter = new evacuationAdapter(getContext(),evacuationInfoArrayList,has_data);
                    rv_evacuation.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    class evacuationAdapter extends RecyclerView.Adapter<evacuationAdapter.ViewHolder>{
        private ArrayList<EvacuationInfo> evacuations;
        private LayoutInflater mInflater;
        boolean has_data;

        public evacuationAdapter(Context context,ArrayList<EvacuationInfo> evacuations,boolean has_data) {
            this.evacuations = evacuations;
            this.mInflater = LayoutInflater.from(context);
            this.has_data = has_data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.evacuation_row,parent,false);
            int height = parent.getMeasuredHeight() / 4;
            view.setMinimumHeight(height);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String title = evacuations.get(position).getName();
            String string_date = evacuations.get(position).getDate();
            String evac_id = evacuations.get(position).getId();
            String address = evacuations.get(position).getAddress();
            String capacity = evacuations.get(position).getCapacity();
            if (!has_data){
                holder.tv_title.setVisibility(View.GONE);
                holder.tv_content.setGravity(Gravity.CENTER);
                holder.tv_content.setTextSize(TypedValue.COMPLEX_UNIT_PX,50);
                holder.tv_content.setText(address);
            }
            else{
                holder.tv_title.setText(title);
                holder.tv_content.setText(address);
                holder.tv_date.setText(string_date);

                JSONArray barangays = evacuations.get(position).getBarangays();
                ArrayList<String> barangay_name = new ArrayList<>();
                for (int i = 0; i < barangays.length();i++){
                    try {
                        JSONObject jsonObject = barangays.getJSONObject(i);
                        barangay_name.add(jsonObject.getString("name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                holder.cl_row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ViewEvacuation.class);
                        intent.putExtra("evac_id",evac_id);
                        intent.putExtra("evac_name",title);
                        intent.putExtra("evac_barangay", barangay_name);
                        intent.putExtra("evac_address",address);
                        intent.putExtra("evac_capacity",capacity);
                        intent.putExtra("from_activity","evacuation");
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
            }



        }

        @Override
        public int getItemCount() {
            return evacuations.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_title,tv_content,tv_date;
            ConstraintLayout cl_row;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_title = itemView.findViewById(R.id.tv_subject_id);
                tv_content = itemView.findViewById(R.id.tv_content_id);
                tv_date = itemView.findViewById(R.id.tv_date_id);
                cl_row = itemView.findViewById(R.id.cl_row_id);
            }
        }
    }
}