package com.example.ialerto.resident.alert;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.CountDownTimer;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ebanx.swipebtn.OnActiveListener;
import com.ebanx.swipebtn.SwipeButton;
import com.example.ialerto.MainActivity;
import com.example.ialerto.MyConfig;
import com.example.ialerto.ui.AlertChat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;



import com.example.ialerto.R;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlertFragment extends Fragment implements MapboxMap.OnMapClickListener {

    private boolean mLocationPermissionGranted = false;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 8002;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 8003;
    private FusedLocationProviderClient fusedLocationClient;


    private MapView mapView;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private GeoJsonSource geoJsonSource;
    private ValueAnimator animator;
    private Button btn_submitalert;
    private static Context context;
    private LatLng myLocationLatLng = null;
    private static LatLng currentLatLng;
    private static String myAddress = "N/A";
    private static boolean notDoneOnDialog = false;
    static SharedPreferences profileinfo_pref;
    private static SwipeButton sb_alert;
    private static long START_TIME_IN_MILLIS = 10000;
    private static CountDownTimer mCountDownTimer;
    private static boolean mTimerRunning;
    private static long mTimeLeftInMillis;
    private static long mEndTime;
    public static FragmentActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Mapbox.getInstance(getActivity(), getString(R.string.mapbox_token));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        View view = inflater.inflate(R.layout.fragment_alert, container, false);

        profileinfo_pref = getActivity().getSharedPreferences(MainActivity.PROFILEPREF_NAME,Context.MODE_PRIVATE);
//        SharedPreferences.Editor profileinfo_pref_editor = profileinfo_pref.edit();

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
//        mapView.getMapAsync(this);
        context = getActivity();
        activity = getActivity();
        sb_alert = view.findViewById(R.id.sb_alert_id);


        sb_alert.setOnActiveListener(new OnActiveListener() {
            @Override
            public void onActive() {
//                Toast.makeText(context, "Active!", Toast.LENGTH_SHORT).show();

                if (mLocationPermissionGranted && isGPSEnabled()){
                    ChooseAlertFragment alert = new ChooseAlertFragment();
                    alert.show(getActivity().getSupportFragmentManager(), "My Dialog");
                }
                else{
                    Toast.makeText(context,"Please allow location services and turn on GPS.",Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    private static void startTimer() {
        sb_alert.setEnabled(false);

        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;

        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                updateCountDown();
            }

            @Override
            public void onFinish() {
                mTimerRunning = false;
                mTimeLeftInMillis = START_TIME_IN_MILLIS;
                updateCountDown();
                updateSendButton();
                if (!mTimerRunning) {
                    sb_alert.setEnabled(true);
                    sb_alert.setText("SWIPE TO ALERT");
                }

            }
        }.start();

        mTimerRunning = true;
        updateSendButton();
    }

    private static void updateCountDown() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        Log.d("check", "Time : " + timeLeftFormatted);

        sb_alert.setText(timeLeftFormatted);
    }

    private static void updateSendButton() {
        Log.d("check", "UpdateSendButton:" + mTimerRunning);
        if (mTimerRunning) {
            sb_alert.setEnabled(false);
        } else {
            sb_alert.setEnabled(true);
            sb_alert.setText("SWIPE TO ALERT");
        }
    }


    private void Started() {
        SharedPreferences prefs = getActivity().getSharedPreferences("prefs", MODE_PRIVATE);

        mTimeLeftInMillis = prefs.getLong("millisLeft", START_TIME_IN_MILLIS);
        mTimerRunning = prefs.getBoolean("timerRunning", false);

        updateCountDown();
        updateSendButton();

        if (mTimerRunning) {
            mEndTime = prefs.getLong("endTime", 0);
            mTimeLeftInMillis = mEndTime - System.currentTimeMillis();

            Log.d("check", "Time Left Millis" + mTimeLeftInMillis);

            if (mTimeLeftInMillis < 0) {
                mTimeLeftInMillis = 0;
                mTimerRunning = false;
                mTimeLeftInMillis = START_TIME_IN_MILLIS;
                updateCountDown();
                updateSendButton();
                Log.d("check", "mTimeLeftInMillis < 0 = " + false);

            } else {
                Log.d("check", "mTimeLeftInMillis < 0 = " + true);
                startTimer();
            }
        }
    }



    public void addMarker(LatLng latLng){
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if (!addresses.isEmpty()){
                myAddress = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mapboxMap == null){
            geoJsonSource = new GeoJsonSource("source-id",
                    Feature.fromGeometry(Point.fromLngLat(latLng.getLongitude(),latLng.getLatitude())));
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                    AlertFragment.this.mapboxMap = mapboxMap;
                    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            style.addImage(("marker_icon"), BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default));
                            style.addSource(geoJsonSource);
                            style.addLayer(new SymbolLayer("layer-id", "source-id")
                                    .withProperties(
                                            PropertyFactory.iconImage("marker_icon"),
                                            PropertyFactory.iconIgnorePlacement(true),
                                            PropertyFactory.iconAllowOverlap(true)
                                    ));
                            CameraPosition position = new CameraPosition.Builder()
                                    .target(latLng)
                                    .zoom(16)
                                    .build();
                            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position),7000);
                            mapboxMap.addOnMapClickListener(AlertFragment.this);
                        }
                    });
                }
            });
        }
        else{
            if (animator != null && animator.isStarted()){
                currentLatLng = (LatLng) animator.getAnimatedValue();
            }

            animator = ObjectAnimator
                    .ofObject(latLngEvaluator,currentLatLng,latLng)
                    .setDuration(500);
            animator.addUpdateListener(animatorUpdateListener);
            animator.start();
            currentLatLng = latLng;
            CameraPosition position = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(16)
                    .build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position),500);
        }

        Log.d("checkMap",myAddress);
        currentLatLng = latLng;
    }

    private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();
            geoJsonSource.setGeoJson(Point.fromLngLat(animatedPosition.getLongitude(), animatedPosition.getLatitude()));
        }
    };

    private static final TypeEvaluator<LatLng> latLngEvaluator = new TypeEvaluator<LatLng>() {
        private final LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    };

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
//        requestNewLocationData();
        addMarker(point);
        return true;
    }

    public static class ChooseAlertFragment extends DialogFragment{
        String selectedDisaster = null;
        boolean is_others = false;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View inflater = requireActivity().getLayoutInflater().inflate(R.layout.alert_types,null);
            RadioGroup rg_disaster = inflater.findViewById(R.id.rg_disaster_id);
            TextInputLayout etl_others = inflater.findViewById(R.id.etl_others_id);
            TextInputEditText et_others = inflater.findViewById(R.id.et_others_id);

            // Set the dialog title
            builder.setTitle(R.string.dialog_title);
            builder.setView(inflater)
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (selectedDisaster == null){
                                Toast.makeText(context,"Please choose disaster type.",Toast.LENGTH_LONG).show();
                            }
                            else if (!is_others && selectedDisaster == "Accident"){
                                ChooseAccidentType chooseAccidentType = new ChooseAccidentType();
                                chooseAccidentType.show(getActivity().getSupportFragmentManager(), "Accident Dialog");
                            }
                            else{
                                if (is_others){
                                    selectedDisaster = et_others.getText().toString();
                                }
                                dialog.dismiss();
                                new sendAlert(selectedDisaster).execute();

                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setEnabled(false);

                    et_others.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (hasFocus){
                                b.setEnabled(true);
                                rg_disaster.clearCheck();
                                is_others = true;
                                selectedDisaster = "Others";
                            }
                            else{
                                is_others = false;
                            }
                        }
                    });

                    rg_disaster.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            b.setEnabled(true);
                            if (is_others){
                                hideSoftKeyboard(getActivity(),inflater);
                                et_others.clearFocus();
                            }
                            is_others = false;
                            switch (checkedId){
                                case R.id.rb_fire_id:
                                    selectedDisaster = "Fire";
                                    break;
                                case R.id.rb_flood_id:
                                    selectedDisaster = "Flood";
                                    break;
                                case R.id.rb_crime_id:
                                    selectedDisaster = "Crime";
                                    break;
                                case R.id.rb_accident_id:
                                    selectedDisaster = "Accident";
                                    break;
                            }
                        }
                    });
                }
            });

            return alertDialog;
        }
    }

    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    public static class ChooseAccidentType extends DialogFragment{
        String selectedAccident = null;
        boolean is_others = false;
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            View inflater = requireActivity().getLayoutInflater().inflate(R.layout.accident_types,null);
            RadioGroup rg_accident = inflater.findViewById(R.id.rg_accident_id);
            TextInputLayout etl_others = inflater.findViewById(R.id.etl_others_id);
            TextInputEditText et_others = inflater.findViewById(R.id.et_others_id);
            builder.setTitle(R.string.accident_dialog_title);
            builder.setView(inflater);
            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (selectedAccident == null){
                        Toast.makeText(context,"Please choose disaster type.",Toast.LENGTH_LONG).show();
                    }
                    else{
                        if (is_others){
                            selectedAccident = et_others.getText().toString();
                        }
//                        Toast.makeText(context, "Accident: " + selectedAccident, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        new sendAlert(selectedAccident).execute();
                    }
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            AlertDialog accidentDialog = builder.create();
            accidentDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button b = accidentDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    b.setEnabled(false);

                    et_others.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (hasFocus){
                                b.setEnabled(true);
                                rg_accident.clearCheck();
                                is_others = true;
                                selectedAccident = "Others";
                            }
                            else{
                                is_others = false;
                            }
                        }
                    });

                    rg_accident.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup group, int checkedId) {
                            b.setEnabled(true);
                            if (is_others){
                                hideSoftKeyboard(getActivity(),inflater);
                                et_others.clearFocus();
                            }
                            is_others = false;
                            switch (checkedId){
                                case R.id.rb_caraccident_id:
                                    selectedAccident = "Car Accident";
                                    break;
                                case R.id.rb_roadaccident_id:
                                    selectedAccident = "Road Accident";
                                    break;
                                case R.id.rb_fatalaccident_id:
                                    selectedAccident = "Fatal Accident";
                                    break;
                            }
                        }
                    });
                }
            });

            return accidentDialog;
        }
    }


    public static class SendDisasterPicture extends DialogFragment{
        String alert_id;

        public SendDisasterPicture(String alert_id) {
            this.alert_id = alert_id;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Do you want to send picture in private message?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent chat = new Intent(context, AlertChat.class);
                            chat.putExtra("alert_id",alert_id);
                            chat.putExtra("from","alertsview");
                            context.startActivity(chat);
                            getActivity().finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            return builder.create();
        }
    }

    static class sendAlert extends AsyncTask<String,Void,String> {
        ProgressDialog pd;
        String id = profileinfo_pref.getString("id","");
        String selectedDisaster;

        public sendAlert(String selectedDisaster) {
            this.selectedDisaster = selectedDisaster;
        }

        @Override
        protected String doInBackground(String... strings) {
            OkHttpClient getstudents = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("user_id",id)
                    .addFormDataPart("latitude", String.valueOf(currentLatLng.getLatitude()))
                    .addFormDataPart("longitude", String.valueOf(currentLatLng.getLongitude()))
                    .addFormDataPart("address",myAddress)
                    .addFormDataPart("type",selectedDisaster)
                    .build();
            Request request = new Request.Builder()
                    .url(MyConfig.base_url+"/alert")
                    .post(requestBody)
                    .build();
            try {
                Response response = getstudents.newCall(request).execute();
                Log.d("check", String.valueOf(response.code()));
//                Log.d("check", response.body().string());
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
            pd = new ProgressDialog(context);
            pd.setMessage("Sending Alert...");
            pd.setIndeterminate(false);
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONObject jsonObject = new JSONObject(s);
                boolean response = jsonObject.getBoolean("success");
                if (response){
                    JSONObject alert = jsonObject.getJSONObject("response");
                    Log.d("checkResult","Response: " + alert);
                    Toast.makeText(context,"Alert Successfully Sent.",Toast.LENGTH_LONG).show();
                    startTimer();
                    String type = alert.getString("type").toLowerCase();
//                    if (type.equals("others")){
//                        Log.d("checkResult","OTHERS");
//                    }
                    String alert_id = alert.getString("alert_id");
                    SendDisasterPicture sendDisasterPicture = new SendDisasterPicture(alert_id);
                    sendDisasterPicture.show(activity.getSupportFragmentManager(), "Send Picture Dialog");

                }
                else{
                    String error = jsonObject.getString("response");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            pd.dismiss();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        Log.d("checkMap","On Start");
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mLocationPermissionGranted){
            mLocationPermissionGranted = true;
            if (isGPSEnabled()){
                getMyLocation();
                mapView.onResume();
                Log.d("checkMap","On Resume Mapview");
            }
        }
        else{
            Log.d("checkMap","On Resume Else");
            if (!notDoneOnDialog){
                Log.d("checkMap","On Resume Dialog");
                getLocationPermission();
            }
        }

        Started();
    }

    private void getMyLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                requestNewLocationData();
            }
            else{
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            notDoneOnDialog = false;
            mLocationPermissionGranted = true;
            if (isGPSEnabled()){
                getMyLocation();
            }

        } else {
            notDoneOnDialog = true;
            requestPermissions(
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getLocationPermission();
                }
//                else if (Build.VERSION.SDK_INT >= 23 && !ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[0])) {
//                    PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog();
//                    permissionDeniedDialog.show(getActivity().getSupportFragmentManager(), "Permission Denied Forever");
//                    // User selected the Never Ask Again Option
//                }
                else{
                    Log.d("checkPermission","Denied");
                    PermissionDeniedDialog permissionDeniedDialog = new PermissionDeniedDialog();
                    permissionDeniedDialog.setCancelable(false);
                    permissionDeniedDialog.show(getActivity().getSupportFragmentManager(), "Permission Denied Forever");
                }
                break;
        }
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return super.shouldShowRequestPermissionRationale(permission);
    }

    public static class ShowEducationalMessage extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();

            builder.setMessage("Permission Denied")
                    .setCancelable(false)
                    .setView(inflater.inflate(R.layout.permission_denied_message,null))
                    .setPositiveButton("RE-TRY", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            requestPermissions(
                                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public static class PermissionDeniedDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            builder.setMessage("Permission Denied");
            builder.setCancelable(false);
            builder.setView(inflater.inflate(R.layout.permission_denied_message,null));
            builder.setPositiveButton("GO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    notDoneOnDialog = false;
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setInterval(5000);
//        mLocationRequest.setFastestInterval(2500);
        mLocationRequest.setNumUpdates(1);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.getMainLooper()
        );
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null){
                Location mLastLocation = locationResult.getLastLocation();
                double latitude,longitude;

                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
                myLocationLatLng = new LatLng(latitude,longitude);
                addMarker(myLocationLatLng);
            }
        }
    };



    private boolean isGPSEnabled(){
        final LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                        }
                    });
            final AlertDialog alert = builder.create();
            if (!alert.isShowing()){
                alert.show();
            }
            return false;
        }
        else{
            return true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        SharedPreferences prefs = getActivity().getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        SharedPreferences prefs = getActivity().getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong("millisLeft", mTimeLeftInMillis);
        editor.putBoolean("timerRunning", mTimerRunning);
        editor.putLong("endTime", mEndTime);

        editor.apply();

        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null){
            mapView.onDestroy();
            fusedLocationClient.removeLocationUpdates(mLocationCallback);
            notDoneOnDialog = false;
        }
    }

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        mapView.onSaveInstanceState(outState);
//    }

}
