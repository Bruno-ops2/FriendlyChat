package com.example.mohamed.friendlychat.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mohamed.friendlychat.Adapter.UserRecyclerAdapter;
import com.example.mohamed.friendlychat.Model.ChatList;
import com.example.mohamed.friendlychat.Model.User;
import com.example.mohamed.friendlychat.Notifications.Token;
import com.example.mohamed.friendlychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    RecyclerView recyclerview_chats;
    private UserRecyclerAdapter adapter;
    private List<User>users;
    FirebaseUser fUser;
    DatabaseReference reference;
    private List<ChatList>userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerview_chats = view.findViewById(R.id.recyclerview_chats);
        recyclerview_chats.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerview_chats.setLayoutManager(linearLayoutManager);

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        userList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(fUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    ChatList chatList = data.getValue(ChatList.class);
                    userList.add(chatList);
                }
                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

//        updateToken(FirebaseInstanceId.getInstance().getToken());

        return view;
    }

//    private void updateToken(String token){
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
//        Token t = new Token(token);
//        reference.child(fUser.getUid()).setValue(t);
//    }

    private void chatList() {
        users = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    User user = data.getValue(User.class);
                    for (ChatList chatList : userList){
                        if (user.getId().equals(chatList.getId())){
                            users.add(user);
                        }
                    }
                }
                UserRecyclerAdapter adapter = new UserRecyclerAdapter(getContext(),users,true);
                recyclerview_chats.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
