package com.strollimo.android;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;
import com.strollimo.android.dialog.DemoFinishedDialog;

public class MapActivity extends Activity {
    private GoogleMap mMap;
    private LocationClient mLocationClient;
    private PlacesService mPlacesService;
    private StrollimoPreferences mPrefs;

    private UserService mUserService;
    private boolean firstStart = true;
    private ImageView mPlaceImage;
    private TextView mPlaceTitle;
    private View mRibbonPanel;
    private View mRibbonTouchView;
    private MapPlacesModel mMapPlacesModel;

    private GoogleMap.OnInfoWindowClickListener onInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            Log.i("BB", "on info window selected");
            launchDetailsActivity();
        }
    };
    private GoogleMap.OnMarkerClickListener mOnMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            mMapPlacesModel.onMarkerClick(marker);
            displayRibbon(mMapPlacesModel.getSelectedPlace());
            return false;
        }
    };
    private GoogleMap.OnMapClickListener mOnMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            hideRibbon();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = ((StrollimoApplication)getApplication()).getService(StrollimoPreferences.class);
        firstStart = true;
        setContentView(R.layout.stroll_map_layout);
        mPlacesService = ((StrollimoApplication) getApplication()).getService(PlacesService.class);
        mUserService = ((StrollimoApplication) getApplication()).getService(UserService.class);
        mPlaceImage = (ImageView) findViewById(R.id.place_image);
        mPlaceTitle = (TextView) findViewById(R.id.place_title);
        mRibbonPanel = findViewById(R.id.ribbon_panel);
        mRibbonTouchView = findViewById(R.id.ribbon_touch_view);

        mRibbonTouchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("BB", "on click");
                launchDetailsActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
            mMapPlacesModel.refreshSelectedMarker();
        if (mUserService.getFoundPlacesNum() == mPlacesService.getPlacesCount()) {
            new DemoFinishedDialog().show(getFragmentManager(), "dialog");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("BB", "onStart");
        setUpMapIfNecessary();

        setUpLocationClientIfNecessary();
        setUpMapMarkersIfNecessary();
    }

    private void setUpMapIfNecessary() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            mMap.setMyLocationEnabled(true);
            mMap.setOnInfoWindowClickListener(onInfoWindowClickListener);
            mMap.setOnMarkerClickListener(mOnMarkerClickListener);
            mMap.setOnMapClickListener(mOnMapClickListener);
            mMap.getUiSettings().setZoomControlsEnabled(false);
            mMap.getUiSettings().setCompassEnabled(false);
        }
    }

    public void setUpMapMarkersIfNecessary() {
        if (mMapPlacesModel != null) {
            return;
        }

        mMapPlacesModel = new MapPlacesModel(mUserService);
        mMap.clear();
        for (Place place : mPlacesService.getAllPlaces()) {
            addPlaceToMap(place);
        }
    }

    private void addPlaceToMap(Place place) {
        BitmapDescriptor bitmapDescriptor;
        if (mUserService.isPlaceCaptured(place.getmId())) {
            bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.pink_flag);
        } else {
            bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.azure_flag);
        }

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(place.getmLat(), place.getmLon()))
                .title(place.getmTitle()).icon(bitmapDescriptor));
        mMapPlacesModel.add(place, marker);
    }

    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

    private void launchDetailsActivity() {
        Place selectedPlace = mMapPlacesModel.getSelectedPlace();
        if (selectedPlace != null) {
            this.startActivity(DetailsActivity.createDetailsIntent(this, selectedPlace.getmId()));
        }
    }

    private void setUpLocationClientIfNecessary() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(this, new GooglePlayServicesClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    if (firstStart) {
                        Location loc = mLocationClient.getLastLocation();
                        Place place = mPlacesService.getPlaceById(1);
                        CameraPosition pos = CameraPosition.builder().target(new LatLng(place.getmLat(), place.getmLon())).zoom(16f).tilt(45).build();
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
                        firstStart = false;
                    }
                }

                @Override
                public void onDisconnected() {

                }
            }, new GooglePlayServicesClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(ConnectionResult connectionResult) {

                }
            }
            );
        }
        if (!mLocationClient.isConnected()) {
            mLocationClient.connect();
        }
    }

    private void displayRibbon(Place place) {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_right);
        mRibbonPanel.setVisibility(View.VISIBLE);
        mPlaceImage.setImageBitmap(place.getBitmap());
        mPlaceTitle.setText(place.getmTitle().toUpperCase());
        mRibbonPanel.startAnimation(anim);
    }

    private void hideRibbon() {
        if (mMapPlacesModel.getSelectedPlace() != null) {
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_out_to_left);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mRibbonPanel.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mRibbonPanel.startAnimation(anim);
        }
        mMapPlacesModel.hideSelectedPlace();
    }

}