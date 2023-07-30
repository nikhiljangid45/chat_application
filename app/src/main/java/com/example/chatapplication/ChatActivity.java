package com.example.chatapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chatapplication.Utile.AndroidUtil;
import com.example.chatapplication.Utile.FirebaseUtil;
import com.example.chatapplication.adapter.ChatRecyclerAdapter;
import com.example.chatapplication.model.ChatMessageModel;
import com.example.chatapplication.model.ChatroomModel;
import com.example.chatapplication.model.UserModel;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formattable;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {


    EditText messageInput;
    ImageButton sendMessageButton ,backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    UserModel otherUser;

    String chatroomId;

    ChatroomModel chatroomModel;

    ChatRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());

        sendMessageButton = findViewById(R.id.message_send_btn);
        messageInput = findViewById(R.id.chat_message_input);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView= findViewById(R.id.chat_recycler_view);


        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri uri = task.getResult();
                    AndroidUtil.setProfile(getApplicationContext(),uri,findViewById(R.id.profile_pic_image_view));
                }
            }
        });


        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(),otherUser.getUserId());


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        otherUsername.setText(otherUser.getUsername());
        sendMessageButton.setOnClickListener(v->{
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()){
                return;
            }
            sendMessageToUser(message);
            sendNotification(message);
        });

        getOrCreateChatRoomModel();
        setUpChatRecycleView();


    }
    void setUpChatRecycleView(){
    Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
            .orderBy("timestamp",Query.Direction.DESCENDING);

    FirestoreRecyclerOptions<ChatMessageModel> option = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query,ChatMessageModel.class).build();

    adapter = new ChatRecyclerAdapter(option,getApplicationContext());
    LinearLayoutManager manager = new LinearLayoutManager(this);
    manager.setReverseLayout(true);
    recyclerView.setLayoutManager(manager);
    recyclerView.setAdapter(adapter);
    adapter.startListening();
    adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            recyclerView.smoothScrollToPosition(0);
        }
    });
}
    private void sendMessageToUser(String message) {


        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessage(message);

        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message,FirebaseUtil.currentUserId(),Timestamp.now());

        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                messageInput.setText("");
            }
        });


    }

    private void getOrCreateChatRoomModel() {

        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    chatroomModel = task.getResult().toObject(ChatroomModel.class);
                       if (chatroomModel == null){
                           chatroomModel = new ChatroomModel(
                                   chatroomId,
                                   Arrays.asList(FirebaseUtil.currentUserId(),otherUser.getUserId()),
                                   Timestamp.now(),
                                   "");

                           FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                       }
                }
            }
        });

    }

    void sendNotification(String message){

       FirebaseUtil.currentUserDetail().get().addOnCompleteListener(task -> {
           if (task.isSuccessful()){
               UserModel currentUser = task.getResult().toObject(UserModel.class);

               try {
                   JSONObject jsonObject = new JSONObject();


                   JSONObject notificationObj = new JSONObject();
                   JSONObject dataObj = new JSONObject();


                   notificationObj.put("title",currentUser.getUsername());
                   notificationObj.put("body",message);

                   dataObj.put("userId",currentUser.getUserId());
                   jsonObject.put("notification",notificationObj);
                   jsonObject.put("data",dataObj);
                   jsonObject.put("to",otherUser.getFcmToken());

                   callApi(jsonObject);
               } catch (JSONException e) {
                   throw new RuntimeException(e);
               }
           }
       });

    }

    void callApi(JSONObject jsonObject){
        MediaType JSON
                = MediaType.get("application/json; charset=utf-8");

        OkHttpClient client = new OkHttpClient();

        String uri ="https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString() ,JSON);
        Request request = new Request.Builder()
                .url(uri)
                .post(body)
                .header("Authorization","Bearer AAAAmNQAmN8:APA91bEmekqdvnNnycU-sJhbbEMvLaLfbPS-LpcYl8D9iQAagySMyoytgvpcgpNft2wOm3hCt62KJb4l2LteMKOZW6aDhgNdM14OUpCNgyHm4Sf6kjKPn5SMrp1RpniA9O9Ztelb5HQ8")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });


    }
}