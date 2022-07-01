package com.matrix_maeny.foodrecipes.fragments.profile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.foodrecipes.AboutActivity;
import com.matrix_maeny.foodrecipes.R;
import com.matrix_maeny.foodrecipes.UserModel;
import com.matrix_maeny.foodrecipes.databinding.FragmentProfileBinding;
import com.matrix_maeny.foodrecipes.fragments.search.SearchAdapter;
import com.matrix_maeny.foodrecipes.fragments.search.SearchFragment;
import com.matrix_maeny.foodrecipes.posts.PostActivity;
import com.matrix_maeny.foodrecipes.posts.PostModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;


public class ProfileFragment extends Fragment {


    private FragmentProfileBinding binding;

    private ProfileFragmentListener listener;
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    private ProgressDialog progressDialog;
    private ArrayList<PostModel> list;
    private SearchAdapter adapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        try {
            listener = (ProfileFragmentListener) requireContext();
            listener.hideToolbar(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        initialize();

        binding.prSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchUserData();
            }
        });

        binding.prAboutTv.setOnClickListener(v -> startActivity(new Intent(requireContext().getApplicationContext(), AboutActivity.class)));


        binding.prNewPostIv.setOnClickListener(v -> requireContext().startActivity(new Intent(requireContext().getApplicationContext(), PostActivity.class)));

        binding.prMenuIv.setOnClickListener(v -> listener.showLogoutFragment());

        binding.prEditProfileCv.setOnClickListener(v -> requireContext().startActivity(new Intent(requireContext().getApplicationContext(), EditProfileActivity.class)));
        return binding.getRoot();
    }

    private void initialize() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        list = new ArrayList<>();
        adapter = new SearchAdapter(requireContext(), list);

        binding.prRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.prRecyclerView.setAdapter(adapter);


        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Fetching details");
        progressDialog.setMessage("wait few seconds");

        fetchUserData();
    }

    private void fetchUserData() {
        progressDialog.show();
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid()))
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

    private void fetchPosts() {
        database.getReference().child("Posts").child(Objects.requireNonNull(auth.getUid()))
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

        binding.prUserIv.setOnClickListener(v -> {
            ProfilePicViewerActivity.profilePicUrl = model.getProfilePicUrl();
            startActivity(new Intent(requireContext().getApplicationContext(), ProfilePicViewerActivity.class));

        });
    }

    public interface ProfileFragmentListener {

        void hideToolbar(boolean shouldHide);

        void showLogoutFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchUserData();

    }
}