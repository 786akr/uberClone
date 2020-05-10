package com.example.uberclone;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.List;

public class Passenger extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    private Button requestcar;
    private Button logut;
    private Boolean IsUberCancelled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        requestcar = findViewById(R.id.button);
        requestcar.setOnClickListener(this);
        logut=findViewById(R.id.LogoutFromPassengerActivity);
             logut.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View v) {
                     ParseUser.logOutInBackground(new LogOutCallback() {
                         @Override
                         public void done(ParseException e) {
                             finish();
                         }
                     });
                 }
             });
        mapFragment.getMapAsync(this);
        ParseQuery<ParseObject> gadichidi = new ParseQuery<ParseObject>("RequestCar");
        gadichidi.whereEqualTo("Username", ParseUser.getCurrentUser().getUsername());
        gadichidi.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects.size() > 0 && e == null) {
                    IsUberCancelled = false;
                    requestcar.setText("Uber request Is Canceled");
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Updatepassengercolation(location);

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
        };
        // Add a marker in Sydney and mo ve the camera
        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(Passenger.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(Passenger.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2000);
                } else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location currentLocationPassenger = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Updatepassengercolation(currentLocationPassenger);
                }
            }
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2000 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(Passenger.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location currentLocationPassenger = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Updatepassengercolation(currentLocationPassenger);
        }
    }

    private void Updatepassengercolation(Location pLocation) {
        LatLng Passengerlocation = new LatLng(pLocation.getLatitude(), pLocation.getLongitude());
        mMap.clear();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Passengerlocation, 15));
        mMap.addMarker(new MarkerOptions().position(Passengerlocation).title("You are here"));
    }

    @Override
    public void onClick(View v) {
        if (IsUberCancelled) {
            if (ContextCompat.checkSelfPermission(Passenger.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location PassengerCurretLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (PassengerCurretLocation != null) {
                    ParseObject RequestCar = new ParseObject("RequestCar");
                    RequestCar.put("Username", ParseUser.getCurrentUser().getUsername());
                    ParseGeoPoint UserLocation = new ParseGeoPoint(PassengerCurretLocation.getLatitude(), PassengerCurretLocation.getLongitude());
                    RequestCar.put("UserLocation", UserLocation);
                    RequestCar.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                FancyToast.makeText(Passenger.this, "Location Had Been Send", FancyToast.LENGTH_LONG, FancyToast.SUCCESS, true).show();
                                requestcar.setText("cancel uber Driver");
                                IsUberCancelled = false;
                            }
                        }
                    });
                } else {
                    FancyToast.makeText(Passenger.this, "Something Went Wrong", FancyToast.LENGTH_LONG, FancyToast.ERROR, true).show();
                }
            }
        } else {
            ParseQuery<ParseObject> gadichidi = new ParseQuery<ParseObject>("RequestCar");
            gadichidi.whereEqualTo("Username", ParseUser.getCurrentUser().getUsername());

            gadichidi.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> RequestList, ParseException e) {
                    if (RequestList.size() > 0 && e == null) {
                        IsUberCancelled = true;
                        requestcar.setText("Request Uber");
                        for (ParseObject car : RequestList) {
                            car.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        FancyToast.makeText(Passenger.this, "Request delete", FancyToast.LENGTH_LONG, FancyToast.INFO, true).show();

                                    }
                                }
                            });
                        }
                    }
                }
            });
        }
    }
}

