package org.studia.barterapplication;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.studia.barterapplication.inventory.Inventory;

public class DetailsActivity extends AppCompatActivity {
    private TextView title;
    private TextView description;
    private ImageView photo;
    boolean isImageFitToScreen = false;
    public static final String INVENTORY_ID = "inventoryId";
    public static final String USER_ID = "userId";
    private ViewGroup.LayoutParams layoutParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        description = findViewById(R.id.details_description);
        title = findViewById(R.id.details_title);
        photo = findViewById(R.id.details_item_photo);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        FirebaseFirestore.getInstance()
                .collection("inventory").document(getIntent().getStringExtra(USER_ID))
                .collection("items")
                .document(getIntent().getStringExtra(INVENTORY_ID))
                .addSnapshotListener((query, e) -> inflateView(query));
        photo.setOnClickListener(v -> photoViewToggle());
    }

    private void inflateView(DocumentSnapshot query) {
        Inventory inventory = query.toObject(Inventory.class);
        title.setText(inventory.getName());
        description.setText(inventory.getDescription());
        if (!"default".equals(inventory.getPhotoUrl()) && !this.isDestroyed()) {
            Glide.with(this)
                    .load(inventory.getPhotoUrl())
                    .into(photo);
        }
    }

    private void photoViewToggle() {
        if (isImageFitToScreen) {
            isImageFitToScreen = false;
            photo.setLayoutParams(layoutParams);
            photo.setAdjustViewBounds(true);
        } else {
            isImageFitToScreen = true;
            layoutParams = photo.getLayoutParams();
            photo.setLayoutParams(new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            photo.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }
}
