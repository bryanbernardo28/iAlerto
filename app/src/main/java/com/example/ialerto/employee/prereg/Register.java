package com.example.ialerto.employee.prereg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Toast;

import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost;
import com.example.ialerto.Globals;
import com.example.ialerto.MyConfig;
import com.example.ialerto.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Register extends Fragment {
    private String m_deviceName = "";
    private static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";
    private final int GENERAL_ACTIVITY_RESULT = 2;
    Reader m_reader;

    String declaration;
    boolean has_declaration = false;


    private static TextInputEditText et_firstname,et_middlename,et_lastname,et_email,et_birthdate,et_address,et_contact_number,et_password,et_confirm_password,et_health_concern,et_declaration;
    private TextInputLayout etl_firstname,etl_middlename,etl_lastname,etl_email,etl_birthdate,etl_address,etl_contact_number,etl_password,etl_confirm_password,etl_health_concern;
    SharedPreferences loginlogout_pref,profileinfo_pref;
    static String birthdate = "",province,city,barangay,detailed_address;
    private final int ADDRESS_INFORMATION_REQUEST_CODE = 1;
    private final int DECLARATION_REQUEST_CODE = 3;
    private CheckBox cb_pwd,cb_senior;

    private int pwd = 0,senior = 0;
    private JSONObject user;

    private Button btn_next;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_register, container, false);

        et_firstname = view.findViewById(R.id.et_firstname_id);
        et_middlename = view.findViewById(R.id.et_middlename_id);
        et_lastname = view.findViewById(R.id.et_lastname_id);
        et_email = view.findViewById(R.id.et_email_id);
        et_birthdate = view.findViewById(R.id.et_birthdate_id);
        et_contact_number = view.findViewById(R.id.et_contact_number_id);
        et_address = view.findViewById(R.id.et_address_id);
        et_password = view.findViewById(R.id.et_password_id);
        et_confirm_password = view.findViewById(R.id.et_confirm_password_id);
        et_health_concern = view.findViewById(R.id.et_health_concern_id);

//        et_firstname.setText("Bryan");
//        et_middlename.setText("Lalangan");
//        et_lastname.setText("Bernardo");
//        et_contact_number.setText("09063363022");
//        et_password.setText("secrettt");
//        et_confirm_password.setText("secrettt");
//        et_email.setText("bryanbernardo9828@gmail.com");

        etl_firstname = view.findViewById(R.id.etl_firstname_id);
        etl_middlename = view.findViewById(R.id.etl_middlename_id);
        etl_lastname = view.findViewById(R.id.etl_lastname_id);
        etl_email = view.findViewById(R.id.etl_email_id);
        etl_birthdate = view.findViewById(R.id.etl_birthdate_id);
        etl_contact_number = view.findViewById(R.id.etl_contact_number_id);
        etl_address = view.findViewById(R.id.etl_address_id);
        etl_password = view.findViewById(R.id.etl_password_id);
        etl_confirm_password = view.findViewById(R.id.etl_confirm_password_id);
        etl_health_concern = view.findViewById(R.id.etl_health_concern_id);

        view.findViewById(R.id.cb_pwd_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                pwd = checked ? 1 : 0;
            }
        });
        view.findViewById(R.id.cb_senior_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                senior = checked ? 1 : 0;
            }
        });


        view.findViewById(R.id.btn_next_id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new register().execute();
            }
        });

        et_declaration = view.findViewById(R.id.et_declaration_id);
        et_declaration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent set_declaration = new Intent(getActivity(),DeclarationActivity.class);
                set_declaration.putExtra("has_declaration",has_declaration);
                set_declaration.putExtra("declarations",declaration);
                startActivityForResult(set_declaration,DECLARATION_REQUEST_CODE);
            }
        });

        et_birthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
            }
        });

        et_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent set_address = new Intent(getActivity(), AddressInformation.class);
                startActivityForResult(set_address,ADDRESS_INFORMATION_REQUEST_CODE);
            }
        });
        return view;
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(0);
            cal.set(year, month, day, 0, 0, 0);
            Date chosenDate = cal.getTime();
            birthdate = String.valueOf(cal.getTimeInMillis());

