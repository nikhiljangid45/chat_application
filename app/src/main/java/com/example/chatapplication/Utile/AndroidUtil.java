package com.example.chatapplication.Utile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chatapplication.model.UserModel;

public class AndroidUtil {
    public static void showToast(Context context,String message){
        Toast.makeText(context, " "+message, Toast.LENGTH_SHORT).show();
    }


    public static void passUserModelAsIntent(Intent intent , UserModel userModel){
        intent.putExtra("username",userModel.getUsername());
        intent.putExtra("phone",userModel.getPhone());
        intent.putExtra("userId",userModel.getUserId());
        intent.putExtra("fcmToken",userModel.getFcmToken());
    }

    public static UserModel getUserModelFromIntent(Intent intent){
        UserModel userModel = new UserModel();
        userModel.setUsername(intent.getStringExtra("username"));
        userModel.setPhone(intent.getStringExtra("phone"));
        userModel.setUserId(intent.getStringExtra("userId"));
        userModel.setFcmToken(intent.getStringExtra("fcmToken"));
        return userModel;
    }

    public static void  setProfile(Context context, Uri imageUri, ImageView imageView){

        Glide.with(context).load(imageUri).apply(RequestOptions.circleCropTransform()).into(imageView);
    }

}
