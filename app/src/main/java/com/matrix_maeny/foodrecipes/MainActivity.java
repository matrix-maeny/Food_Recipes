package com.matrix_maeny.foodrecipes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.FirebaseAppCheckRegistrar;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.matrix_maeny.foodrecipes.databinding.ActivityMainBinding;
import com.matrix_maeny.foodrecipes.fragments.home.HomeFragment;
import com.matrix_maeny.foodrecipes.fragments.profile.LogoutFragment;
import com.matrix_maeny.foodrecipes.fragments.profile.ProfileFragment;
import com.matrix_maeny.foodrecipes.fragments.search.SearchFragment;
import com.matrix_maeny.foodrecipes.posts.PostActivity;
import com.matrix_maeny.foodrecipes.posts.PostAdapter;
import com.matrix_maeny.foodrecipes.posts.PostModel;
import com.matrix_maeny.foodrecipes.posts.SavedPostsActivity;
import com.matrix_maeny.foodrecipes.registerActivities.LoginActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PostAdapter.PostAdapterListener, SearchFragment.SearchFragmentListener
        , HomeFragment.HomeFragmentListener, LogoutFragment.LogoutFragmentListener, ProfileFragment.ProfileFragmentListener {

    //    public static String currentUsername = null;

    //    private FirebaseAuth firebaseAuth;
//    private FirebaseDatabase firebaseDatabase;
//    Handler?
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        getCurrentUserData();
        FirebaseApp.initializeApp(MainActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());


        setSupportActionBar(binding.toolbar);
//        BottomNavigationView navView = findViewById(R.id.nav_view);
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_search, R.id.navigation_notifications,
//                R.id.navigation_profile)
//                .build();
//
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

//        binding.navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
//            @SuppressLint("NonConstantResourceId")
//            @Override
//            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
//                switch (item.getItemId()) {
//                    case R.id.navigation_home:
//                        fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, new HomeFragment());
//                        break;
//                    case R.id.navigation_search:
//                        fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, new SearchFragment());
//                        break;
//                    case R.id.navigation_notifications:
//                        fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, new NotificationFragment());
//                        break;
//                    case R.id.navigation_profile:
//                        fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, new ProfileFragment());
//                        break;
//                }
//                item.setChecked(true);
//
//                fragmentTransaction.commit();
//                fragmentTransaction.runOnCommit(() -> {
//                    if (item.getItemId() == R.id.navigation_search) {
//                        Objects.requireNonNull(getSupportActionBar()).hide();
//                    }
//                });
//                return false;
//            }
//        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // post activity
        startActivity(new Intent(MainActivity.this, PostActivity.class));
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void showKeyboard(boolean shouldShow, EditText editText) {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if (manager != null) {
            if (shouldShow) {
                manager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.RESULT_SHOWN);
            } else {

                manager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void showDeleteFragment(PostModel model) {
        LogoutFragment.shouldDelete = true;
        LogoutFragment.model = model;
        LogoutFragment logoutFragment = new LogoutFragment();
        logoutFragment.show(getSupportFragmentManager(), logoutFragment.getTag());
    }

    @Override
    public void hideToolbar(boolean shouldHide) {
        if (shouldHide) {
            Objects.requireNonNull(getSupportActionBar()).hide();
        } else {
            Objects.requireNonNull(getSupportActionBar()).show();

        }
    }

    @Override
    public void showLogoutFragment() {
        LogoutFragment.shouldDelete = false;
        LogoutFragment logoutFragment = new LogoutFragment();
        logoutFragment.show(getSupportFragmentManager(), logoutFragment.getTag());
    }


    @Override
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Logging out");
        progressDialog.setMessage("Please wait");

        progressDialog.show();

        new Handler().postDelayed(() -> {
            progressDialog.dismiss();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }, 1500);
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseApp.initializeApp(MainActivity.this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance());
    }


}