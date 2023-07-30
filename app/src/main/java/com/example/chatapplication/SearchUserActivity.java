package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.chatapplication.Utile.FirebaseUtil;
import com.example.chatapplication.adapter.SearchUserRecyclerAdapter;
import com.example.chatapplication.model.UserModel;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class SearchUserActivity extends AppCompatActivity {


    EditText searchInput;
    ImageButton searchButton;
    ImageButton backButton;
    RecyclerView recyclerView;

    SearchUserRecyclerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);


        searchInput = findViewById(R.id.seach_username_input);
        searchButton = findViewById(R.id.search_user_btn);
        backButton = findViewById(R.id.back_btn);
        recyclerView = findViewById(R.id.search_user_recycler_view);


        backButton.setOnClickListener(v->{
            onBackPressed();
        });

        searchInput.requestFocus();


        searchButton.setOnClickListener(v->{
            String searchTrim = searchInput.getText().toString().trim();
            if (searchTrim.isEmpty() || searchTrim.length()<3){
                searchInput.setError("Invalid user");
                return;

            }
            setupSearchRecyclerView(searchTrim);

        });

    }

    private void setupSearchRecyclerView(String searchTrim) {

        Query query = FirebaseUtil.allUserCollectionReference()
                .whereGreaterThanOrEqualTo("username",searchTrim)
                .whereLessThanOrEqualTo("username",searchTrim+'\uf8ff');



        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>().setQuery(query,UserModel.class).build();

        adapter = new SearchUserRecyclerAdapter(options,getApplicationContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.startListening();



    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter!= null){
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter!= null){
            adapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter!= null){
            adapter.notifyDataSetChanged();
        }
    }
}