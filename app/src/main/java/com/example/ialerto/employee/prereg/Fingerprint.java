package com.example.ialerto.employee.prereg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Quality;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.jni.DpfjQuality;
import com.example.ialerto.Dashboard;
import com.example.ialerto.Globals;
import com.example.ialerto.MainActivity;
import com.example.ialerto.MyConfig;
import com.example.ialerto.R;
import com.example.ialerto.resident.alert.AlertFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Fingerprint extends AppCompatActivity {
    ImageView iv_leftfinger,iv_rightfinger;
    String device_name = "";
    private Reader m_reader = null;
    private int m_DPI = 0;
    private boolean m_reset = false;
    private Reader.CaptureResult cap_result = null;
    private String m_text_conclusionString;
    private String active_finger = "left";
    private Bitmap fingerprint_bitmap = null,left_fingerprint = null,right_fingerprint = null,selfie_image = null;

    private TextView tv_LeftFingerprintStatus,tv_RightFingerprintStatus;

    Thread scanner_thread;

    static CheckBox cb_tac;

    private volatile boolean stopLeftThread = false,stopRightThread = false,stopThread = false;

    RadioButton rb_left_finger,rb_right_finger;

    private FingerprintThread fingerprintThread;

    private JSONObject user;
    SharedPreferences loginlogout_pref,profileinfo_pref;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    boolean has_image = false;
    ImageView iv_selfie;

    static boolean cb_tac_checked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fingerprint);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        iv_leftfinger = findViewById(R.id.iv_leftfinger_id);
        iv_rightfinger = findViewById(R.id.iv_rightfinger_id);
        iv_selfie = findViewById(R.id.iv_selfie_id);

        tv_LeftFingerprintStatus = findViewById(R.id.tv_leftfingerprintstatus_id);
        tv_RightFingerprintStatus = findViewById(R.id.tv_rightfingerprintstatus_id);
        Bitmap m_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
        iv_leftfinger.setImageBitmap(m_bitmap);
        iv_rightfinger.setImageBitmap(m_bitmap);

        cb_tac = findViewById(R.id.cb_tac_id);
        cb_tac.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TacFragment tacFragment = new TacFragment();
                tacFragment.show(getSupportFragmentManager(), "My Dialog");
            }
        });

        String str_user = getIntent().getStringExtra("user");

        try {
            user = new JSONObject(str_user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        rb_left_finger = findViewById(R.id.rb_leftfinger_id);
        rb_left_finger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rb_left_finger.isChecked() && active_finger.equals("right")){
                    active_finger = "left";
                    fingerprintThread.interrupt();
                    fingerprintThread = new FingerprintThread();
                    fingerprintThread.start();
                }
            }
        });
        rb_right_finger = findViewById(R.id.rb_rightfinger_id);
        rb_right_finger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rb_right_finger.isChecked() && active_finger.equals("left")){
                    rb_left_finger.setEnabled(false);
                    rb_left_finger.setClickable(false);
                    active_finger = "right";
                    fingerprintThread.interrupt();
                    fingerprintThread = new FingerprintThread();
                    fingerprintThread.start();
                }
            }
        });

        device_name = getIntent().getStringExtra("device_name");
//         Image Processing DEFAULT
        Globals.DefaultImageProcessing = Reader.ImageProcessing.IMG_PROC_DEFAULT;

        // initialize dp sdk

        try {
            Context applContext = getApplicationContext();
            m_reader = Globals.getInstance().getReader(device_name, applContext);
            m_reader.Open(Reader.Priority.EXCLUSIVE);
            m_DPI = Globals.GetFirstDPI(m_reader);
        } catch (UareUException e) {
            Log.d("checkDevice", "error during init of reader");
            e.printStackTrace();
            device_name = "";
            return;
        }


        // loop capture on a separate thread to avoid freezing the UI
        fingerprintThread = new FingerprintThread();
        fingerprintThread.start();



