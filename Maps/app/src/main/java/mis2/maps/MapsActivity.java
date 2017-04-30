package mis2.maps;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashSet;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText editText;
    private SharedPreferences sharedPref;
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

        // Move view to Weimar
        LatLng weimar = new LatLng(50.98, 11.33);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(weimar, 14.0f));

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){


            @Override
            public void onMapLongClick(LatLng latLng) {
                String text = editText.getText().toString();
                String counterString = Integer.toString(counter);

                editor = sharedPref.edit();
                editor.clear();
                Set<String> oldSet = sharedPref.getStringSet("key", new HashSet<String>());


                Set<String> newSet = new HashSet<String>();
                newSet.add(counterString+"lat"+Double.toString(latLng.latitude));
                newSet.add(counterString+"long"+Double.toString(latLng.longitude));
                newSet.add(counterString+"text"+ text);
                newSet.addAll(oldSet);

                editor.putStringSet("key", newSet);
                editor.commit();
                counter++;

                mMap.addMarker(new MarkerOptions().position(latLng).title(text));

            }
        });
    }


}
