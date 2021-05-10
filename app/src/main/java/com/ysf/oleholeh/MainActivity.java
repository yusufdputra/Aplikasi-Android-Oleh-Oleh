package com.ysf.oleholeh;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ysf.oleholeh.ui.auth.LoginActivity;
import com.ysf.oleholeh.ui.favorite.FavoriteFragment;
import com.ysf.oleholeh.ui.home.HomeFragment;
import com.ysf.oleholeh.ui.notifications.NotificationsFragment;
import com.ysf.oleholeh.ui.profil.ProfilFragment;

public class MainActivity extends AppCompatActivity {


    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment;
            Intent intent;
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new HomeFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_favorite:
                    if (currentUser != null) {
                        fragment = new FavoriteFragment();
                        loadFragment(fragment);
                        return true;
                    } else {
                        intent = new Intent(getBaseContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                    return true;
                case R.id.navigation_notifications:
                    if (currentUser != null) {
                        fragment = new NotificationsFragment();
                        loadFragment(fragment);
                        return true;
                    } else {
                        intent = new Intent(getBaseContext(), LoginActivity.class);
                        startActivity(intent);
                    }
                    return true;
                case R.id.navigation_account:
                    if (currentUser != null) {
                        fragment = new ProfilFragment();
                        loadFragment(fragment);
                        return true;
                    } else {
                        intent = new Intent(getBaseContext(), LoginActivity.class);
                        startActivity(intent);
                    }

            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        loadFragment(new HomeFragment());
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null){
            getSupportFragmentManager().beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit();
            return true;
        }
        return false;
    }

}