//            DateFormat df_long = DateFormat.getDateInstance(DateFormat.LONG);
//            String df_long_str = df_long.format(chosenDate);

            SimpleDateFormat format = new SimpleDateFormat("LLLL dd,yyyy");
            String strDate = format.format(chosenDate);

            et_birthdate.setText(strDate);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Globals.ClearLastBitmap();
        if (resultCode != Activity.RESULT_CANCELED){
            m_deviceName = (String) data.getExtras().get("device_name");
            boolean has_device = data.getBooleanExtra("has_device",false);

            switch (requestCode){
                case ADDRESS_INFORMATION_REQUEST_CODE:
                    if (!data.getStringExtra("address").isEmpty() || data.getStringExtra("address") != null || data.getStringExtra("address") != ""){
                        province = data.getStringExtra("province");
                        city = data.getStringExtra("city");
                        barangay = data.getStringExtra("barangay");
                        detailed_address = data.getStringExtra("detailed_address");

                        String address = data.getStringExtra("address");
                        et_address.setText(address);
                    }
                    else{
                        etl_address.setError("Address Error");
                    }
                    break;
                case DECLARATION_REQUEST_CODE:
                    try {
                        has_declaration = data.getBooleanExtra("has_declaration",false);
                        declaration = data.getStringExtra("declarations");
                        if (has_declaration){
                            JSONArray jsonArray = new JSONArray(declaration);
                            ArrayList declaration_names = new ArrayList();
                            for (int i = 0; i < jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String firstname = jsonObject.getString("firstname");
                                String middlename = jsonObject.getString("middlename");
                                String lastname = jsonObject.getString("lastname");
                                String name = firstname +" "+middlename+" "+lastname;
                                declaration_names.add(name);
                            }
                            String names = TextUtils.join(",",declaration_names);
                            et_declaration.setText(names);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case GENERAL_ACTIVITY_RESULT:
                    if (has_device){
                        try {
                            Context applContext = getActivity().getApplicationContext();
                            m_reader = Globals.getInstance().getReader(m_deviceName, applContext);
                            m_deviceName = data.getStringExtra("device_name");
                            PendingIntent mPermissionIntent;
                            mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
                            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                            applContext.registerReceiver(mUsbReceiver, filter);

                            if (DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(applContext, mPermissionIntent, m_deviceName)){
                                GetDevice();
                            }

                        } catch (UareUException e) {
                            e.printStackTrace();
                            displayReaderNotFound();
                        } catch (DPFPDDUsbException e) {
                            e.printStackTrace();
                            displayReaderNotFound();
                        }
                    }
                    else{
                        displayReaderNotFound();
                    }
                    break;
            }
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)){
                synchronized (this){
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
                        if (device != null){
                            GetDevice();
                        }
                    }
                    else{
                        Toast.makeText(getActivity(),"Please allow permission to use the fingerprint scanner",Toast.LENGTH_LONG).show();
                    }
                }
            }

        }
    };


    public void GetDevice(){
        try
        {
            m_reader.Open(Reader.Priority.EXCLUSIVE);
            Reader.Capabilities cap = m_reader.GetCapabilities();
            Log.d("checkDevice","GetDevice: " + cap);
            Intent intent_fingerprint_scanner = new Intent(getActivity(), Fingerprint.class);
            intent_fingerprint_scanner.putExtra("device_name",m_deviceName);
            intent_fingerprint_scanner.putExtra("user", String.valueOf(user));
            startActivity(intent_fingerprint_scanner);
            getActivity().finish();
//            Toast.makeText(Register.this,"GetDevice: " + cap,Toast.LENGTH_LONG).show();
            m_reader.Close();
        }
        catch (UareUException e1)
        {
            displayReaderNotFound();
        }
    }


    public void displayReaderNotFound(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Fingerprint scanner Not Found");
        alertDialogBuilder.setMessage("Plug in a fingerprint scanner and try again.").setCancelable(false).setPositiveButton("Ok",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog,int id) {}
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        if(!getActivity().isFinishing()) {
            alertDialog.show();
        }
    }




    class register extends AsyncTask<String,Void,String>{
        ProgressDialog pd;
        String firstname = et_firstname.getText().toString();
        String middlename = et_middlename.getText().toString();
        String lastname = et_lastname.getText().toString();
        String email = et_email.getText().toString();
        String password = et_password.getText().toString();
        String confirm_password = et_confirm_password.getText().toString();
        String contact_number = et_contact_number.getText().toString();
        String address = et_address.getText().toString();
        String health_concern = et_health_concern.getText().toString();

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("first_name",firstname)
                    .addFormDataPart("middle_name",middlename)
                    .addFormDataPart("last_name",lastname)
                    .addFormDataPart("email",email)
                    .addFormDataPart("contact_number",contact_number)
                    .addFormDataPart("birthdate",birthdate)
                    .addFormDataPart("province",province)
                    .addFormDataPart("city",city)
                    .addFormDataPart("barangay",barangay)
                    .addFormDataPart("detailed_address",detailed_address)
                    .addFormDataPart("health_concern",health_concern)
                    .addFormDataPart("pwd", String.valueOf(pwd))
                    .addFormDataPart("senior", String.valueOf(senior))
                    .addFormDataPart("password",password)
                    .addFormDataPart("password_confirmation",confirm_password)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/register/store/check_first")
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
            pd = new ProgressDialog(getActivity());
            pd.setMessage("Loading...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                etl_firstname.setError(null);
                etl_middlename.setError(null);
                etl_lastname.setError(null);
                etl_email.setError(null);
                etl_password.setError(null);
                etl_address.setError(null);
                etl_birthdate.setError(null);
                etl_contact_number.setError(null);
                JSONObject jsonObject = new JSONObject(s);
                Log.d("check", s);
                boolean response = jsonObject.getBoolean("success");

                if (response){
                    user = jsonObject.getJSONObject("response");
                    user.put("province",province);
                    user.put("city",city);
                    user.put("barangay",barangay);
                    user.put("detailed_address",detailed_address);
                    user.put("declaration",declaration);

                    Log.d("checkUser", String.valueOf(user));

                    Log.d("check_user","Pre-reg User: " + user);

//                    Intent intent_fingerprint_scanner = new Intent(getActivity(), Fingerprint.class);
//                    intent_fingerprint_scanner.putExtra("device_name",m_deviceName);
//                    intent_fingerprint_scanner.putExtra("user", String.valueOf(user));
//                    startActivity(intent_fingerprint_scanner);
//                    getActivity().finish();
                    Intent i = new Intent(getActivity(), VerifyFingerprintScanner.class);
                    i.putExtra("device_name", m_deviceName);
                    startActivityForResult(i, GENERAL_ACTIVITY_RESULT);

                }
                else{
                    JSONObject error = jsonObject.getJSONObject("response");
                    if (error.has("first_name")){
                        JSONArray firstname = error.getJSONArray("first_name");
                        etl_firstname.setError(firstname.get(0).toString());
                    }

                    if (error.has("middle_name")){
                        JSONArray middlename = error.getJSONArray("middle_name");
                        etl_middlename.setError(middlename.get(0).toString());
                    }

                    if (error.has("last_name")){
                        JSONArray last_name = error.getJSONArray("last_name");
                        etl_lastname.setError(last_name.get(0).toString());
                    }

                    if (error.has("email")){
                        JSONArray email = error.getJSONArray("email");
                        etl_email.setError(email.get(0).toString());
                    }

                    if (error.has("contact_number")){
                        JSONArray contact_number = error.getJSONArray("contact_number");
                        etl_contact_number.setError(contact_number.get(0).toString());
                    }

                    if (error.has("birthdate")){
                        JSONArray birthdate = error.getJSONArray("birthdate");
                        etl_birthdate.setError(birthdate.get(0).toString());
                    }

                    if (error.has("address")){
                        JSONArray address = error.getJSONArray("address");
                        etl_address.setError(address.get(0).toString());
                    }

                    if (error.has("password")){
                        JSONArray password = error.getJSONArray("password");
                        etl_password.setError(password.get(0).toString());
                        et_confirm_password.getText().clear();
                    }

                    Toast.makeText(getActivity(),"Please fill out all required fields.",Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.cb_pwd_id:
                pwd = checked ? 1 : 0;
                break;
            case R.id.cb_senior_id:
                senior = checked ? 1 : 0;
                break;
        }
    }
}
