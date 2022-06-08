package com.example.lab1_devanshi_c0852304_android;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnPolylineClickListener, GoogleMap.OnPolygonClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {
    MapView mapView;
    GoogleMap googleMap;

    EditText etLocation;
    Button btnAddLocation;
    LocationManager locationManager;
    Geocoder geocoder;

    // Marker Labels and current markers
    ArrayList<String> MARKER_LABELS = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
    final int POLYGON_SIDES = 4;
    final int POINT_DELETE_DISTANCE_THRESHOLD = 50;




    ArrayList<Marker> currentMarkers = new ArrayList<Marker>();
    HashMap<String, Marker> annotationMarkersPolyLine = new HashMap<>();
    HashMap<String, Marker> annotationMarkersPolygon = new HashMap<>();
    ArrayList<Polyline> currentPolyLines = new ArrayList<Polyline>();


    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mapView=findViewById(R.id.mapView);
        etLocation=findViewById(R.id.etLocation);
        btnAddLocation=findViewById(R.id.btnAdd);
        Bundle mapbundle = null;
        if (savedInstanceState != null)
        {
            mapbundle=savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        mapView.onCreate(mapbundle);
        mapView.getMapAsync(this);
        mapView.setClickable(true);
        locationPermissionRequest();
        btnAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentTextLocation = etLocation.getText().toString();
                Log.d("BUTTON_CLICK", "User Added Location: " + currentTextLocation);
                LatLng latLng = getLocationFromAddress(currentTextLocation);
                Log.d("BUTTON_CLICK", "User Added Location Latlang: " + latLng);
                // check for north america
                if(latLng != null){
                    addMarker(latLng);
                }else{
                    Toast.makeText(MainActivity.this,"Location Not found",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void getLocationUpdates(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISSION", "Permission Not Given");


        }else{
            Log.d("PERMISSION", "Location Permission Granted");
//            locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

        }
    }

    public LatLng getLocationFromAddress(String strAddress) {

        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.size() == 0) {
                return null;
            }
            Log.d("GEOCODE", "Got the location:" + address);
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng( (location.getLatitude()),
                    (location.getLongitude()));

            return p1;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void locationPermissionRequest(){
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                                Toast.makeText(this,"Precise location access granted",Toast.LENGTH_SHORT).show();
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                                Toast.makeText(this,"Only approximate location access granted",Toast.LENGTH_SHORT).show();
                            } else {
                                // No location access granted.
                                Toast.makeText(this,"No location access granted",Toast.LENGTH_SHORT).show();
                            }
                        }
                );
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {
        Log.d("MAP", "Polygon click detected");
        if(annotationMarkersPolygon.get(polygon.getId()) != null){
            annotationMarkersPolygon.get(polygon.getId()).remove();
            annotationMarkersPolygon.remove(polygon.getId());
            return;
        }
        float totalDistance = 0;
        // find total distance  A - D
        for (Polyline polyline: currentPolyLines) {
            LatLng polylineStart = polyline.getPoints().get(0);
            LatLng polylineEnd = polyline.getPoints().get(1);
            float[] results = new float[1];
            Location.distanceBetween(polylineStart.latitude, polylineStart.longitude,
                    polylineEnd.latitude, polylineEnd.longitude, results);
            totalDistance += results[0];
        }

        Marker centerOneMarker = googleMap.addMarker(new MarkerOptions()
                .position(getPolygonCenterPoint(polygon.getPoints()))
                .icon(makeBitmaptoShowDetails(String.valueOf(totalDistance))));
        annotationMarkersPolygon.put(polygon.getId(), centerOneMarker);
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {
        Log.d("MAP", "Polyline click detected: " + polyline.getId());
        if(annotationMarkersPolyLine.get(polyline.getId()) != null){
            annotationMarkersPolyLine.get(polyline.getId()).remove();
            annotationMarkersPolyLine.remove(polyline.getId());
            return;
        }
        List<LatLng> coordiantesPolyLine = polyline.getPoints();
        LatLng polylineStart = coordiantesPolyLine.get(0);
        LatLng polylineEnd = coordiantesPolyLine.get(1);
        float[] results = new float[1];
        Location.distanceBetween(polylineStart.latitude, polylineStart.longitude,
                polylineEnd.latitude, polylineEnd.longitude, results);

        LatLng midPoint = getPolylineCentroid(polyline);
        Marker centerOneMarker = googleMap.addMarker(new MarkerOptions()
                .position(midPoint)
                .icon(makeBitmaptoShowDetails(String.valueOf(results[0]))));
        annotationMarkersPolyLine.put(polyline.getId(), centerOneMarker);



    }

    private LatLng getPolygonCenterPoint(List<LatLng> polygonPointsList){
        LatLng centerLatLng = null;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0 ; i < polygonPointsList.size() ; i++)
        {
            builder.include(polygonPointsList.get(i));
        }
        LatLngBounds bounds = builder.build();
        centerLatLng =  bounds.getCenter();

        return centerLatLng;
    }

    public LatLng getPolylineCentroid(Polyline p) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(int i = 0; i < p.getPoints().size(); i++){
            builder.include(p.getPoints().get(i));
        }
        LatLngBounds bounds = builder.build();

        return bounds.getCenter();
    }

    private BitmapDescriptor makeBitmaptoShowDetails(String label){
        LinearLayout distanceMarkerLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.marker_details, null);

        distanceMarkerLayout.setDrawingCacheEnabled(true);
        distanceMarkerLayout.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        distanceMarkerLayout.layout(0, 0, distanceMarkerLayout.getMeasuredWidth(), distanceMarkerLayout.getMeasuredHeight());
        distanceMarkerLayout.buildDrawingCache(true);

        TextView positionDistance = distanceMarkerLayout.findViewById(R.id.tvmarkerText);

        positionDistance.setText(label);

        Bitmap flagBitmap = Bitmap.createBitmap(distanceMarkerLayout.getDrawingCache());
        distanceMarkerLayout.setDrawingCacheEnabled(false);
        BitmapDescriptor flagBitmapDescriptor = BitmapDescriptorFactory.fromBitmap(flagBitmap);
        return  flagBitmapDescriptor;

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Adding map controls
        this.googleMap = googleMap;
        googleMap.setMinZoomPreference(12);
        googleMap.setIndoorEnabled(true);
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setIndoorLevelPickerEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setMapToolbarEnabled(true);
        uiSettings.setCompassEnabled(true);
        uiSettings.setZoomControlsEnabled(true);

        LatLng ny = new LatLng(43.6532, -79.3832);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(ny));



        googleMap.setOnMapLongClickListener(this);
        googleMap.setOnMapClickListener(this);
        googleMap.setOnPolylineClickListener(this);
        googleMap.setOnPolygonClickListener(this);
        googleMap.setOnMarkerClickListener(this);
    }

    private String findNextMarker(){
        if(MARKER_LABELS.size() > 0){
            String currentMarker =  MARKER_LABELS.get(0);
            MARKER_LABELS.remove(0);
            return currentMarker;
        }else{
            return "";
        }


    }


    private void clearallMarkers(){
        MARKER_LABELS = new ArrayList<String>(Arrays.asList("A", "B", "C", "D"));
    }

    private void addMarker(LatLng latLng){
        String currentMarker = findNextMarker();
        if(currentMarker == ""){
            Log.d("MARKER_LIMIT", "Marker Limit Reached");
            return;
        }
        MarkerOptions markerOption = new MarkerOptions().position(latLng).title(currentMarker);
        Marker marker = googleMap.addMarker(markerOption);
        marker.showInfoWindow();
        currentMarkers.add(marker);
        if(currentMarkers.size() > 1){
            //draw polyline
            Log.d("MAP_CLICK", "Adding polyline");
            Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                    .clickable(true)
                    .color(Color.RED)
                    .add(
                            currentMarkers.get(currentMarkers.size() - 2).getPosition(),
                            currentMarkers.get(currentMarkers.size() - 1).getPosition())
            );
            currentPolyLines.add(polyline);

            if(currentMarkers.size() > POLYGON_SIDES - 1){

                //Adding final poly line connecting all
                Polyline polylineEnf = googleMap.addPolyline(new PolylineOptions()
                        .clickable(true)
                        .color(Color.RED)
                        .add(
                                currentMarkers.get(0).getPosition(),
                                currentMarkers.get(currentMarkers.size() - 1).getPosition())
                );
                currentPolyLines.add(polyline);


                PolygonOptions opts=new PolygonOptions();

                for (Marker markerX: currentMarkers) {
                    opts.add(markerX.getPosition());
                }
                Polygon polygon = googleMap.addPolygon(opts.strokeColor(Color.RED).fillColor(Color.parseColor("#4300ff00")));
                polygon.setClickable(true);
            }

        }
    }
