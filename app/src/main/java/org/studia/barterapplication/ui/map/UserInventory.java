package org.studia.barterapplication.ui.map;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.studia.barterapplication.DetailsActivity;
import org.studia.barterapplication.R;
import org.studia.barterapplication.chat.MessageActivity;
import org.studia.barterapplication.chat.User;
import org.studia.barterapplication.inventory.Inventory;
import org.studia.barterapplication.ui.inventory.InventoryAdapter;

import de.hdodenhof.circleimageview.CircleImageView;

import static org.studia.barterapplication.chat.MessageActivity.USER_ID;
import static org.studia.barterapplication.ui.map.MapFragment.USER_UID;

public class UserInventory extends AppCompatActivity {
    private CircleImageView profileImageView;
    private TextView displayName;
    private InventoryAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_inventory);
        profileImageView = findViewById(R.id.user_profile_photo);
        displayName = findViewById(R.id.user_name);
        Button button = findViewById(R.id.start_conversation);
        String uid = getIntent().getStringExtra(USER_UID);
        if (uid != null) {
            loadUserData(uid);
        }
        button.setOnClickListener(view -> startMessageActivity(uid));
        setupRecycleView(uid);
    }

    private void startMessageActivity(String uid) {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra(USER_ID, uid);
        startActivity(intent);
    }

    private void loadUserData(String uid) {
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(uid).get().addOnCompleteListener(this::inflateViewWithUser);
    }

    private void inflateViewWithUser(Task<DocumentSnapshot> documentSnapshotTask) {
        User user = documentSnapshotTask.getResult().toObject(User.class);
        displayName.setText(user.getUsername());
        if ("default".equals(user.getImageUrl())) {
            profileImageView.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profileImageView);
        }
    }

    private void setupRecycleView(String userId) {
        RecyclerView recyclerView = findViewById(R.id.user_inventory_recycler_view);
        CollectionReference inventory = FirebaseFirestore.getInstance()
                .collection("inventory")
                .document(userId)
                .collection("items");
        FirestoreRecyclerOptions<Inventory> options = new FirestoreRecyclerOptions.Builder<Inventory>()
                .setQuery(inventory, Inventory.class)
                .build();

        adapter = new InventoryAdapter(options);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.setOnItemClickListener(documentSnapshot -> startDetailsActivity(userId, documentSnapshot));
    }

    private void startDetailsActivity(String userId, DocumentSnapshot documentSnapshot) {
        Inventory inventory1 = documentSnapshot.toObject(Inventory.class);
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(DetailsActivity.INVENTORY_ID, inventory1.getId());
        intent.putExtra(DetailsActivity.USER_ID, userId);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }
}
