package com.example.mohamed.friendlychat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mohamed.friendlychat.MessageActivity;
import com.example.mohamed.friendlychat.Model.Chat;
import com.example.mohamed.friendlychat.Model.User;
import com.example.mohamed.friendlychat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<User>mUsers;
    private boolean isChat;
    String the_last_message;

    public UserRecyclerAdapter(Context mContext , List<User>mUsers , boolean isChat){
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public UserRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_row_item,viewGroup,false);
        return new UserRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserRecyclerAdapter.ViewHolder holder, int position) {
        final User user = mUsers.get(position);
        holder.name.setText(user.getUsername());
        if (user.getImageUrl().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }else {
            Glide.with(mContext).load(user.getImageUrl()).into(holder.profile_image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,MessageActivity.class);
                intent.putExtra("userId",user.getId());
                mContext.startActivity(intent);
            }
        });

        if (isChat){
            checkLastMsg(user.getId(),holder.last_msg);
        }else {
            holder.last_msg.setVisibility(View.GONE);
        }

        if (isChat){
            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        }else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView profile_image,img_on,img_off;
        TextView name,last_msg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);
        }
    }
    // check for the last message
    public void checkLastMsg(final String userId, final TextView lastMsg){
        the_last_message = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    Chat chat = data.getValue(Chat.class);
                    assert firebaseUser != null;
                    assert userId != null;
                    if (chat.getReciever().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)
                            || chat.getReciever().equals(userId) && chat.getSender().equals(firebaseUser.getUid())){
                        the_last_message = chat.getMessage();
                    }
                }
                switch (the_last_message){
                    case "default":
                        lastMsg.setText("No message");
                        break;

                        default:
                            lastMsg.setText(the_last_message);
                            break;
                }
                the_last_message = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
