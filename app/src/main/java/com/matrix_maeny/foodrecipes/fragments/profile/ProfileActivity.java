package com.matrix_maeny.foodrecipes.fragments.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.foodrecipes.R;
import com.matrix_maeny.foodrecipes.UserModel;
import com.matrix_maeny.foodrecipes.comments.CommentModel;
import com.matrix_maeny.foodrecipes.databinding.ActivityProfileBinding;
import com.matrix_maeny.foodrecipes.databinding.FragmentProfileBinding;
import com.matrix_maeny.foodrecipes.fragments.search.SearchAdapter;
import com.matrix_maeny.foodrecipes.posts.PostModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding  binding;

    private FirebaseDatabase database;

    private ProgressDialog progressDialog;
    private ArrayList<PostModel> list;
    private SearchAdapter adapter;

    public static  String profileUserId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.prNToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");

        initialize();
    }

    private void initialize() {
        database = FirebaseDatabase.getInstance();

        list = new ArrayList<>();
        adapter = new SearchAdapter(ProfileActivity.this, list);

        binding.prRecyclerView.setLayoutManager(new GridLayoutManager(ProfileActivity.this, 2));
        binding.prRecyclerView.setAdapter(adapter);


        progressDialog = new ProgressDialog(ProfileActivity.this);
        progressDialog.setTitle("Fetching details");
        progressDialog.setMessage("wait few seconds");


        fetchUserData();
    }

    private void fetchUserData() {
        progressDialog.show();
        database.getReference().child("Users").child(Objects.requireNonNull(profileUserId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel model = snapshot.getValue(UserModel.class);
                        if (model != null) {
                            setUserData(model);
                            fetchPosts();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void setUserData(UserModel model) {
        try {
            Picasso.get().load(model.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(binding.prUserIv);
        } catch (Exception e) {
            e.printStackTrace();
            binding.prUserIv.setImageResource(R.drawable.profile_pic);
        }

        binding.prUsernameTv.setText(model.getUsername());
        if (!model.getAbout().equals("")) {
            binding.prUserAboutTv.setText(model.getAbout());
        } else {
            binding.prUserAboutTv.setText("No about provided");
        }

        binding.prNoOfPostsTv.setText(model.getPostCount() + " Posts");


    }

    private void fetchPosts() {
        database.getReference().child("Posts").child(Objects.requireNonNull(profileUserId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        binding.prSwipeRefreshLayout.setRefreshing(false);
                        list.clear();
                        if (snapshot.exists()) {
                            binding.lgEmptyTv.setVisibility(View.INVISIBLE);

                            for (DataSnapshot s : snapshot.getChildren()) {
                                PostModel model = s.getValue(PostModel.class);
                                if (model != null) {
                                    list.add(model);
                                }
                            }

                            refreshAdapter();

                        } else {
                            binding.lgEmptyTv.setVisibility(View.VISIBLE);
                        }
                        try {
                            progressDialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshAdapter() {
        new Handler().post(() -> adapter.notifyDataSetChanged());
    }


}