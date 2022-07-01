package com.matrix_maeny.foodrecipes.comments;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.foodrecipes.R;
import com.matrix_maeny.foodrecipes.databinding.ActivityCommentViewerBinding;
import com.matrix_maeny.foodrecipes.posts.PostModel;

import java.util.ArrayList;
import java.util.Objects;

public class CommentViewerActivity extends AppCompatActivity {

    private ActivityCommentViewerBinding binding;

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private ArrayList<CommentModel> list;
    private CommentAdapter adapter;

    private ProgressDialog progressDialog;

    public static PostModel model = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.commentToolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle("Comments");

        initialize();

        if (model != null) {
            fetchComments();
        }

    }

    private void initialize() {

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        list = new ArrayList<>();
        adapter = new CommentAdapter(CommentViewerActivity.this, list);

        binding.commentRecyclerView.setLayoutManager(new LinearLayoutManager(CommentViewerActivity.this));
        binding.commentRecyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(CommentViewerActivity.this);
        progressDialog.setTitle("Loading comments");
        progressDialog.setMessage("Fetching information");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);


    }

    private void fetchComments() {

        progressDialog.show();
        database.getReference().child("Comments").child(model.getUserUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot s : snapshot.getChildren()) {

                                String title = s.getKey();
                                if (title != null && title.equals(model.getTitle())) {

                                    for (DataSnapshot cm : s.getChildren()) {
                                        CommentModel commentModel = cm.getValue(CommentModel.class);
                                        if (commentModel != null) {
                                            list.add(commentModel);
                                        }
                                    }

                                    break;
                                }


                            }

                            refreshAdapter();
                        }

                        progressDialog.dismiss();
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