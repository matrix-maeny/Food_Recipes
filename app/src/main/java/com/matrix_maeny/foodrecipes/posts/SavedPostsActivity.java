package com.matrix_maeny.foodrecipes.posts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Binder;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.foodrecipes.databinding.ActivitySavedPostsBinding;
import com.matrix_maeny.foodrecipes.fragments.search.SearchAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class SavedPostsActivity extends AppCompatActivity {

    private ActivitySavedPostsBinding binding;

    private SearchAdapter adapter;
    private ArrayList<PostModel> list;

    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private ProgressDialog progressDialog;

//    final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.spToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Saved Recipes");

        FirebaseApp.initializeApp(SavedPostsActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        initialize();
    }

    private void initialize() {

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(SavedPostsActivity.this);

        list = new ArrayList<>();
        adapter = new SearchAdapter(SavedPostsActivity.this, list);

        binding.spRecyclerView.setLayoutManager(new GridLayoutManager(SavedPostsActivity.this, 2));

        binding.spRecyclerView.setAdapter(adapter);

        fetchPosts();
    }

    private void fetchPosts() {
        showProgressDialog();
        database.getReference().child("Saves").child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // retrieve posts of saved space

                            for (DataSnapshot s : snapshot.getChildren()) {

                                for (DataSnapshot pm : s.getChildren()) {
                                    PostModel model = pm.getValue(PostModel.class);
                                    if (model != null)
                                        list.add(model);
                                }
                            }

                        } else {
                            Toast.makeText(SavedPostsActivity.this, "No posts are available", Toast.LENGTH_SHORT).show();
                        }

                        if(list.isEmpty()){
                            binding.spEmptyTv.setVisibility(View.VISIBLE);
                        }else{
                            binding.spEmptyTv.setVisibility(View.GONE);
                        }
                        refreshAdapter();
                        dismissProgressDialog();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @SuppressLint("NotifyDataSetChanged")
    private void refreshAdapter() {
        adapter.notifyDataSetChanged();
    }


    private void showProgressDialog() {
        progressDialog.setTitle("Loading posts");
        progressDialog.setMessage("wait few seconds");

        try {
            progressDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissProgressDialog() {
        try {
            progressDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseApp.initializeApp(SavedPostsActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
    }
}