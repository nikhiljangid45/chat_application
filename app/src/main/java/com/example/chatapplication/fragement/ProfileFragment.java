package com.example.chatapplication.fragement;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chatapplication.MainActivity;
import com.example.chatapplication.R;
import com.example.chatapplication.SplashActivity;
import com.example.chatapplication.Utile.AndroidUtil;
import com.example.chatapplication.Utile.FirebaseUtil;
import com.example.chatapplication.model.UserModel;


import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.UploadTask;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class ProfileFragment extends Fragment {
    public ProfileFragment() {
        // Required empty public constructor
    }

    ImageView profilePic;
    EditText usernameInput;
    EditText phoneInput;
    Button updateProfileBtn;
    ProgressBar progressBar;
    TextView logoutBtn;

    UserModel currentUserModel;
    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectedImageUri;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                     if (result.getResultCode() == Activity.RESULT_OK){
                         Intent data = result.getData();
                         if (data != null && data.getData() != null){
                             selectedImageUri = data.getData();
                             AndroidUtil.setProfile(getContext(),selectedImageUri,profilePic);
                         }
                     }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        profilePic = view.findViewById(R.id.profile_image_view);
        usernameInput = view.findViewById(R.id.profile_username);
        phoneInput = view.findViewById(R.id.profile_phone);
        updateProfileBtn = view.findViewById(R.id.profle_update_btn);
        progressBar = view.findViewById(R.id.profile_progress_bar);
        logoutBtn = view.findViewById(R.id.logout_btn);




        getUserData();

        updateProfileBtn.setOnClickListener((v -> {

            if (selectedImageUri != null){
                FirebaseUtil.getCurrentProfilePicStorageRef().putFile(selectedImageUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                updateBtnClick();

                            }
                        });
            }else {
                updateBtnClick();

            }


        }));
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getContext(), SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });

            }
        });



        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.with(ProfileFragment.this).cropSquare().compress(512).maxResultSize(512,512)
                        .createIntent(new Function1<Intent, Unit>() {
                            @Override
                            public Unit invoke(Intent intent) {
                                 imagePickLauncher.launch(intent);
                                return null;
                            }
                        });
            }
        });

        return view;

    }

    private void updateBtnClick() {

        String newUsername = usernameInput.getText().toString();
        if(newUsername.isEmpty() || newUsername.length()<3){
            usernameInput.setError("Username length should be at least 3 chars");
            return;
        }

        currentUserModel.setUsername(newUsername);
        setInProgress(true);
        updateToFirestore();
    }

    void updateToFirestore(){
        FirebaseUtil.currentUserDetail().set(currentUserModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        setInProgress(false);
                        AndroidUtil.showToast(getContext(),"Update Successfully");
                    }else {
                        AndroidUtil.showToast(getContext(),"Update Successfully");

                    }
                });
    }
    private void getUserData() {
        setInProgress(true);
        FirebaseUtil.getCurrentProfilePicStorageRef().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()){
                    Uri uri = task.getResult();
                    AndroidUtil.setProfile(getContext(),uri,profilePic);
                }
            }
        });
        FirebaseUtil.currentUserDetail().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    currentUserModel = task.getResult().toObject(UserModel.class);
                    setInProgress(false);
                    usernameInput.setText(currentUserModel.getUsername());
                    phoneInput.setText(currentUserModel.getPhone());
                }
            }
        });
        
    }

    void  setInProgress(boolean inProgress){
        if (inProgress){
            progressBar.setVisibility(View.VISIBLE);
            updateProfileBtn.setVisibility(View.INVISIBLE);
        }else {
            progressBar.setVisibility(View.INVISIBLE);
            updateProfileBtn.setVisibility(View.VISIBLE);
        }
    }
}