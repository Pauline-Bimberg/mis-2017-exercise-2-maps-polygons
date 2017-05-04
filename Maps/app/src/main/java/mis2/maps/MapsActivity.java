package mis2.maps;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText editText;
    private LatLng myLatLng = new LatLng(50.98, 11.33);
    private Button startPoly;
    private Button clearButton;
    private Polygon mypolygon;
    private PolygonOptions polyOpt;
    private boolean polyStarted = false;
    private ArrayList pointList = new ArrayList();
    private LocationManager locationManager;


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




        // getting the current location should work for older Android versions
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

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    myLatLng = new LatLng(latitude, longitude);

                   Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> adressList = geocoder.getFromLocation(latitude, longitude, 1);
                        String str = adressList.get(0).getLocality()+",";
                        str += adressList.get(0).getCountryName();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


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
        }
        else if (locationManager.isProviderEnabled(locationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    myLatLng = new LatLng(latitude, longitude);

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

        }

        else {
            myLatLng =  new LatLng(50.00, 180.57);


        }




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



        // sets the Location to Weimar, if the Phones Location can't be accessed
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 14.0f));
        mMap.addMarker(new MarkerOptions().position(myLatLng).title("My Position"));

        // Code inspired by: http://wptrafficanalyzer.in/blog/adding-multiple-marker-locations-in-google-maps-android-api-v2-and-save-it-in-shared-preferences/
        // get shared Preferences
        sharedPref = getSharedPreferences("Locations", Context.MODE_PRIVATE);
        // get the LocationCount from the shared preferences
        counter = sharedPref.getInt("Counter",0);


        // check if there are any Locations saved
        if(counter != 0){
            String latitude = "";
            String longitude = "";
            String text = "";

            // get the Lat, Long and Text for each Location from the according Strings in SharedPref, add marker on Location
            for(int i = 0; i<counter; i++){
                String s = Integer.toString(i);
                latitude = sharedPref.getString("lat"+s, "0");
                longitude = sharedPref.getString("lng" +s,"0");
                text = sharedPref.getString("text"+s, "0");

                LatLng position = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                mMap.addMarker(new MarkerOptions().position(position).title(text));
            }
        }




        // Check for longClicks on the Map
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){


            @Override
            public void onMapLongClick(LatLng latLng) {

                String text = editText.getText().toString();
                String counterString = Integer.toString(counter);

                myLatLng = latLng;

                // get SharedPreferences Object
                sharedPref = getSharedPreferences("Locations", Context.MODE_PRIVATE);

                counter++;

                // edit the sharedPreferences
                editor = sharedPref.edit();
                // add the Location of the LongClick and the text of the textfield to the sharedPreferences
                editor.putString("lat" + counterString, Double.toString(latLng.latitude));
                editor.putString("lng" + counterString, Double.toString(latLng.longitude));
                editor.putString("text" + counterString, text);
                editor.putInt("Counter", counter);

                // If a Polygon has been started, add the Location of the LongClick to the pointList
                if (polyStarted == true) {
                    pointList.add(latLng);
                }
                // commit the contents of the editor to the sharedPreferences
                editor.commit();

                // Add a new marker at the location of the LongClick
                mMap.addMarker(new MarkerOptions().position(latLng).title(text));

                // if there are points in the pointlist, add them to the polygon
               if (pointList.isEmpty() == false) {
                    mypolygon.setPoints(pointList);
               }


            }
        });

        // Listener for the Start/End Polygon Button
        startPoly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if start polygon has been false and the button is clicked, set true and change button-text to End Polygon
                if (polyStarted == false){
                        polyStarted = true;
                    startPoly.setText("End Polygon");
                    // add a new Polygon to our Map, defining a default point and Colors
                polyOpt = new PolygonOptions()
                        .add(new LatLng(0, 0))
                        .strokeColor(Color.argb(255, 102, 204, 0))
                        .fillColor(Color.argb(100, 102, 204, 0));
                mypolygon = mMap.addPolygon(polyOpt);
            }
            else {
                    // if the polygon has already been started get its center Point and add it with the corresponding Size as Title
                    LatLng centroid = getPolyCentroid(pointList);
                    mMap.addMarker(new MarkerOptions().position(centroid).title(getPolySize(pointList)));
                    // clear the point List after the Polygon has ended
                    pointList.clear();
                    // set polygonstarted to false and change the buttontext accordingly
                    polyStarted = false;


                    startPoly.setText("Start Polygon");
                }
        }});

        // Listener for the clear Button
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // if the isClearButton is Clicked, delete all Locations from the map and from the sharedPrefenrences

                sharedPref = getSharedPreferences("Locations", Context.MODE_PRIVATE);
                editor = sharedPref.edit();
                editor.clear();
                editor.commit();

                mMap.clear();
                // set polygonstarted to false and change the buttontext accordingly
                polyStarted = false;
                startPoly.setText("Start Polygon");

            }
        });

    }

    // Calculating the centroid of a polygon (Code inspired by: http://stackoverflow.com/questions/18440823/how-do-i-calculate-the-center-of-a-polygon-in-google-maps-android-api-v2)
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

    // Calculate the Size of the Polygon from its points (Code inspired by: www.mathopenref.com/coordpolygonarea.html)
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
