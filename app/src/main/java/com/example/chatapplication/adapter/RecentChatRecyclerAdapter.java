package com.example.chatapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.ChatActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.Utile.AndroidUtil;
import com.example.chatapplication.Utile.FirebaseUtil;
import com.example.chatapplication.model.ChatroomModel;
import com.example.chatapplication.model.UserModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;

import kotlin.time.TimeSource;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel,RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options,Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull RecentChatRecyclerAdapter.ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {

        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {

                            UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                            FirebaseUtil.getOtherProfilePicStorageRef(otherUserModel.getUserId()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()){
                                        Uri uri = task.getResult();
                                        AndroidUtil.setProfile(context,uri,holder.profilePic);
                                    }
                                }
                            });

                            holder.usernameText.setText(otherUserModel.getUsername());
                            if (model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId())){
                                holder.lastMessageText.setText("(you) "+model.getLastMessage());
                            }else {
                                holder.lastMessageText.setText(model.getLastMessage());

                            }
                            holder.lastMessageTime.setText(new SimpleDateFormat("HH:MM").format(model.getLastMessageTimestamp().toDate()));

                            holder.itemView.setOnClickListener(v->{
                                Intent intent = new Intent(context, ChatActivity.class);
                                AndroidUtil.passUserModelAsIntent(intent,otherUserModel);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            });
                        }
                    }
                });



    }

    @NonNull
    @Override
    public RecentChatRecyclerAdapter.ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row,parent,false);
        return new ChatroomModelViewHolder(view);
    }

    public class ChatroomModelViewHolder extends RecyclerView.ViewHolder {

        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}
