package com.matrix_maeny.foodrecipes.fragments.profile;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.matrix_maeny.foodrecipes.R;
import com.matrix_maeny.foodrecipes.databinding.ActivityProfilePicViewerBinding;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ProfilePicViewerActivity extends AppCompatActivity {

    private ActivityProfilePicViewerBinding binding;

    private Uri imageUri = null;
    public static String profilePicUrl = "";

    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfilePicViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.ppvToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile Pic");

        initialize();
    }

    private void initialize() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();


        progressDialog = new ProgressDialog(ProfilePicViewerActivity.this);

        try {
            Picasso.get().load(profilePicUrl).placeholder(R.drawable.profile_pic).into(binding.ppvUserProfilePicIv);
        } catch (Exception e) {
            e.printStackTrace();
            chooseProfilePic();
        }
    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();

                if (data != null) {
                    imageUri = data.getData(); // assigning the uri of selected image
                    binding.ppvUserProfilePicIv.setImageURI(imageUri);
//                    saveProfilePic();
                } else {
                    imageUri = null; // else make it null
                }
                dismissProgressDialog(); // dismissing the dialog
            });

    private void showProgressDialog(String title, String msg) {
        progressDialog.setTitle(title);
        progressDialog.setMessage(msg);
        try {
            progressDialog.show(); // inCase of illegalState exception
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chooseProfilePic() {

        showProgressDialog("Getting Image", "Wait few seconds"); // showing a waiting dialog

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        launcher.launch(Intent.createChooser(intent, "Select a picture"));
    }

    private void dismissProgressDialog() {
        try {
            progressDialog.dismiss(); // inCase of illegalState exception
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void saveProfilePic() {
        showProgressDialog("Saving", "wait few seconds");


        final StorageReference storageReference = storage.getReference().child(getString(R.string.firebase_users)).child(Objects.requireNonNull(auth.getUid()));

        storageReference.putFile(imageUri).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (task.isSuccessful()) {

                        database.getReference().child(getString(R.string.firebase_users))
                                .child(Objects.requireNonNull(auth.getUid())).child("profilePicUrl").setValue(uri.toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        dismissProgressDialog();

                                        if(task.isSuccessful()){
                                            Toast.makeText(ProfilePicViewerActivity.this, "Profile image saved", Toast.LENGTH_SHORT).show();
                                            finish();
                                        }else
                                            Toast.makeText(ProfilePicViewerActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                                    }
                                }).addOnFailureListener(e -> {
                                    dismissProgressDialog();
                                    Toast.makeText(ProfilePicViewerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                });

                    } else
                        Toast.makeText(ProfilePicViewerActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    dismissProgressDialog();

                }).addOnFailureListener(e -> {
                    dismissProgressDialog();

                    Toast.makeText(ProfilePicViewerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            } else
                Toast.makeText(ProfilePicViewerActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

            dismissProgressDialog();
        }).addOnFailureListener(e -> {
            Toast.makeText(ProfilePicViewerActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            dismissProgressDialog();

        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_pic_viewer, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // choose profile pic
        switch (item.getItemId()) {
            case R.id.ppv_edit_pic:
                chooseProfilePic();
                break;
            case R.id.ppv_save:
                saveProfilePic();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


}