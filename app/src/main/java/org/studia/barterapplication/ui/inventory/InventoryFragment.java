package org.studia.barterapplication.ui.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.rtchagas.pingplacepicker.PingPlacePicker;

import org.studia.barterapplication.AddEditInventoryActivity;
import org.studia.barterapplication.R;
import org.studia.barterapplication.inventory.Inventory;
import org.studia.barterapplication.inventory.UserLocationData;

import static android.app.Activity.RESULT_OK;

public class InventoryFragment extends Fragment {

    public static final String INVENTORY_ID = "inventory_id";

    private static final int REQUEST_PLACE_PICKER = 1200;
    private InventoryAdapter adapter;
    private RecyclerView recyclerView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inventory, container, false);
        setupRecyclerView(root);
        setupFloatingActionButton(root);
        root.findViewById(R.id.place_picker)
                .setOnClickListener(view -> startPlacePicker());
        return root;
    }

    private void setupRecyclerView(View root) {
        recyclerView = root.findViewById(R.id.inventory_recycler_view);
        CollectionReference inventory = FirebaseFirestore.getInstance().collection("inventory")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("items");
        FirestoreRecyclerOptions<Inventory> options = new FirestoreRecyclerOptions.Builder<Inventory>()
                .setQuery(inventory, Inventory.class)
                .build();

        adapter = new InventoryAdapter(options);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        setupSwipeActions();
        adapter.setOnItemClickListener(this::onItemClick);
    }

    private void onItemClick(DocumentSnapshot documentSnapshot) {
        Intent intent = new Intent(this.getContext(), AddEditInventoryActivity.class);
        intent.putExtra(INVENTORY_ID, documentSnapshot.getId());
        startActivity(intent);
    }

    private void setupSwipeActions() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void startPlacePicker() {
        PingPlacePicker.IntentBuilder builder = new PingPlacePicker.IntentBuilder();
        builder.setAndroidApiKey(getContext().getResources().getString(R.string.google_api_key))
                .setMapsApiKey(getContext().getResources().getString(R.string.google_maps_key));
        try {
            Intent placeIntent = builder.build(getActivity());
            startActivityForResult(placeIntent, REQUEST_PLACE_PICKER);
        } catch (Exception ex) {
            // Google Play services is not available...
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == RESULT_OK) {
                Place place = PingPlacePicker.getPlace(data);
                if (place != null) {
                    setInventoryLocation(place);
                }
            }
        }
    }

    private void setInventoryLocation(Place place) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        UserLocationData userLocationData =
                new UserLocationData(
                        new GeoPoint(place.getLatLng().latitude, place.getLatLng().longitude),
                        currentUser.getUid(),
                        currentUser.getDisplayName());
        FirebaseFirestore.getInstance()
                .collection("Location")
                .document(currentUser.getUid())
                .set(userLocationData)
                .addOnFailureListener(x -> Toast.makeText(getContext(), "Failed", Toast.LENGTH_LONG))
                .addOnSuccessListener(x -> Toast.makeText(getContext(), "Success", Toast.LENGTH_LONG));
    }

    private void setupFloatingActionButton(View root) {
        FloatingActionButton button = root.findViewById(R.id.floatingActionButton);
        button.setOnClickListener(e -> {
            startActivity(new Intent(this.getContext(), AddEditInventoryActivity.class));
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }
}
