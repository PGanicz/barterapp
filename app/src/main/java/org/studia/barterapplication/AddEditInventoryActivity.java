package org.studia.barterapplication;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.studia.barterapplication.inventory.Inventory;
import org.studia.barterapplication.ui.inventory.InventoryFragment;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AddEditInventoryActivity extends AppCompatActivity {
    private static final int TAKE_IMAGE_CODE = 1002;
    private EditText itemName;
    private EditText description;
    private ImageView itemPhoto;
    private Uri imageUri;
    private CollectionReference inventory;
    private StorageReference storageReference;
    private String inventoryId;
    private Button uploadButton;
    private ProgressBar progressBar;
    private ViewGroup.LayoutParams layoutParams;
    private boolean isImageFitToScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_inventory);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        itemName = findViewById(R.id.inventory_item_name);
        description = findViewById(R.id.description);
        itemPhoto = findViewById(R.id.itemPhoto);
        inventory = FirebaseFirestore.getInstance()
                .collection("inventory")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("items");
        progressBar = findViewById(R.id.upload_progress_bar);
        uploadButton = findViewById(R.id.add_image_button);
        storageReference = FirebaseStorage.getInstance().getReference();
        checkFilePermission();
        setCurrentInventory();
        progressBar.setVisibility(View.GONE);
        itemPhoto.setOnClickListener(v -> onItemPhotoClick());
    }

    private void setCurrentInventory() {
        String inventoryId = getIntent().getStringExtra(InventoryFragment.INVENTORY_ID);
        if (inventoryId != null) {
            this.inventoryId = inventoryId;
            loadInventoryData(inventoryId);
        } else {
            this.inventoryId = inventory.document().getId();
        }
    }

    private void loadInventoryData(String inventoryId) {
        FirebaseFirestore.getInstance()
                .collection("inventory").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("items")
                .document(inventoryId)
                .addSnapshotListener((query, e) -> {
                    inflateView(query);
                });
    }

    private void inflateView(DocumentSnapshot query) {
        Inventory inventory = query.toObject(Inventory.class);
        itemName.setText(inventory.getName());
        description.setText(inventory.getDescription());
        if (!"default".equals(inventory.getPhotoUrl()) && !this.isDestroyed()) {
            Glide.with(this)
                    .load(inventory.getPhotoUrl())
                    .into(itemPhoto);
        }
    }

    private void onItemPhotoClick() {
        if (isImageFitToScreen) {
            isImageFitToScreen = false;
            itemPhoto.setLayoutParams(layoutParams);
            itemPhoto.setAdjustViewBounds(true);
        } else {
            isImageFitToScreen = true;
            layoutParams = itemPhoto.getLayoutParams();
            itemPhoto.setLayoutParams(new ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            itemPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    private void checkFilePermission() {
        int permissionCheck = AddEditInventoryActivity.this.checkSelfPermission("Manifest.permissions.READ_EXTERNAL_STORAGE");
        permissionCheck += AddEditInventoryActivity.this.checkSelfPermission("Manifest.permissions.WRITE_EXTERNAL_STORAGE");
        if (permissionCheck != 0) {
            this.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_item_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_item) {
            saveItem();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveItem() {
        progressBar.setVisibility(View.VISIBLE);
        itemName.setEnabled(false);
        uploadButton.setEnabled(false);
        updateItemTextData();
        if (imageUri != null) {
            updateItemPhoto();
        }
        finish();
    }

    private void updateItemPhoto() {
        storageReference
                .child("items")
                .child(inventoryId + ".jpeg")
                .putFile(imageUri)
                .addOnCompleteListener(snapshotTask -> onPhotoUploadCompleted())
                .addOnFailureListener(err -> onUploadFailure());
    }

    private void updateItemTextData() {
        String title = itemName.getText().toString().trim();
        String description = itemName.getText().toString().trim();
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("title", title);
            put("description", description);
        }};
        inventory.document(inventoryId).update(map);
    }

    private void onUploadFailure() {
        progressBar.setVisibility(View.GONE);
        itemName.setEnabled(true);
        uploadButton.setEnabled(true);
        Toast.makeText(this, "File Upload failed", Toast.LENGTH_LONG).show();
    }

    private void onPhotoUploadCompleted() {
        getContentResolver().delete(imageUri, "", new String[]{});
        storageReference
                .child("items")
                .child(inventoryId + ".jpeg").getDownloadUrl().addOnSuccessListener(uri -> {
            progressBar.setVisibility(View.GONE);
            itemName.setEnabled(true);
            uploadButton.setEnabled(true);
            inventory.document(inventoryId).update(Collections.singletonMap("photoUrl", uri.toString()));
            finish();
        }).addOnFailureListener(x -> finish());
    }

    public void handleImageAddClick(View view) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, inventoryId);
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_IMAGE_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_IMAGE_CODE && resultCode == RESULT_OK) {
            try {
                Bitmap b = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                itemPhoto.setImageBitmap(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
