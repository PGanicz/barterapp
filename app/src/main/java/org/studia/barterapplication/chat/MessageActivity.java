package org.studia.barterapplication.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.studia.barterapplication.R;
import org.studia.barterapplication.ui.map.UserInventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

import static org.studia.barterapplication.ui.map.MapFragment.USER_UID;

public class MessageActivity extends AppCompatActivity {
    public static final String USER_ID = "userid";
    private RecyclerView recyclerView;
    private ImageView profile_image;
    private String userId;
    private EditText textToSend;
    private TextView username;
    private FirebaseUser fuser;
    private TreeMap<Timestamp, Chat> mchat;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        getSupportActionBar().hide();
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        ImageButton btnSend = findViewById(R.id.btn_send);
        textToSend = findViewById(R.id.text_send);
        userId = getIntent().getStringExtra(USER_ID);
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        btnSend.setOnClickListener(view -> onSendButtonClicked());

        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(userId).get()
                .addOnCompleteListener(this::renderUserAndMessages);
        findViewById(R.id.profile_image).setOnClickListener(view -> onUserProfileClick());
        seenMessage(userId);
    }

    private void onUserProfileClick() {
        Intent intent = new Intent(this, UserInventory.class);
        intent.putExtra(USER_UID, userId);
        startActivity(intent);
    }

    private void renderUserAndMessages(Task<DocumentSnapshot> documentSnapshotTask) {
        User user = documentSnapshotTask.getResult().toObject(User.class);
        username.setText(user.getUsername());
        if (user.getImageUrl().equals("default")) {
            profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profile_image);
        }

        readMessages(fuser.getUid(), userId, user.getImageUrl());
    }

    private void onSendButtonClicked() {
        String msg = textToSend.getText().toString();
        if (!msg.equals("")) {
            sendMessage(fuser.getUid(), userId, msg);
        } else {
            Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
        }
        textToSend.setText("");
    }

    private void seenMessage(String userid) {
        FirebaseFirestore.getInstance().collection("Chats")
                .addSnapshotListener((querySnapshot, e) -> {
                    for (DocumentSnapshot snapshot : querySnapshot.getDocuments()) {
                        Chat chat = snapshot.toObject(Chat.class);
                        if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {
                            snapshot.getReference().update(Collections.singletonMap("isseen", true));
                        }
                    }
                });
    }

    private void readMessages(String myid, String userid, String imageUrl) {
        mchat = new TreeMap<>();
        FirebaseFirestore.getInstance().collection("Chats")
                .orderBy("timestamp")
                .addSnapshotListener((query, e) -> {
                    mchat.clear();
                    for (DocumentSnapshot snapshot : query.getDocuments()) {
                        Chat chat = snapshot.toObject(Chat.class, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE);
                        if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {
                            mchat.put(chat.getTimestamp(), chat);
                        }

                        messageAdapter = new MessageAdapter(MessageActivity.this, new ArrayList<>(mchat.values()), imageUrl);
                        recyclerView.setAdapter(messageAdapter);
                    }
                });
    }

    private void sendMessage(String sender, String receiver, String message) {
        CollectionReference chats = FirebaseFirestore.getInstance().collection("Chats");
        chats.document()
                .set(new Chat(sender, receiver, message, false));

        FirebaseFirestore.getInstance()
                .collection("Chatlist")
                .document(fuser.getUid())
                .collection("receivers")
                .document(userId)
                .set(new Chatlist(userId));

        FirebaseFirestore.getInstance()
                .collection("Chatlist")
                .document(userId)
                .collection("receivers")
                .document(fuser.getUid())
                .set(new Chatlist(fuser.getUid()));
    }
}