//        disableFingerprint();
    }

    public void disableFingerprint(){
        rb_left_finger.setVisibility(View.GONE);
        rb_right_finger.setVisibility(View.GONE);
        iv_rightfinger.setVisibility(View.GONE);
        iv_leftfinger.setVisibility(View.GONE);
        findViewById(R.id.btn_submit_id).setEnabled(true);
        findViewById(R.id.btn_reset_id).setVisibility(View.GONE);
        findViewById(R.id.btn_reconnectdevice_id).setVisibility(View.GONE);
        tv_LeftFingerprintStatus.setVisibility(View.GONE);
        tv_RightFingerprintStatus.setVisibility(View.GONE);
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
            iv_selfie.setImageBitmap(imageBitmap);
            selfie_image = imageBitmap;
            has_image = true;
        }
    }


    class FingerprintThread extends Thread{
        @Override
        public void run() {
            super.run();
            Reader.CaptureResult cap_result = null;
            try {
                m_reset = false;
                while (!m_reset){
                    // capture the image (synchronous)
                    if (true){
                        cap_result = m_reader.Capture(Fid.Format.ANSI_381_2004, Globals.DefaultImageProcessing, m_DPI, -1);
                    }

                    if(cap_result.image != null){
                        // save bitmap image locally
                        fingerprint_bitmap = Globals.GetBitmapFromRaw(cap_result.image.getViews()[0].getImageData(), cap_result.image.getViews()[0].getWidth(), cap_result.image.getViews()[0].getHeight());

                        // calculate nfiq score
                        DpfjQuality quality = new DpfjQuality();
                        int nfiqScore = quality.nfiq_raw(
                                cap_result.image.getViews()[0].getImageData(),	// raw image data
                                cap_result.image.getViews()[0].getWidth(),		// image width
                                cap_result.image.getViews()[0].getHeight(),		// image height
                                m_DPI,											// device DPI
                                cap_result.image.getBpp(),						// image bpp
                                Quality.QualityAlgorithm.QUALITY_NFIQ_NIST		// qual. algo.
                        );

                        // log NFIQ score
                        Log.d("checkDevice", "capture result nfiq score: " + nfiqScore);

                        // update ui string
                        m_text_conclusionString = Globals.QualityToString(cap_result);
                    }
                    else{
                        fingerprint_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
                        // update ui string
                        m_text_conclusionString = Globals.QualityToString(cap_result);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateGUI();
                        }
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                if (!m_reset){
                    Log.d("checkDevice", "error during capture: " + e.toString());
                    device_name = "";
//                        onBackPressed();
                }
            }
        }
    }


    private void updateGUI(){
        if (active_finger == "left" && rb_left_finger.isChecked()){
            left_fingerprint = fingerprint_bitmap;
            iv_leftfinger.setImageBitmap(left_fingerprint);
            tv_LeftFingerprintStatus.setText("Status: " + m_text_conclusionString);
        }
        else if (active_finger == "right" && rb_right_finger.isChecked()){
            right_fingerprint = fingerprint_bitmap;
            iv_rightfinger.setImageBitmap(right_fingerprint);
            tv_RightFingerprintStatus.setText("Status: " + m_text_conclusionString);
            findViewById(R.id.btn_submit_id).setEnabled(true);
        }
    }

    class sendPreReg extends AsyncTask<String,Void,String>{
        ProgressDialog pd;
        @Override
        protected String doInBackground(String... strings) {
            try {
                ByteArrayOutputStream left_output_stream = new ByteArrayOutputStream();
                left_fingerprint.compress(Bitmap.CompressFormat.PNG, 100, left_output_stream);
                byte[] left_byteArray = left_output_stream.toByteArray();

                ByteArrayOutputStream right_output_stream = new ByteArrayOutputStream();
                right_fingerprint.compress(Bitmap.CompressFormat.PNG, 100, right_output_stream);
                byte[] right_byteArray = right_output_stream.toByteArray();

                ByteArrayOutputStream selfie_image_stream = new ByteArrayOutputStream();
                selfie_image.compress(Bitmap.CompressFormat.PNG,100,selfie_image_stream);
                byte[] selfie_byteArray = selfie_image_stream.toByteArray();


                String first_name = user.getString("first_name");
                String middle_name = user.getString("middle_name");
                String last_name = user.getString("last_name");
                String name = first_name + " " + middle_name + " " + last_name;
                String email = user.getString("email");
                String password = user.getString("password");
                String contact_number = user.getString("contact_number");
                String province = user.getString("province");
                String city = user.getString("city");
                String barangay = user.getString("barangay");
                String detailed_address = user.getString("detailed_address");
                String birthdate = user.getString("birthdate");
                String health_concern = user.getString("health_concern");
                String pwd = user.getString("pwd");
                String senior = user.getString("senior");
                String declarations = user.getString("declaration");


                OkHttpClient getstudents = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("name",name)
                        .addFormDataPart("first_name",first_name)
                        .addFormDataPart("middle_name",middle_name)
                        .addFormDataPart("last_name",last_name)
                        .addFormDataPart("email",email)
                        .addFormDataPart("password",password)
                        .addFormDataPart("contact_number",contact_number)
                        .addFormDataPart("province",province)
                        .addFormDataPart("city",city)
                        .addFormDataPart("barangay",barangay)
                        .addFormDataPart("detailed_address",detailed_address)
                        .addFormDataPart("birthdate",birthdate)
                        .addFormDataPart("health_concern",health_concern)
                        .addFormDataPart("pwd",pwd)
                        .addFormDataPart("senior",senior)
                        .addFormDataPart("declarations",declarations)
                        .addFormDataPart("left_index_fingerprint","left_index_fingerprint",RequestBody.create( left_byteArray,MediaType.parse("image/*jpg")))
                        .addFormDataPart("right_index_fingerprint","right_index_fingerprint",RequestBody.create(right_byteArray,MediaType.parse("image/*jpg")))
                        .addFormDataPart("selfie_image","selfie_image",RequestBody.create(selfie_byteArray,MediaType.parse("image/*jpg")))
                        .build();
                Request request = new Request.Builder()
                        .url(MyConfig.base_url+"/register")
                        .post(requestBody)
                        .build();

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
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(Fingerprint.this);
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pd.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");
                Log.d("checkResult", String.valueOf(jsonObject));
                if (response){
                    Log.d("check_user","Pre-reg User: " + user);
                    onBackPressed();
                    Toast.makeText(Fingerprint.this, "Pre-registeration successful", Toast.LENGTH_SHORT).show();

                }
                else{
                    JSONObject error = jsonObject.getJSONObject("response");
                    Toast.makeText(getApplicationContext(),"Some required fields already exists in database.",Toast.LENGTH_LONG).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void buttonClicked(View v){
        switch (v.getId()){
            case R.id.btn_reset_id:
                resetFingerprint();
                break;
            case R.id.btn_submit_id:
                btn_submit();
                break;
            case R.id.btn_reconnectdevice_id:
                break;
            case R.id.btn_selfie_id:
                dispatchTakePictureIntent();
                break;
        }
    }

    public void btn_submit(){
        if (!has_image){
            Toast.makeText(this, "You need to take a selfie.", Toast.LENGTH_SHORT).show();
        }
        else if (!cb_tac_checked){
            Toast.makeText(this, "Please read the terms and condition.", Toast.LENGTH_SHORT).show();
        }
        else{
            new sendPreReg().execute();
        }
    }

    private void resetFingerprint(){
        rb_right_finger.setChecked(false);
        rb_left_finger.setChecked(true);
        rb_left_finger.setEnabled(true);
        rb_left_finger.setClickable(true);
        fingerprint_bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.black);
        iv_leftfinger.setImageBitmap(fingerprint_bitmap);
        iv_rightfinger.setImageBitmap(fingerprint_bitmap);
        tv_LeftFingerprintStatus.setText("Status: ");
        tv_RightFingerprintStatus.setText("Status: ");
        active_finger = "left";
        fingerprintThread.interrupt();
        fingerprintThread = new FingerprintThread();
        fingerprintThread.start();
    }

    public static class TacFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            View inflater = requireActivity().getLayoutInflater().inflate(R.layout.terms_and_condition,null);
            ScrollView sv_tac = inflater.findViewById(R.id.sv_tac_id);
            builder.setView(inflater);
            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cb_tac_checked = true;
                    cb_tac.setEnabled(false);
                }
            });

            AlertDialog tacDialog = builder.create();
            tacDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = tacDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setEnabled(false);
                    sv_tac.getViewTreeObserver()
                            .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                                @Override
                                public void onScrollChanged() {
                                    if (sv_tac.getChildAt(0).getBottom()
                                            <= (sv_tac.getHeight() + sv_tac.getScrollY())) {
                                        b.setEnabled(true);
                                    } else {
                                        //scroll view is not at bottom
                                    }
                                }
                            });

                    tacDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                cb_tac.setChecked(false);
                                cb_tac_checked = false;
                                dialog.dismiss();
                            }
                            return true;
                        }
                    });

                }
            });
            return tacDialog;
        }
    }


    @Override
    public void onBackPressed() {
        Intent i = new Intent(Fingerprint.this, Dashboard.class);
        i.putExtra("from_activity","prereg");
        startActivity(i);
        finish();
    }
}