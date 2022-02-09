package com.example.ialerto.ui.announcement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ialerto.Dashboard;
import com.example.ialerto.R;
import com.example.ialerto.ui.evacuation.EvacuationInfo;
import com.example.ialerto.ui.evacuation.ViewEvacuation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ViewAnnouncement extends AppCompatActivity {
    TextView tv_title,tv_details,tv_date;
    RecyclerView rv_evacuation;
    RecyclerView.Adapter adapter;
    LinearLayout ll_evac_center;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_announcement);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ll_evac_center = findViewById(R.id.ll_evac_center_id);

        rv_evacuation = findViewById(R.id.rv_evacuation_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv_evacuation.setLayoutManager(layoutManager);
        rv_evacuation.setHasFixedSize(true);
        rv_evacuation.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        rv_evacuation.setAdapter(adapter);

        boolean has_evacuation = getIntent().getBooleanExtra("is_evacuation",false);
        if (!has_evacuation){
            ll_evac_center.setVisibility(View.GONE);
        }
        else{
            try {
                JSONArray evacuation = new JSONArray(getIntent().getStringExtra("evacuation"));
                ArrayList<EvacuationInfo> evacuationInfoArrayList = new ArrayList<>();
                EvacuationInfo evacuationInfo;
                for (int i = 0;i < evacuation.length();i++){
                    JSONObject jsonObject = evacuation.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    String date = jsonObject.getString("date");
                    String address = jsonObject.getString("address");
                    String capacity = jsonObject.getString("capacity");
                    String is_avail = jsonObject.getString("is_avail");
                    JSONArray barangays = jsonObject.getJSONArray("barangays");
                    String status = jsonObject.getString("status");

                    evacuationInfo = new EvacuationInfo(id,name,address,capacity,status,date,is_avail,barangays);
                    evacuationInfoArrayList.add(evacuationInfo);
                }

                adapter = new evacuationAdapter(this,evacuationInfoArrayList);
                rv_evacuation.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        tv_details = findViewById(R.id.tv_content_id);
        tv_details.setMovementMethod(new ScrollingMovementMethod());
        tv_title = findViewById(R.id.tv_subject_id);
        tv_date = findViewById(R.id.tv_date_id);

        String title = getIntent().getStringExtra("title");
        tv_title.setText(title);

        String details = getIntent().getStringExtra("details");
        tv_details.setText(details);

        String date = getIntent().getStringExtra("date");
        tv_date.setText(date);
    }

    class evacuationAdapter extends RecyclerView.Adapter<evacuationAdapter.ViewHolder>{
        private ArrayList<EvacuationInfo> evacuations;
        private LayoutInflater mInflater;

        public evacuationAdapter(Context context,ArrayList<EvacuationInfo> evacuations) {
            this.evacuations = evacuations;
            this.mInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public evacuationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.evacuation_row,parent,false);
            int height = parent.getMeasuredHeight() / 4;
            view.setMinimumHeight(height);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull evacuationAdapter.ViewHolder holder, int position) {
            String title = evacuations.get(position).getName();
//            String string_date = evacuations.get(position).getDate();
            String evac_id = evacuations.get(position).getId();
            String address = evacuations.get(position).getAddress();
            String capacity = evacuations.get(position).getCapacity();
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

            holder.tv_title.setText(title);
            holder.tv_content.setText(address);
//            holder.tv_date.setText(string_date);
            holder.cl_row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ViewAnnouncement.this, ViewEvacuation.class);
                    intent.putExtra("evac_id",evac_id);
                    intent.putExtra("evac_name",title);
                    intent.putExtra("evac_barangay", barangay_name);
                    intent.putExtra("evac_address",address);
                    intent.putExtra("evac_capacity",capacity);
                    intent.putExtra("from_activity","announcement");
                    startActivity(intent);
                    finish();
                }
            });
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
//                tv_date = itemView.findViewById(R.id.tv_date_id);
                cl_row = itemView.findViewById(R.id.cl_row_id);
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","announcement");
        startActivity(goback);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","announcement");
        startActivity(goback);
        finish();
        return true;
    }
}
