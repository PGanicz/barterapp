package org.studia.barterapplication.ui.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import org.studia.barterapplication.R;
import org.studia.barterapplication.chat.User;
import org.studia.barterapplication.inventory.UserLocationData;

import java.util.HashMap;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    public static final String USER_UID = "UID";

    private GoogleMap map;
    private Map<String, UserLocationData> mMarkerMap = new HashMap<>();
    private FirebaseUser user;
    private BitmapDescriptor bitmapDescriptor;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        MapView mapView = view.findViewById(R.id.mapView);
        if (mapView != null) {
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(getActivity(), this::onUserLastLocation);

    }

    private void onUserLastLocation(Location location) {
        if (location != null) {
            UserLocationData userLocationData =
                    new UserLocationData(
                            new GeoPoint(location.getLatitude(), location.getLongitude()),
                            user.getUid(),
                            user.getDisplayName());
            LatLng currentLocation = new LatLng(userLocationData.getLocation().getLatitude(),
                    userLocationData.getLocation().getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        bitmapDescriptor = vectorToBitmap(R.drawable.ic_gps_fixed_black_24dp, 0xFF000000);
        MapsInitializer.initialize(getContext());
        map = googleMap;
        // Add a marker in Sydney and move the camera
        if (checkLocationPermission()) return;

        map.getUiSettings()
                .setZoomControlsEnabled(true);
        FirebaseFirestore.getInstance().collection("Location")
                .addSnapshotListener((querySnapshot, e) -> {
                    map.clear();
                    querySnapshot.getDocuments().stream()
                            .map(query -> query.toObject(UserLocationData.class))
                            .forEach(this::renderLocation);

                });
        map.setOnMarkerClickListener(this::onMarkerClick);
    }

    private void renderLocation(UserLocationData userLocationData) {
        LatLng latLng = new LatLng(userLocationData.getLocation().getLatitude(),
                userLocationData.getLocation().getLongitude());
        if (user.getUid().equals(userLocationData.getUid())) {
            renderCurrentUserInventory(userLocationData, latLng);
        } else {
            renderOthersInventory(userLocationData, latLng);
        }
    }

    private void renderOthersInventory(UserLocationData userLocationData, LatLng latLng) {
        FirebaseFirestore.getInstance().collection("Users")
                .document(userLocationData.getUid())
                .addSnapshotListener((snapshot, ee) -> {
                    renderCircleAvatar(userLocationData, latLng, snapshot);
                });
    }

    private void renderCircleAvatar(UserLocationData userLocationData, LatLng latLng, DocumentSnapshot snapshot) {
        User user = snapshot.toObject(User.class);
        if (!"default".equals(user.getImageUrl())) {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(userLocationData.getDisplayName());
            Glide.with(this)
                    .asBitmap()
                    .apply(RequestOptions.circleCropTransform())
                    .load(user.getImageUrl())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Marker marker = map.addMarker(options);
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(resource));
                            mMarkerMap.put(marker.getId(), userLocationData);
                        }
                    });
        } else {
            MarkerOptions options = new MarkerOptions()
                    .position(latLng)
                    .title(userLocationData.getDisplayName());
            Marker marker = map.addMarker(options);
            mMarkerMap.put(marker.getId(), userLocationData);
        }
    }

    private void renderCurrentUserInventory(UserLocationData userLocationData, LatLng latLng) {
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .icon(bitmapDescriptor)
                .title("Moja skrzynka");
        Marker marker = map.addMarker(options);
        mMarkerMap.put(marker.getId(), userLocationData);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
    }

    private boolean onMarkerClick(Marker marker) {
        UserLocationData uld = mMarkerMap.get(marker.getId());
        if (uld != null && !FirebaseAuth.getInstance().getUid().equals(uld.getUid())) {
            Intent intent = new Intent(getContext(), UserInventory.class);
            intent.putExtra(USER_UID, uld.getUid());
            startActivity(intent);
        }
        return false;
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1004);
            return true;
        }
        return false;
    }

    private BitmapDescriptor vectorToBitmap(@DrawableRes int id, @ColorInt int color) {
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(), id, null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable, color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
