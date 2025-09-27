package com.example.habittrackerrpg;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.habittrackerrpg.databinding.LevelProgressHeaderBinding;
import com.example.habittrackerrpg.ui.auth.AuthViewModel;
import com.example.habittrackerrpg.ui.auth.AuthenticationActivity;
import com.example.habittrackerrpg.ui.profile.ProfileViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private AuthViewModel authViewModel;
    private ProfileViewModel profileViewModel;
    private NavController navController;
    private LevelProgressHeaderBinding headerBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Povezujemo Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Povezujemo heder pomoću ViewBindinga
        View headerView = findViewById(R.id.level_header);
        headerBinding = LevelProgressHeaderBinding.bind(headerView);

        // Povezujemo Navigaciju
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_tasks, R.id.nav_profile, R.id.nav_stats)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // Posmatramo promene u progresu nivoa i ažuriramo heder
        profileViewModel.getLevelProgress().observe(this, result -> {
            if (result != null) {
                headerBinding.textViewLevel.setText("Level " + result.level);
                headerBinding.textViewXp.setText(result.xpForCurrentLevel + " / " + result.xpForNextLevel + " XP");
                headerBinding.progressBarXp.setMax((int) result.xpForNextLevel);
                headerBinding.progressBarXp.setProgress((int) result.xpForCurrentLevel);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            performLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void performLogout() {
        authViewModel.logoutUser();
        startActivity(new Intent(MainActivity.this, AuthenticationActivity.class));
        finish();
    }
}