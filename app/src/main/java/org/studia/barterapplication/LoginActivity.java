package org.studia.barterapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.studia.barterapplication.chat.User;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LoginActivity extends AppCompatActivity {

    private static final int AUTH_UI_REQUEST_CODE = 10001;
    private static final String TAG = "LoginRegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    public void handleLogin(View view) {
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build());
        Intent intent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_launcher_foreground)
                .setAlwaysShowSignInMethodScreen(true)
                .setIsSmartLockEnabled(false)
                .build();
        startActivityForResult(intent, AUTH_UI_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTH_UI_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                setupUserData();
                startMainActivity();
            } else {
                // Signing in failed
                IdpResponse response = IdpResponse.fromResultIntent(data);
                if (response == null) {
                    Log.d(TAG, "onActivityResult: the user has cancelled the sign in request");
                } else {
                    Log.e(TAG, "onActivityResult: ", response.getError());
                }
            }
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        this.finish();
    }

    private void setupUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                FirebaseFirestore.getInstance().collection("Users")
                        .document(user.getUid())
                        .set(new User(user.getUid(), Optional.ofNullable(user.getDisplayName())
                                .orElse("Display Name " + user.getUid()), "default"));
            }
        });
        Log.d(TAG, "onActivityResult: " + user.toString());
    }


}
