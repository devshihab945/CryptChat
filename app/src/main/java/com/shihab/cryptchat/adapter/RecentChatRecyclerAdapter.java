package com.shihab.cryptchat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shihab.cryptchat.ChatActivity;
import com.shihab.cryptchat.Auth.EncryptDecrypt;
import com.shihab.cryptchat.R;
import com.shihab.cryptchat.model.ChatroomModel;
import com.shihab.cryptchat.model.UserModel;
import com.shihab.cryptchat.utils.AndroidUtil;
import com.shihab.cryptchat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.Objects;

public class RecentChatRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    Context context;

    public RecentChatRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options,Context context) {
        super(options);
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get().addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtil.currentUserId());


                            UserModel otherUserModel = task.getResult().toObject(UserModel.class);

                            FirebaseUtil.getOtherProfilePicStorageRef(Objects.requireNonNull(otherUserModel).getUserId()).getDownloadUrl()
                                    .addOnCompleteListener(t -> {
                                        if(t.isSuccessful()){
                                            Uri uri  = t.getResult();
                                            AndroidUtil.setProfilePic(context,uri,holder.profilePic);
                                        }
                                    });

                            holder.usernameText.setText(otherUserModel.getUsername());

                            String encryptedMsg = model.getLastMessage();
                            Log.d("TAG", "onBindViewHolder: "+encryptedMsg);

                            if(lastMessageSentByMe){
                                // Decrypt the message
                                try {
                                    String decryptedMsg = EncryptDecrypt.decryptRequest(encryptedMsg);
                                    holder.lastMessageText.setText("You : "+decryptedMsg);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }

                            }
                            else{
                                // Decrypt the message
                                try {
                                    String decryptedMsg = EncryptDecrypt.decryptRequest(encryptedMsg);
                                    holder.lastMessageText.setText(decryptedMsg);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            // Set the last message time
                            holder.lastMessageTime.setText(FirebaseUtil.timestampToString(model.getLastMessageTimestamp()));

                            holder.itemView.setOnClickListener(v -> {
                                //navigate to chat activity
                                Intent intent = new Intent(context, ChatActivity.class);
                                AndroidUtil.passUserModelAsIntent(intent,otherUserModel);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                            });

                        }
                });
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row,parent,false);
        return new ChatroomModelViewHolder(view);
    }

    static class ChatroomModelViewHolder extends RecyclerView.ViewHolder{
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
