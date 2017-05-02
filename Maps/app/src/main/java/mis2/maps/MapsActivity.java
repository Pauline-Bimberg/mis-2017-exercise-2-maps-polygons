package mis2.maps;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText editText;
    private Button startPoly;
    private Button endPoly;
    private boolean polyStarted = false;
    PolygonOptions polygon = new PolygonOptions();
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
        endPoly = (Button) findViewById(R.id.endPoly);
        Polygon mapPolygon = mMap.addPolygon(polygon);

        // TODO: Code inspired by:
        sharedPref = getSharedPreferences("Locations", Context.MODE_PRIVATE);
        counter = sharedPref.getInt("Counter",0);

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
                sharedPref = getSharedPreferences("Locations", Context.MODE_PRIVATE);
                counter++;

                editor = sharedPref.edit();

                editor.putString("lat" + counterString , Double.toString(latLng.latitude));
                editor.putString("lng" + counterString , Double.toString(latLng.longitude));
                editor.putString("text" + counterString , text);
                editor.putInt("Counter", counter);
                editor.commit();



                mMap.addMarker(new MarkerOptions().position(latLng).title(text));

                if(polyStarted = true){

                    polygon
                            .add( latLng);

                }

            }
        });

        startPoly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                polyStarted = true;

            }
        });

        endPoly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                polyStarted = false;
                // TODO: Berechnung Shape, Setzen Mittelpunkt mit beschriftung



            }
        });

    }


}
