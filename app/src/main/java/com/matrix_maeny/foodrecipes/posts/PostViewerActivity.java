package com.matrix_maeny.foodrecipes.posts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.matrix_maeny.foodrecipes.R;
import com.matrix_maeny.foodrecipes.UserModel;
import com.matrix_maeny.foodrecipes.databinding.ActivityPostViewerBinding;
import com.matrix_maeny.foodrecipes.fragments.profile.ProfileActivity;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Objects;

public class PostViewerActivity extends AppCompatActivity {

    private ActivityPostViewerBinding binding;


    public static PostModel model = null;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.pvToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(getString(R.string.app_name));

        FirebaseApp.initializeApp(PostViewerActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());

        database = FirebaseDatabase.getInstance();

        if (model != null) {
            setPostContent();
        }

        binding.pvUserIv.setOnClickListener(v -> {
            ProfileActivity.profileUserId = model.getUserUid();
            startActivity(new Intent(PostViewerActivity.this,ProfileActivity.class));
        });


    }


    private void setPostContent() {
        setUserDetails();

        setPostDetails();

        setRecipeContent();


    }

    private void setRecipeContent() {
        String headings = "<u><b>" + getString(R.string.required_ingredients) + "</b></u>";
        binding.pvRIHeadingTv.setText(Html.fromHtml(headings));

        headings = "<u><b>" + getString(R.string.procedure_to_be_followed) + "</b></u>";
        binding.pvPHeadingTv.setText(Html.fromHtml(headings));

        headings = "<u><b>" + getString(R.string.additional_instructions_pv) + "</b></u>";
        binding.pvAdInsHeadingTv.setText(Html.fromHtml(headings));

        binding.pvIngredientsTv.setText(model.getIngredients());
        binding.pvProcedureTv.setText(model.getProcedure());

        if (model.getAdditionalIns().equals("")) {
            binding.addInsLayout2.setVisibility(View.GONE);
        } else {
            binding.pvAddInsTv.setText(model.getAdditionalIns());
        }


    }

    private void setPostDetails() {
        Picasso.get().load(model.getImageUrl()).into(binding.pvPostIv);
        binding.pvPostTitleTv.setText(model.getTitle());
        binding.pvPostTagTv.setText(model.getTagLine());
    }

    private void setUserDetails() {
        setProfilePic();

        binding.pvUsernameTv.setText(model.getUsername());

    }

//    private void setPostedDate() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(model.getTime());
//
//        String timeTxt = "Shared on " + calendar.get(Calendar.DAY_OF_MONTH) + "/" + calendar.get(calendar.get(Calendar.MONTH))
//                + "/" + calendar.get(Calendar.YEAR);
//
//        binding.pvDateTv.setText(timeTxt);
//    }

    private void setProfilePic() {
        database.getReference().child("Users").child(Objects.requireNonNull(model.getUserUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel model = snapshot.getValue(UserModel.class);
                        if (model != null) {
                            try {

                                Picasso.get().load(model.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(binding.pvUserIv);

                            } catch (Exception e) {
                                e.printStackTrace();
                                binding.pvUserIv.setImageResource(R.drawable.profile_pic);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseApp.initializeApp(PostViewerActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
    }

}