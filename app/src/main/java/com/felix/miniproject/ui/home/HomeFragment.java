package com.felix.miniproject.ui.home;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.felix.miniproject.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HomeFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap googleMap;
    private ImageButton btnLocation;
    private List<Marker> markers = new ArrayList<Marker>();
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        btnLocation = root.findViewById(R.id.imageButton);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        SupportMapFragment mapFragment =  (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.btnLocation);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMapInstance) {
                googleMap = googleMapInstance;
                googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        markers.forEach(new Consumer<Marker>() {
                            @Override
                            public void accept(Marker marker) {
                                marker.remove();
                            }
                        });
                        markers.clear();
                        Marker marker = googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Search for near by"));
                        markers.add(marker);
                        SearchNearByRestrant(latLng);
                    }
                });
            }
        });
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if(location != null){
                                    LatLng myplce = new LatLng(location.getLatitude(), location.getLongitude());
                                    Marker lastMarker =  googleMap.addMarker(new MarkerOptions().position(myplce).title("This is my location").draggable(true));
                                    markers.add(lastMarker);
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(myplce));
                                }else {
                                    Toast.makeText(getContext(), "don't retrieve any location", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
        return root;
    }

    private void SearchNearByRestrant(LatLng latLng){
        StringBuilder stringBuilder = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyC7pxD4N9ioRXo-XfpkiXFJQbvSlO6ua-c&location=");
        stringBuilder.append(String.valueOf(latLng.latitude));
        stringBuilder.append(String.valueOf(","));
        stringBuilder.append(String.valueOf(latLng.longitude));
        stringBuilder.append("&radius=2000&keyword=shop");
        String url = stringBuilder.toString();
        RequestQueue queue = Volley.newRequestQueue(getContext());
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jobject= new JSONObject(response);
                            JSONArray results = jobject.getJSONArray("results");
                            int length = results.length();
                            for(int i=0; i<length; i++){
                                JSONObject oneShop = results.getJSONObject(i);
                                JSONObject location =  oneShop.getJSONObject("geometry").getJSONObject("location");
                                double lat = location.getDouble("lat");
                                double longtitude = location.getDouble("lng");
                                LatLng myplce = new LatLng(lat, longtitude);
                                Marker lastMarker =  googleMap.addMarker(new MarkerOptions().
                                        position(myplce).title(oneShop.getString("name")));
                                markers.add(lastMarker);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }
}
