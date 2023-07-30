package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.chatapplication.LogInActivity.LoginPhoneNumberActivity;
import com.example.chatapplication.Utile.AndroidUtil;
import com.example.chatapplication.Utile.FirebaseUtil;
import com.example.chatapplication.model.UserModel;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (FirebaseUtil.isLoggedIn() && getIntent().getExtras() != null){

            String userId = getIntent().getExtras().getString("userId");
            if (userId == null){
                nikhil();
                return;
            }

            FirebaseUtil.allUserCollectionReference().document(userId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    UserModel userModel = task.getResult().toObject(UserModel.class);


                    Intent mainIntent = new Intent(this, MainActivity.class);

                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(mainIntent);


                    Intent intent = new Intent(this, ChatActivity.class);
                    AndroidUtil.passUserModelAsIntent(intent,userModel);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                 finish();
                }
            });
        }else {
nikhil();
        }

    }
    void  nikhil(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (FirebaseUtil.isLoggedIn()){
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                           finish();
                }else {
                    startActivity(new Intent(SplashActivity.this, LoginPhoneNumberActivity.class));
                    finish();

                }

            }
        },1000);

    }
}