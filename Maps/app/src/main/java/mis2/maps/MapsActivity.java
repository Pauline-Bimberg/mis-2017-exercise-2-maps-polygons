package mis2.maps;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.sql.Array;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText editText;
    private LatLng myLatLng;
    private Button startPoly;
    private Button clearButton;
    private Polygon mypolygon;
    private PolygonOptions polyOpt;
    private boolean polyStarted = false;
    private ArrayList pointList = new ArrayList();
    LocationManager locationManager;

    private int polycounter = 0;


    SharedPreferences sharedPref;

    private SharedPreferences.Editor editor;
    private int counter = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        editText = (EditText) findViewById(R.id.editText);
        startPoly = (Button) findViewById(R.id.startPoly);
        clearButton = (Button) findViewById(R.id.clearButton);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });


        // TODO: Code inspired by:
        sharedPref = getSharedPreferences("Locations", Context.MODE_PRIVATE);
        counter = sharedPref.getInt("Counter",0);

        polycounter = sharedPref.getInt("polyCounter",0);

        if(counter != 0){
            String latitude = "";
            String longitude = "";
            String text = "";

            for(int i = 0; i<counter; i++){
                String s = Integer.toString(i);
                latitude = sharedPref.getString("lat"+s, "0");
                longitude = sharedPref.getString("lng" +s,"0");
                text = sharedPref.getString("text"+s, "0");

                LatLng position = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                mMap.addMarker(new MarkerOptions().position(position).title(text));
            }
        }



        // Move view to Weimar
        LatLng weimar = new LatLng(50.98, 11.33);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(weimar, 14.0f));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){


            @Override
            public void onMapLongClick(LatLng latLng) {

                String text = editText.getText().toString();
                String counterString = Integer.toString(counter);

                myLatLng = latLng;

                sharedPref = getSharedPreferences("Locations", Context.MODE_PRIVATE);
                counter++;

                editor = sharedPref.edit();

                editor.putString("lat" + counterString, Double.toString(latLng.latitude));
                editor.putString("lng" + counterString, Double.toString(latLng.longitude));
                editor.putString("text" + counterString, text);
                editor.putInt("Counter", counter);


                if (polyStarted == true) {
                    pointList.add(latLng);
                }

                editor.commit();


                mMap.addMarker(new MarkerOptions().position(latLng).title(text));



               if (pointList.isEmpty() == false) {
                    mypolygon.setPoints(pointList);
               }


            }
        });

        startPoly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (polyStarted == false){
                        polyStarted = true;
                    startPoly.setText("End Polygon");

                polyOpt = new PolygonOptions()
                        .add(new LatLng(0, 0))
                        .strokeColor(Color.argb(255, 102, 204, 0))
                        .fillColor(Color.argb(100, 102, 204, 0));
                mypolygon = mMap.addPolygon(polyOpt);
            }
            else {
                    LatLng centroid = getPolyCentroid(pointList);
                    mMap.addMarker(new MarkerOptions().position(centroid).title(getPolySize(pointList)));

                    pointList.clear();
                    polyStarted = false;


                    startPoly.setText("Start Polygon");
                }
        }});

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPref = getSharedPreferences("Locations", Context.MODE_PRIVATE);
                editor = sharedPref.edit();
                editor.clear();
                editor.commit();

                mMap.clear();

            }
        });

    }

    // Calculating the centroid of a polygon
    private LatLng getPolyCentroid(ArrayList<LatLng> pointlist){
        LatLng centroid;
        int pointCount = pointlist.size();
        double latitude = 0.0;
        double longitude = 0.0;

        for (int i = 0; i<pointlist.size(); i++){

            latitude += pointlist.get(i).latitude;
            longitude += pointlist.get(i).longitude;
        }

        latitude = latitude/pointCount;
        longitude = longitude/pointCount;

        centroid = new LatLng(latitude, longitude);


        return centroid;
    }

    private String getPolySize(ArrayList<LatLng> pointlist){
        double polySize;
        double polySum = 0.0;

        double lat1 = pointlist.get(0).latitude;
        double long1 = pointlist.get(0).longitude;
        double lat2 = pointlist.get(pointlist.size()-1).latitude;
        double long2 = pointlist.get(pointlist.size()-1).longitude;

        for (int i=0; i<(pointlist.size() - 1); i++){

            double latitude1 = pointlist.get(i).latitude;
            double longitude1 = pointlist.get(i).longitude;
            double latitude2 = pointlist.get(i+1).latitude;
            double longitude2 = pointlist.get(i+1).longitude;

            polySum += (latitude1*longitude2) - (longitude1*latitude2);


        }
        polySum = polySum + (lat2*long1 - long2*lat1);

        polySize = Math.abs(polySum) / 2;

        return Double.toString(polySize);
    }
}
