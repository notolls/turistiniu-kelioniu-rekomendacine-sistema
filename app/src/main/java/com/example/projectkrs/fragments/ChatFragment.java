package com.example.projectkrs.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.projectkrs.model.ChatMessage;
import com.example.projectkrs.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private EditText editMessage;
    private ImageView btnSend;
    private ListView listView;

    private FirebaseFirestore db;

    private List<String> messagesList = new ArrayList<>();
    private List<String> userIds = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    public ChatFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        editMessage = view.findViewById(R.id.editMessage);
        btnSend = view.findViewById(R.id.btnSend);
        listView = view.findViewById(R.id.listViewMessages);

        db = FirebaseFirestore.getInstance();

        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1,
                messagesList);

        listView.setAdapter(adapter);

        listenForMessages();

        btnSend.setOnClickListener(v -> sendMessage());

        listView.setOnItemClickListener((parent, v, position, id) -> {

            String clickedUserId = userIds.get(position);

            if (!shouldOpenUserMap(clickedUserId, FirebaseAuth.getInstance().getUid()))
                return;

            new AlertDialog.Builder(requireContext())
                    .setTitle("Pasirinkimas")
                    .setMessage("Peržiūrėti naudotojo aplankytas vietas?")
                    .setPositiveButton("Taip",
                            (dialog, which) -> openUserMap(clickedUserId))
                    .setNegativeButton("Atšaukti", null)
                    .show();
        });

        return view;
    }

    private void sendMessage() {

        String messageText = editMessage.getText().toString().trim();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (!isMessageSendAllowed(messageText, user != null)) return;

        ChatMessage message = new ChatMessage(
                messageText,
                user.getEmail(),
                user.getUid(),
                Timestamp.now()
        );

        db.collection("chats")
                .document("global")
                .collection("messages")
                .add(message);

        editMessage.setText("");
    }

    private void listenForMessages() {

        db.collection("chats")
                .document("global")
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) return;

                    messagesList.clear();
                    userIds.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {

                        String text = doc.getString("text");
                        String user = doc.getString("userEmail");
                        String userId = doc.getString("userId");

                        if (text != null && user != null && userId != null) {
                            messagesList.add(formatChatListItem(user, text));
                            userIds.add(userId);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    listView.setSelection(messagesList.size() - 1);
                });
    }


    static boolean isMessageSendAllowed(String messageText, boolean hasCurrentUser) {
        return !TextUtils.isEmpty(messageText) && hasCurrentUser;
    }

    static boolean shouldOpenUserMap(String clickedUserId, String currentUserId) {
        return clickedUserId != null && !clickedUserId.equals(currentUserId);
    }

    static String formatChatListItem(String userEmail, String text) {
        return userEmail + ": " + text;
    }

    private void openUserMap(String userId) {

        Bundle bundle = new Bundle();
        bundle.putString("view_user_id", userId);

        MapFragment fragment = new MapFragment();
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}