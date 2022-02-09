package com.example.ialerto.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ialerto.ConversationInfo;
import com.example.ialerto.Dashboard;
import com.example.ialerto.MainActivity;
import com.example.ialerto.MyConfig;
import com.example.ialerto.R;
import com.squareup.picasso.Picasso;

import net.mrbin99.laravelechoandroid.Echo;
import net.mrbin99.laravelechoandroid.EchoCallback;
import net.mrbin99.laravelechoandroid.EchoOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AlertChat extends AppCompatActivity {

    String alert_id,user_id;
    String message,role,from;
    RecyclerView rv_conversation;
    ConversationAdapter conversationAdapter;
    ArrayList<ConversationInfo> conversationInfoArrayList;
    EditText et_message;
    int has_image = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap disaster_image = null;
    ImageView iv_image;
    Echo echo;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        echo.disconnect();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        et_message = findViewById(R.id.et_message_id);
        iv_image = findViewById(R.id.iv_image_id);


        SharedPreferences myprofile = getSharedPreferences(MainActivity.PROFILEPREF_NAME, Context.MODE_PRIVATE);
        user_id = myprofile.getString("id","");
        alert_id = getIntent().getStringExtra("alert_id");
        role = myprofile.getString("role","");
        from = getIntent().getStringExtra("from");


        rv_conversation = findViewById(R.id.rv_conversation_id);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rv_conversation.setLayoutManager(layoutManager);
        rv_conversation.setHasFixedSize(true);
//        rv_conversation.setAdapter(conversationAdapter);

        new getConversations().execute();

        initSocket();
//        Toast.makeText(this, "AlertID: " + alert_id, Toast.LENGTH_SHORT).show();
    }

    public void initSocket(){
        // Setup options
        EchoOptions options = new EchoOptions();
        options.headers.put("Accept", "Application/json");
        options.headers.put("Content-Type", "Application/json");

        // Setup host of your Laravel Echo Server
        options.host = MyConfig.socket_url;
        // Create the client
        echo = new Echo(options);
        echo.connect(new EchoCallback() {
            @Override
            public void call(Object... args) {
                Log.d("checkSocket","Connect Success: ");
            }
        }, new EchoCallback() {
            @Override
            public void call(Object... args) {
                Log.d("checkSocket","Connect Failed: " + args[0].toString());
            }
        });

        echo.channel("chatAlertChannel"+alert_id).listen(".chatAlertEvent", new EchoCallback() {
            ConversationInfo conversationInfo;
            @Override
            public void call(Object... args) {

                boolean isnull = conversationAdapter==null;
                try {
                    JSONObject jsonObjectChat = (JSONObject)args[1];
                    Log.d("checkSocket","data: " + jsonObjectChat.toString());
//                    JSONObject jsonObjectChat = new JSONObject(event.getData());

                    jsonObjectChat = jsonObjectChat.getJSONObject("message");
                    String id = jsonObjectChat.getString("id");
                    String user_id = jsonObjectChat.getString("user_id");
                    String alert_id = jsonObjectChat.getString("alert_id");
                    String name = jsonObjectChat.getString("name");
                    String role = jsonObjectChat.getString("role");
                    String message = jsonObjectChat.getString("message");
                    String date = jsonObjectChat.getString("date");
                    String image = jsonObjectChat.getString("image");
                    int has_image = jsonObjectChat.getInt("has_image");


                    Log.d("checkSocket","Message: " + message + " , "+image+", ID: " + id);
                    conversationInfo = new ConversationInfo(id,user_id,alert_id,name,role,message,image,date,has_image);
//                    conversationInfoArrayList.add(conversationInfo);
                    if (isnull){
                        conversationInfoArrayList = new ArrayList<>();
                        conversationInfoArrayList.add(conversationInfo);
                        conversationAdapter = new ConversationAdapter(conversationInfoArrayList,getApplicationContext());
                        rv_conversation.setAdapter(conversationAdapter);
                    }
                    else{
                        Log.d("checkSocket","Called Else");
                        conversationAdapter.addMessage(conversationInfo);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                rv_conversation.scrollToPosition(rv_conversation.getAdapter().getItemCount()- 1);
//                            }
//                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_send_image:
                dispatchTakePictureIntent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            iv_image.setVisibility(View.VISIBLE);
            iv_image.setImageBitmap(imageBitmap);
            disaster_image = imageBitmap;
            has_image = 1;
        }
    }

    private class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private ArrayList<ConversationInfo> conversationInfoArrayList;
        private LayoutInflater layoutInflater;

        public ConversationAdapter(ArrayList<ConversationInfo> conversationInfoArrayList, Context context) {
            this.conversationInfoArrayList = conversationInfoArrayList;
            this.layoutInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            int height = parent.getMeasuredHeight() / 4;

            switch (viewType){
                case 0:
                    view = layoutInflater.from(parent.getContext()).inflate(R.layout.row_receiver,parent,false);
                    view.setMinimumHeight(height);
                    ViewHolderReceiver vh0 = new ViewHolderReceiver(view);
                    return vh0;
                case 1:
                    view = layoutInflater.from(parent.getContext()).inflate(R.layout.row_sender,parent,false);
                    view.setMinimumHeight(height);
                    ViewHolderSender vh1 = new ViewHolderSender(view);
                    return vh1;
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            String message = conversationInfoArrayList.get(position).getMessage();
            boolean has_image_row = conversationInfoArrayList.get(position).getHas_image() == 1 ? true : false;
            String image_name = conversationInfoArrayList.get(position).getImage();
            String image_uri = MyConfig.image_base_url+image_name;
            Log.d("check","image name: " + image_name);
            switch (holder.getItemViewType()){
                case 0:
                    ViewHolderReceiver receiver = (ViewHolderReceiver) holder;
                    if (message != "null"){
                        receiver.tv_receiver.setText(message);
                    }
                    else{
                        receiver.tv_receiver.setVisibility(View.GONE);
                        if (has_image_row){
                            receiver.iv_imagereceiver.setVisibility(View.VISIBLE);
                            Picasso.get().load(image_uri).into(receiver.iv_imagereceiver);
                        }
                    }
                    break;
                case 1:
                    ViewHolderSender sender = (ViewHolderSender) holder;
                    if (message != "null"){
                        sender.tv_sender.setText(message);
                    }
                    else{
                        sender.tv_sender.setVisibility(View.GONE);
                    }
                    if (has_image_row){
                        sender.iv_imagesender.setVisibility(View.VISIBLE);
                        Picasso.get().load(image_uri).into(sender.iv_imagesender);
                    }

                    break;
            }
        }


        @Override
        public int getItemCount() {
            return conversationInfoArrayList.size();
        }

        public class ViewHolderReceiver extends RecyclerView.ViewHolder {
            TextView tv_receiver;
            ImageView iv_imagereceiver;
            public ViewHolderReceiver(@NonNull View v) {
                super(v);
                tv_receiver = v.findViewById(R.id.tv_receiver_id);
                iv_imagereceiver = v.findViewById(R.id.iv_imagereceiver_id);
            }
        }


        public class ViewHolderSender extends RecyclerView.ViewHolder{
            TextView tv_sender;
            ImageView iv_imagesender;
            public ViewHolderSender(@NonNull View v) {
                super(v);
                tv_sender = v.findViewById(R.id.tv_sender_id);
                iv_imagesender = v.findViewById(R.id.iv_imagesender_id);
            }
        }

        @Override
        public int getItemViewType(int position) {
            String from = conversationInfoArrayList.get(position).getRole().toString();
            int int_from = from.equals(role) ? 1 : 0 ;
            return int_from;
        }

        public void addMessage(ConversationInfo conversationInfo){
            boolean isnull = conversationAdapter==null;
            int index = isnull ? 0 : conversationAdapter.getItemCount();
            conversationInfoArrayList.add(index,conversationInfo);
            notifyItemInserted(index);
        }
    }


    class getConversations extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            String segment = alert_id;
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/alert/chat/conversations/"+segment)
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
                JSONArray jsonArray = new JSONArray(s);
                setupConversationRecyclerview(jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setupConversationRecyclerview(JSONArray jsonArrayConversation){
        conversationInfoArrayList = new ArrayList<>();
        ConversationInfo conversationInfo;
        try {
            for (int i = 0; i < jsonArrayConversation.length(); i++){
                JSONObject jsonObjectConversation = jsonArrayConversation.getJSONObject(i);
                String id = jsonObjectConversation.getString("id");
                String user_id = jsonObjectConversation.getString("user_id");
                String alert_id = jsonObjectConversation.getString("alert_id");
                String name = jsonObjectConversation.getString("name");
                String role = jsonObjectConversation.getString("role");
                String message = jsonObjectConversation.getString("message");
                String date = jsonObjectConversation.getString("date");
                int has_image = jsonObjectConversation.getInt("has_image");
                String image = jsonObjectConversation.getString("image");
                conversationInfo = new ConversationInfo(id,user_id,alert_id,name,role,message,image,date,has_image);
                conversationInfoArrayList.add(conversationInfo);
//                int int_from = role.equals("resident") ? 1 : 0 ;
                Log.d("checkResult","Role: " + role);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        conversationAdapter = new ConversationAdapter(conversationInfoArrayList,this);
        rv_conversation.setAdapter(conversationAdapter);
//        rv_conversation.scrollToPosition(conversationAdapter.getItemCount() - 1);
    }


    class sendChat extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... strings) {

            ByteArrayOutputStream disaster_image_stream = new ByteArrayOutputStream();
            if (has_image == 1){
                disaster_image.compress(Bitmap.CompressFormat.PNG,100,disaster_image_stream);
            }
            byte[] disaster_image_byteArray = disaster_image_stream.toByteArray();


            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("user_id",user_id)
                    .addFormDataPart("alert_id",alert_id)
                    .addFormDataPart("message",message)
                    .addFormDataPart("has_image", String.valueOf(has_image))
                    .addFormDataPart("disaster_image","disaster_image",RequestBody.create(disaster_image_byteArray, MediaType.parse("image/*jpg")))
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/alert/chat/chat")
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
            Log.d("checkResult", s);
            disaster_image = null;
            has_image = 0;
        }
    }

    public void sendChat(View v){
        if (!et_message.getText().toString().trim().isEmpty() && has_image == 0){
            message = et_message.getText().toString();
            et_message.getText().clear();
            new sendChat().execute();
        }
        else if (et_message.getText().toString().trim().isEmpty() && has_image == 1){
            message = "";
            iv_image.setVisibility(View.GONE);
            iv_image.setImageBitmap(null);
            new sendChat().execute();
        }
        else if (!et_message.getText().toString().trim().isEmpty() && has_image == 1){
            message = et_message.getText().toString();
            et_message.getText().clear();
            iv_image.setVisibility(View.GONE);
            iv_image.setImageBitmap(null);
            new sendChat().execute();
        }
        else if (et_message.getText().toString().trim().isEmpty() && has_image == 0){
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity",from);
        startActivity(goback);
        finish();
    }
}