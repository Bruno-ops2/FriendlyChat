package com.example.mohamed.friendlychat.Fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.mohamed.friendlychat.Adapter.UserRecyclerAdapter;
import com.example.mohamed.friendlychat.Model.User;
import com.example.mohamed.friendlychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    RecyclerView recyclerview_users;
    UserRecyclerAdapter adapter ;
    List<User>mUsers;
    EditText search_users;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerview_users = view.findViewById(R.id.recyclerview_users);
        recyclerview_users.setHasFixedSize(true);
        recyclerview_users.setLayoutManager(new LinearLayoutManager(getContext()));
        search_users = view.findViewById(R.id.search_users);
        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mUsers = new ArrayList<>();
        readUsers();

        return view;
    }

    private void searchUsers(String s) {
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query =FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(s).endAt(s+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    User user = data.getValue(User.class);
                    if (!user.getId().equals(fUser.getUid())){
                        mUsers.add(user);
                    }
                }

                adapter = new UserRecyclerAdapter(getContext(),mUsers,false);
                recyclerview_users.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void readUsers(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (search_users.getText().toString().equals("")){
                   mUsers.clear();
                   for (DataSnapshot data : dataSnapshot.getChildren()){
                       User user = data.getValue(User.class);
                       assert user != null;
                       assert firebaseUser != null;
                       if (!user.getId().equals(firebaseUser.getUid())){
                           mUsers.add(user);
                       }
                   }
                   adapter = new UserRecyclerAdapter(getContext(),mUsers,false);
                   recyclerview_users.setAdapter(adapter);
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
