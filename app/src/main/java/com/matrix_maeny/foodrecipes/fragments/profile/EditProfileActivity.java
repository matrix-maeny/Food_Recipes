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
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.matrix_maeny.foodrecipes.R;
import com.matrix_maeny.foodrecipes.UserModel;
import com.matrix_maeny.foodrecipes.databinding.ActivityEditProfileBinding;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;

    private Uri imageUri = null;
    private String username, about;

    private ProgressDialog progressDialog;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.editProfileToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Edit Profile");
        initialize();


    }


    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Intent data = result.getData();

                if (data != null) {
                    imageUri = data.getData(); // assigning the uri of selected image
                    binding.peUserIv.setImageURI(imageUri);
                } else {
                    imageUri = null; // else make it null
                }
                dismissProgressDialog(); // dismissing the dialog

            });

    View.OnClickListener peEditProfileCvListener = v -> chooseProfilePic();

    @SuppressLint("SetTextI18n")
    private void initialize() {
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.peAboutEt.setText("No about provided");

        progressDialog = new ProgressDialog(EditProfileActivity.this);

        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        binding.peEditProfileCv.setOnClickListener(peEditProfileCvListener);

        fetchUserData();
    }

    private void fetchUserData() {
       showProgressDialog("Loading...","Fetching details...");
        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel model = snapshot.getValue(UserModel.class);
                        if (model != null) {
                            setUserData(model);
//                            fetchPosts();
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
            Picasso.get().load(model.getProfilePicUrl()).placeholder(R.drawable.profile_pic).into(binding.peUserIv);
        } catch (Exception e) {
            e.printStackTrace();
            binding.peUserIv.setImageResource(R.drawable.profile_pic);
        }

        binding.peUsernameEt.setText(model.getUsername());
        if (!model.getAbout().equals("")) {
            binding.peAboutEt.setText(model.getAbout());
        } else {
            binding.peAboutEt.setText("No about provided");
        }

        dismissProgressDialog();

//        binding.prNoOfPostsTv.setText(model.getPostCount() + " Posts");
    }

    private void saveProfileData() {
        if (checkUsername() && checkAbout()) {
            showProgressDialog("Saving", "wait few seconds");

            if (imageUri != null) {

                final StorageReference storageReference = storage.getReference().child(getString(R.string.firebase_users)).child(Objects.requireNonNull(auth.getUid()));

                storageReference.putFile(imageUri).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            if (task.isSuccessful()) {

                                database.getReference().child(getString(R.string.firebase_users))
                                        .child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                UserModel model = snapshot.getValue(UserModel.class);
                                                if (model != null) {
                                                    model.setUsername(username);
                                                    model.setAbout(about);
                                                    model.setProfilePicUrl(uri.toString());

                                                    database.getReference().child(getString(R.string.firebase_users))
                                                            .child(auth.getUid())
                                                            .setValue(model).addOnCompleteListener(task1 -> {

                                                                if (task1.isSuccessful()) {
                                                                    Toast.makeText(EditProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                                                                    dismissProgressDialog();
                                                                    finish();

                                                                } else
                                                                    Toast.makeText(EditProfileActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                                dismissProgressDialog();

                                                            }).addOnFailureListener(e -> {
                                                                Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                dismissProgressDialog();
                                                            });
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dismissProgressDialog();

                                            }
                                        });

                            } else
                                Toast.makeText(EditProfileActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            dismissProgressDialog();

                        }).addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

                    } else
                        Toast.makeText(EditProfileActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                    dismissProgressDialog();
                }).addOnFailureListener(e -> {
                    Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    dismissProgressDialog();

                });
            } else {
                database.getReference().child(getString(R.string.firebase_users))
                        .child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                UserModel model = snapshot.getValue(UserModel.class);
                                if (model != null) {
                                    model.setUsername(username);
                                    model.setAbout(about);

                                    database.getReference().child(getString(R.string.firebase_users))
                                            .child(auth.getUid())
                                            .setValue(model).addOnCompleteListener(task1 -> {

                                                if (task1.isSuccessful()) {
                                                    Toast.makeText(EditProfileActivity.this, "Profile saved", Toast.LENGTH_SHORT).show();
                                                    dismissProgressDialog();
                                                    finish();

                                                } else
                                                    Toast.makeText(EditProfileActivity.this, Objects.requireNonNull(task1.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                                dismissProgressDialog();

                                            }).addOnFailureListener(e -> {
                                                Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                dismissProgressDialog();
                                            });
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                dismissProgressDialog();

                            }
                        });

            }
        }
    }


    public void chooseProfilePic() {

        showProgressDialog("Getting Image", "Wait few seconds"); // showing a waiting dialog

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        launcher.launch(Intent.createChooser(intent, "Select a picture"));
    }


    private boolean checkUsername() {
        try {
            username = Objects.requireNonNull(binding.peUsernameEt.getText()).toString();
            if (!username.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
        return false;
    }

    private boolean checkAbout() {
        try {
            about = Objects.requireNonNull(binding.peAboutEt.getText()).toString();
            if (!about.equals("")) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Please enter About", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        saveProfileData();
        return super.onOptionsItemSelected(item);
    }


    private void showProgressDialog(String title, String msg) {
        progressDialog.setTitle(title);
        progressDialog.setMessage(msg);
        try {
            progressDialog.show(); // inCase of illegalState exception
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dismissProgressDialog() {
        try {
            progressDialog.dismiss(); // inCase of illegalState exception
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}