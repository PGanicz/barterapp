package org.studia.barterapplication.ui.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.studia.barterapplication.R;
import org.studia.barterapplication.chat.Chatlist;
import org.studia.barterapplication.chat.User;

import java.util.ArrayList;
import java.util.List;

public class MessagesFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;

    private List<Chatlist> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        recyclerView = view.findViewById(R.id.fragment_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();

        FirebaseFirestore.getInstance().collection("Chatlist")
                .document(fuser.getUid())
                .collection("receivers")
                .addSnapshotListener((query, e) -> {
                    renderChatlist(query);
                });
        return view;
    }

    private void renderChatlist(QuerySnapshot query) {
        usersList.clear();
        for (DocumentSnapshot documents : query.getDocuments()) {
            Chatlist chatlist = documents.toObject(Chatlist.class);
            usersList.add(chatlist);
        }
        chatList();
    }

    private void chatList() {
        mUsers = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Users")
                .addSnapshotListener((query, e) -> {
                    mUsers.clear();
                    for (DocumentSnapshot snapshot : query.getDocuments()) {
                        User user = snapshot.toObject(User.class);
                        for (Chatlist chatlist : usersList) {
                            if (user.getUid().equals(chatlist.getId())) {
                                mUsers.add(user);
                            }
                        }
                    }
                    userAdapter = new UserAdapter(getContext(), mUsers, true);
                    recyclerView.setAdapter(userAdapter);
                });
    }
}
