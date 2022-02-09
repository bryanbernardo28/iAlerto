package com.example.ialerto.ui.alerts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.example.ialerto.Dashboard;
import com.example.ialerto.R;
import com.example.ialerto.resident.alert.AlertFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.optimization.v1.MapboxOptimization;
import com.mapbox.api.optimization.v1.models.OptimizationResponse;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.light.Position;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class AlertRoute extends AppCompatActivity {
    private boolean mLocationPermissionGranted = false;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 8002;
    private static final int PERMISSIONS_REQUEST_ENABLE_GPS = 8003;
    private FusedLocationProviderClient fusedLocationClient;
    private static boolean notDoneOnDialog = false;
    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";


    private MapView mapView;
    private MapboxMap mapboxMap;
    private GeoJsonSource geoJsonSource;
    private ValueAnimator animator;

    private double latitude,longitude;

    private List<Feature> symbolLayerIconFeatureList;

    private Point origin,destination;
    DirectionsRoute currentRoute;
    private MapboxDirections client;
    List<Point> locations;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Mapbox.getInstance(this, getString(R.string.mapbox_token));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_route);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mapView = findViewById(R.id.mapView);
        latitude = getIntent().getDoubleExtra("latitude",0);
        longitude = getIntent().getDoubleExtra("longitude",0);


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
                    permissionDeniedDialog.show(getSupportFragmentManager(), "Permission Denied Forever");
                }
                break;
        }
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return super.shouldShowRequestPermissionRationale(permission);
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
                LatLng myLocationLatLng = new LatLng(latitude,longitude);
                addMarker(myLocationLatLng);
            }
        }
    };

    public void addMarker(LatLng latLng){
        if (mapboxMap == null){
            double my_lat,my_long;

            my_lat = latLng.getLatitude();
            my_long = latLng.getLongitude();
            symbolLayerIconFeatureList = new ArrayList<>();
            locations = new ArrayList<>();
            symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude())));
            locations.add(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
            symbolLayerIconFeatureList.add(Feature.fromGeometry(Point.fromLngLat(longitude, latitude)));
            locations.add(Point.fromLngLat(longitude, latitude));


            Log.d("check","My coordinates: " + my_lat + ","+my_long + " - Resident Coordinates: " + latitude +","+longitude);
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {
                    AlertRoute.this.mapboxMap = mapboxMap;

                    mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {
                            style.addImage(("marker_icon"), BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default));
                            style.addSource(new GeoJsonSource(ROUTE_SOURCE_ID,
                                    FeatureCollection.fromFeatures(symbolLayerIconFeatureList)));
                            LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);

                            // Add the LineLayer to the map. This layer will display the directions route.
                            routeLayer.setProperties(
                                    lineCap(Property.LINE_CAP_ROUND),
                                    lineJoin(Property.LINE_JOIN_ROUND),
                                    lineWidth(5f),
                                    lineColor(Color.parseColor("#009688"))
                            );

                            style.addLayer(routeLayer);


                            style.addSource(new GeoJsonSource("marker-source-id",
                                    FeatureCollection.fromFeatures(symbolLayerIconFeatureList)));
                            // Add the red marker icon image to the map
                            style.addImage(("marker_icon"), BitmapFactory.decodeResource(getResources(), R.drawable.mapbox_marker_icon_default));

                            // Add the red marker icon SymbolLayer to the map
                            style.addLayer(new SymbolLayer("marker-layer-id", "marker-source-id")
                                    .withProperties(
                                            PropertyFactory.iconImage("marker_icon"),
                                            PropertyFactory.iconIgnorePlacement(true),
                                            PropertyFactory.iconAllowOverlap(true),
                                            PropertyFactory.iconOffset(new Float[] {0f, -9f})
                                    ));

                            LatLng residentLatLng = new LatLng(latitude,longitude);
                            CameraPosition position = new CameraPosition.Builder()
                                    .target(residentLatLng)
                                    .zoom(16)
                                    .build();
                            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position),7000);


                            origin = Point.fromLngLat(my_lat, my_long);
                            destination = Point.fromLngLat(latitude, longitude);
                            getRoute(locations);


                        }
                    });
                }
            });
        }
    }

    private void getRoute(List<Point> destinations) {
        MapboxOptimization optimizedClient = MapboxOptimization.builder()
                .coordinates(destinations)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(Mapbox.getAccessToken())
                .source("first")
                .destination("last")
                .steps(true)
                .roundTrip(false)
                .build();

        optimizedClient.enqueueCall(new Callback<OptimizationResponse>() {
            @Override
            public void onResponse(Call<OptimizationResponse> call, Response<OptimizationResponse> response) {
                Log.d("check", String.valueOf(response));
                if (!response.isSuccessful()) {
                    Log.d("check", "Optimization call not successful");
                    return;
                } else if (response.body() == null) {
                    Log.d("check", "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().trips() == null) {
                    Log.d("check", "No trips found.");
                    return;
                } else {
                    if (response.body().trips().isEmpty()) {
                        Log.d("check", "No routes found");
                        return;
                    }
                }

//                currentRoute = response.body().trips().get(0);
                currentRoute = response.body().trips().get(0);
                if (mapboxMap != null) {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {

//                            // Retrieve and update the source designated for showing the directions route
                            GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);
//
//                            // Create a LineString with the directions route's geometry and
//                            // reset the GeoJSON source for the route LineLayer source
                            if (source != null) {
                                source.setGeoJson(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6));
                            }
//                            GeoJsonSource optimizedLineSource = style.getSourceAs(ROUTE_SOURCE_ID);
//                            if (optimizedLineSource != null) {
//                                optimizedLineSource.setGeoJson(FeatureCollection.fromFeature(Feature.fromGeometry(
//                                        LineString.fromPolyline(currentRoute.geometry(), PRECISION_6))));
//                                Log.d("check","OptimizedLineSource");
//                            }
                        }
                    });
                }


            }

            @Override
            public void onFailure(Call<OptimizationResponse> call, Throwable t) {
                Log.d("check","Error: " + t.getMessage());
//                Toast.makeText(getApplicationContext(), "Error: " + throwable.getMessage(),
//                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getLocationPermission() {
        notDoneOnDialog = true;
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            mLocationPermissionGranted = true;
            if (isGPSEnabled()){
                getMyLocation();
            }

        } else {
            requestPermissions(
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        mLocationRequest.setInterval(5000);
//        mLocationRequest.setFastestInterval(2500);
        mLocationRequest.setNumUpdates(1);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.getMainLooper()
        );
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

    private boolean isGPSEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
    protected void onResume() {
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
    }

    public void goback(){
        Intent goback = new Intent(this, Dashboard.class);
        goback.putExtra("from_activity","alert_route");
        startActivity(goback);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        goback();
        return true;
    }

    @Override
    public void onBackPressed() {
        goback();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


}