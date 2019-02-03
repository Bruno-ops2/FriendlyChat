package com.example.mohamed.friendlychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mohamed.friendlychat.Adapter.MessageAdapter;
import com.example.mohamed.friendlychat.Model.Chat;
import com.example.mohamed.friendlychat.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView userName;
    FirebaseUser user;
    DatabaseReference reference;
    MessageAdapter adapter;
    List<Chat>mChats;
    RecyclerView recyclerView;
    EditText text_send;
    ImageButton btn_send;
    ValueEventListener seenListener;
//    ApiService apiService;
//    boolean notify =false;

    String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        userId= getIntent().getStringExtra("userId");
        user = FirebaseAuth.getInstance().getCurrentUser();

//        apiService = Client.getClient("https://fcm.googleapis.com/").create(ApiService.class);

        recyclerView = findViewById(R.id.recyclerview_message);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                notify = true;
                String message = text_send.getText().toString();
                if (!message.equals("")){
                    sendMessage(user.getUid(),userId,message);
                }else {
                    Toast.makeText(MessageActivity.this, "you can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        userName = findViewById(R.id.userName);
        profile_image = findViewById(R.id.profile_image);

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                userName.setText(user.getUsername());
                if (user.getImageUrl().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }else {
                    Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profile_image);
                }
                readMessage(user.getId(),userId,user.getImageUrl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userId);

    }

    private void seenMessage(final String userId){
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Chat chat = data.getValue(Chat.class);
                    if (chat.getReciever().equals(user.getUid()) && chat.getSender().equals(userId)) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("isseen", true);
                        data.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readMessage(final String myid,final String userid,final String imageurl){
        mChats = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChats.clear();
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Chat chat = data.getValue(Chat.class);
                    if (chat.getReciever().equals(myid) && chat.getSender().equals(userid)
                            || chat.getReciever().equals(userid) || chat.getSender().equals(myid)){
                        mChats.add(chat);
                    }
                    adapter = new MessageAdapter(MessageActivity.this,mChats,imageurl);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage(String sender,final String reciever,String message){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object>hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("reciever",reciever);
        hashMap.put("message",message);
        hashMap.put("isseen",false);
        databaseReference.child("Chats").push().setValue(hashMap);

        // add user to Chat fragment

        final DatabaseReference chatRefrence = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(user.getUid())
//                .child(userId);
        .child(reciever);
        chatRefrence.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRefrence.child("id").setValue(reciever);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
//
//        final String msg = message;
//        reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                User user = dataSnapshot.getValue(User.class);
//                if (notify) {
//                    sendNotifications(reciever, user.getUsername(), msg);
//                }
//                notify = false;
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

    }

//    private void sendNotifications(final String reciever, final String username, final String msg) {
//        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
//        Query query = tokens.orderByKey().equalTo(reciever);
//        query.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
//                    Token token = dataSnapshot1.getValue(Token.class);
//                    Data data = new Data(user.getUid(),R.mipmap.ic_launcher,username+": "+msg,"New Message",userId);
//                    Sender sender = new Sender(data,token.getToken());
//                    apiService.sendNotification(sender)
//                            .enqueue(new Callback<MyResponse>() {
//                                @Override
//                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
//                                   if (response.code() == 200){
//                                       if (response.body().success != 1){
//                                           Toast.makeText(MessageActivity.this, "Failed", Toast.LENGTH_SHORT).show();
//                                       }
//                                   }
//                                }
//
//                                @Override
//                                public void onFailure(Call<MyResponse> call, Throwable t) {
//
//                                }
//                            });
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

//    private void currentUser(String userId){
//        SharedPreferences.Editor editor = getSharedPreferences("PREFs",MODE_PRIVATE).edit();
//        editor.putString("currentuser",userId);
//        editor.apply();
//    }

    public void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());
        HashMap<String,Object>map=new HashMap<>();
        map.put("status",status);
        reference.updateChildren(map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
//        currentUser(userId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
//        currentUser("none");
    }
}
