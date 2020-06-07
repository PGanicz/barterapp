package org.studia.barterapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.studia.barterapplication.chat.User;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int TAKE_IMAGE_CODE = 10001;
    private static final String TAG = "ProfileActivity";
    private CircleImageView profileImageView;
    private TextInputEditText displayNameInput;
    private ProgressBar progressBar;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileImageView = findViewById(R.id.profile_image);
        displayNameInput = findViewById(R.id.displayName);
        progressBar = findViewById(R.id.updateProfileProgressBar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            setupUserData(user);
        }
        progressBar.setVisibility(View.GONE);
    }

    private void setupUserData(FirebaseUser user) {
        Log.d(TAG, "onCreate: " + user.getDisplayName());
        if (user.getDisplayName() != null) {
            displayNameInput.setText(user.getDisplayName());
            displayNameInput.setSelection(user.getDisplayName().length());
        }
        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(profileImageView);
        }
    }

    public void updateProfile(final View view) {
        view.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayNameInput.getText().toString())
                .build();
        handleUpload(bitmap);
        user.updateProfile(request)
                .addOnSuccessListener(result -> updateSuccess(view, user))
                .addOnFailureListener(e -> updateResult(view, "Profile image failed..."));

    }

    private void updateResult(View view, String s) {
        progressBar.setVisibility(View.GONE);
        view.setEnabled(true);
        Toast.makeText(ProfileActivity.this, s, Toast.LENGTH_SHORT).show();
    }

    private void updateSuccess(View view, FirebaseUser user) {
        FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid())
                .set(new User(user.getUid(), user.getDisplayName(), user.getPhotoUrl().toString()));
        updateResult(view, "Updated successfully");
    }

    public void handleImageClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_IMAGE_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TAKE_IMAGE_CODE && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            profileImageView.setImageBitmap(bitmap);
        }
    }

    private void handleUpload(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final StorageReference reference = FirebaseStorage.getInstance().getReference()
                .child("profileImages")
                .child(uid + ".jpeg");

        reference.putBytes(baos.toByteArray())
                .addOnSuccessListener(taskSnapshot -> getDownloadUrl(reference))
                .addOnFailureListener(e -> Log.e(TAG, "onFailure: ", e.getCause()));
    }

    private void getDownloadUrl(StorageReference reference) {
        reference.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Log.d(TAG, "onSuccess: " + uri);
                    setUserProfileUrl(uri);
                });
    }

    private void setUserProfileUrl(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setPhotoUri(uri)
                .build();
        user.updateProfile(request)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ProfileActivity.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, "Profile image failed...", Toast.LENGTH_SHORT).show());

        ;
    }
}
