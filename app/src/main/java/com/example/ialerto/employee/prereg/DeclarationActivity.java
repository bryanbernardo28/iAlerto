package com.example.ialerto.employee.prereg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost;
import com.example.ialerto.Globals;
import com.example.ialerto.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class DeclarationActivity extends AppCompatActivity {
    RecyclerView rv_declarations;
    RecyclerView.Adapter adapter;
    private final int DECLARATION_REQUEST_CODE = 1;
    boolean has_declaration;
    String declaration;
    Button btn_add_declaration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_declaration);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_add_declaration = findViewById(R.id.btn_add_declaration_id);

        has_declaration = getIntent().getBooleanExtra("has_declaration",false);
        if (has_declaration){
            try {
                declaration = getIntent().getStringExtra("declarations");
                JSONArray declarationArray = new JSONArray(declaration);
                if (declarationArray.length() > 2){
                    btn_add_declaration.setEnabled(false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        rv_declarations = findViewById(R.id.rv_declarations_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rv_declarations.setLayoutManager(layoutManager);
        rv_declarations.setHasFixedSize(true);
        rv_declarations.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        rv_declarations.setAdapter(adapter);

        if(has_declaration){
            try {
                declaration = getIntent().getStringExtra("declarations");
                JSONArray declarationArray = new JSONArray(declaration);
                ArrayList<DeclarationInfo> declarationInfoArrayList = new ArrayList<>();
                DeclarationInfo declarationInfo;
                int declaration_length = declarationArray.length();

                for (int i = 0; i < declaration_length; i++){
                    JSONObject jsonObject = declarationArray.getJSONObject(i);
                    String firstname = jsonObject.getString("firstname");
                    String middlename = jsonObject.getString("middlename");
                    String lastname = jsonObject.getString("lastname");
                    String birthdate = jsonObject.getString("birthdate");

                    declarationInfo = new DeclarationInfo(firstname,middlename,lastname,birthdate);
                    declarationInfoArrayList.add(declarationInfo);
                }


                adapter = new declarationAdapter(this,declarationInfoArrayList);
                rv_declarations.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    class declarationAdapter extends RecyclerView.Adapter<declarationAdapter.ViewHolder>{
        private ArrayList<DeclarationInfo> declarationInfos;
        private LayoutInflater mInflater;

        public declarationAdapter(Context context, ArrayList<DeclarationInfo> declarationInfos) {
            this.declarationInfos = declarationInfos;
            this.mInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.from(parent.getContext()).inflate(R.layout.declaration_row,parent,false);
            int height = parent.getMeasuredHeight() / 4;
            view.setMinimumHeight(height);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            String firstname = declarationInfos.get(position).getFirstname();
            String middlename = declarationInfos.get(position).getMiddlename();
            String lastname = declarationInfos.get(position).getLastname();
            String birthdate = declarationInfos.get(position).getBirthdate();

            long longbirthdate = Long.parseLong(birthdate);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(longbirthdate);

            SimpleDateFormat format = new SimpleDateFormat("LLLL dd,yyyy");
            String strDate = format.format(calendar.getTime());


            holder.tv_firstname.setText(firstname);
            holder.tv_middlename.setText(middlename);
            holder.tv_lastname.setText(lastname);
            holder.tv_birthdate.setText(strDate);
            //2020-01-26 15:05:21
//            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy hh:mm a");
//            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
//            String date = null;
//            try {
//                date = dateFormat.format(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(string_date));
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            holder.tv_date.setText(date);


        }

        @Override
        public int getItemCount() {
            return declarationInfos.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_firstname,tv_middlename,tv_lastname,tv_birthdate;
            ConstraintLayout cl_row;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tv_firstname = itemView.findViewById(R.id.tv_firstname_id);
                tv_middlename = itemView.findViewById(R.id.tv_middlename_id);
                tv_lastname = itemView.findViewById(R.id.tv_lastname_id);
                tv_birthdate = itemView.findViewById(R.id.tv_birthdate_id);
                cl_row = itemView.findViewById(R.id.cl_row_id);
            }
        }
    }

    public void submit(View v){
        Intent i = new Intent();
        i.putExtra("has_declaration",has_declaration);
        i.putExtra("declarations", declaration);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    public void add_declaration(View v){
        Intent i = new Intent(DeclarationActivity.this,AddDeclaration.class);
        if (has_declaration){
            i.putExtra("declarations",declaration);
            i.putExtra("has_declaration",has_declaration);
        }
        startActivityForResult(i,DECLARATION_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED){
            switch (requestCode){
                case DECLARATION_REQUEST_CODE:
                    try {
                        has_declaration = data.getBooleanExtra("has_declaration",false);
                        Log.d("check","Has Declaration: " + has_declaration);
                        declaration = data.getStringExtra("declarations");
                        if (has_declaration){
                            JSONArray declarationArray = new JSONArray(declaration);
                            if (declarationArray.length() > 2){
                                btn_add_declaration.setEnabled(false);
                            }
                            ArrayList<DeclarationInfo> declarationInfoArrayList = new ArrayList<>();
                            DeclarationInfo declarationInfo;
                            int declaration_length = declarationArray.length();

                            for (int i = 0; i < declaration_length; i++){
                                JSONObject jsonObject = declarationArray.getJSONObject(i);
                                String firstname = jsonObject.getString("firstname");
                                String middlename = jsonObject.getString("middlename");
                                String lastname = jsonObject.getString("lastname");
                                String birthdate = jsonObject.getString("birthdate");

                                declarationInfo = new DeclarationInfo(firstname,middlename,lastname,birthdate);
                                declarationInfoArrayList.add(declarationInfo);
                            }

                            adapter = new declarationAdapter(this,declarationInfoArrayList);
                            rv_declarations.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        i.putExtra("has_declaration",has_declaration);
        i.putExtra("declarations", declaration);
        setResult(Activity.RESULT_OK, i);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}