//    @Override
//    public void onLocationChanged(@NonNull Location location) {
//
//    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        Log.d("MAP", "Map Long click" + latLng);
        //clear marker
        float[] pointADistance = new float[1];
        float[] pointBDistance = new float[1];
        float[] pointCDistance = new float[1];
        float[] pointEDistance = new float[1];
        Location.distanceBetween(currentMarkers.get(0).getPosition().latitude, currentMarkers.get(0).getPosition().longitude,
                latLng.latitude, latLng.longitude, pointADistance);
        Location.distanceBetween(currentMarkers.get(1).getPosition().latitude, currentMarkers.get(1).getPosition().longitude,
                latLng.latitude, latLng.longitude, pointBDistance);
        Location.distanceBetween(currentMarkers.get(2).getPosition().latitude, currentMarkers.get(2).getPosition().longitude,
                latLng.latitude, latLng.longitude, pointCDistance);
        Location.distanceBetween(currentMarkers.get(3).getPosition().latitude, currentMarkers.get(3).getPosition().longitude,
                latLng.latitude, latLng.longitude, pointEDistance);

        if(pointADistance[0] < POINT_DELETE_DISTANCE_THRESHOLD){
            currentMarkers.get(0).remove();
        }
        if(pointBDistance[0] < POINT_DELETE_DISTANCE_THRESHOLD){
            currentMarkers.get(1).remove();
        }
        if(pointCDistance[0] < POINT_DELETE_DISTANCE_THRESHOLD){
            currentMarkers.get(2).remove();
        }
        if(pointEDistance[0] < POINT_DELETE_DISTANCE_THRESHOLD){
            currentMarkers.get(3).remove();
        }





    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        Log.d("MAP", "Map just click" + latLng);
        addMarker(latLng);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return true;
    }
}