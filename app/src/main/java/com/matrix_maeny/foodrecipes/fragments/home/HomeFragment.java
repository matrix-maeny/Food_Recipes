package com.matrix_maeny.foodrecipes.fragments.home;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.foodrecipes.R;
import com.matrix_maeny.foodrecipes.databinding.FragmentHomeBinding;
import com.matrix_maeny.foodrecipes.posts.PostAdapter;
import com.matrix_maeny.foodrecipes.posts.PostModel;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;


public class HomeFragment extends Fragment {


    private FragmentHomeBinding binding;
    private PostAdapter adapter;
    private ArrayList<PostModel> list;
//    private String image = "https://images.immediate.co.uk/production/volatile/sites/30/2020/08/processed-food700-350-e6d0f0f.jpg?quality=90&resize=556,505";

    //    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    private ArrayList<PostModel> listDup;
    private ProgressDialog progressDialog;



    final Handler handler = new Handler();
    final Random random = new Random();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        try {
            HomeFragmentListener listener = (HomeFragmentListener) requireContext();
            listener.hideToolbar(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initialize();

        binding.swipeRefreshLayout.setOnRefreshListener(this::fetchPosts);
        return binding.getRoot();
    }

    private void initialize() {
//        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        list = new ArrayList<>();
        listDup = new ArrayList<>();

        progressDialog = new ProgressDialog(requireContext());

        adapter = new PostAdapter(requireContext(), list);


        binding.recyclerViewHome.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewHome.setAdapter(adapter);

        fetchPosts();
    }


    private void fetchPosts() {
        showProgressDialog();
        firebaseDatabase.getReference().child(getString(R.string.firebase_posts))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // we need to fetch last three days information about recipes
                            // day 1 , day 2, day 3 for each day 100
                            list.clear();
                            listDup.clear();

                            int i = 1;

                            Iterable<DataSnapshot> children = snapshot.getChildren();
                            whole:
                            for (DataSnapshot s : snapshot.getChildren()) { // for every user

                                for (DataSnapshot sc : s.getChildren()) { // for each post

                                    PostModel model = sc.getValue(PostModel.class); // get post

                                    if (model != null) { // checking
                                        listDup.add(model);
                                        i++;
                                        if (i >= 1000) {
                                            break whole;
                                        }

                                    }


                                }// second loop
                            }// first loop


                            setAdapterList();

                            dismissProgressDialog();
                            if (list.isEmpty()) {
                                binding.hmEmptyTv.setVisibility(View.VISIBLE);
                            } else binding.hmEmptyTv.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(requireContext(), "No posts are available", Toast.LENGTH_SHORT).show();
                            dismissProgressDialog();
                        }
                        refreshAdapter();

                        binding.swipeRefreshLayout.setRefreshing(false);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setAdapterList() {



        int limit = 1000;

        if (listDup.size() < limit) {
            limit = listDup.size();
        }

        for (int i = 0; i < limit; i++) {
            int rN = random.nextInt(listDup.size());
            if (!list.contains(listDup.get(rN)))
                list.add(listDup.get(rN));
        }

        for (int i = 0; i < limit; i++) {
            if (!list.contains(listDup.get(i))) list.add(listDup.get(i));

        }

        listDup.clear();

    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshAdapter() {
        handler.post(() -> adapter.notifyDataSetChanged());
    }

    private void showProgressDialog() {
        progressDialog.setTitle("Loading posts");
        progressDialog.setMessage("wait few seconds");

        handler.post(() -> {
            try {
                progressDialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void dismissProgressDialog() {
        handler.post(() -> {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchPosts();
    }


    public interface HomeFragmentListener {
        void hideToolbar(boolean shouldHide);
    }
}