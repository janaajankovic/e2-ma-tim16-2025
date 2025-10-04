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
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import com.example.habittrackerrpg.data.model.Alliance;
import com.example.habittrackerrpg.data.repository.AllianceRepository;
import com.example.habittrackerrpg.data.repository.ProfileRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();

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
                R.id.nav_tasks, R.id.nav_profile, R.id.nav_shop, R.id.nav_stats)
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
        handleIncomingIntent(getIntent());
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
    private void handleIncomingIntent(Intent intent) {
        if (intent == null || !"HANDLE_ALLIANCE_INVITE".equals(intent.getStringExtra("action"))) {
            return;
        }
        String inviteId = intent.getStringExtra("inviteId");
        if (inviteId == null) return;
        getIntent().removeExtra("action");

        AllianceRepository allianceRepository = new AllianceRepository();
        ProfileRepository profileRepository = new ProfileRepository();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        profileRepository.getUserById(uid).observeForever(new androidx.lifecycle.Observer<com.example.habittrackerrpg.data.model.User>() {
            @Override
            public void onChanged(com.example.habittrackerrpg.data.model.User user) {
                profileRepository.getUserById(uid).removeObserver(this);

                if (user == null) return;

                boolean isAlreadyInAlliance = user.getAllianceId() != null && !user.getAllianceId().isEmpty();

                if (!isAlreadyInAlliance) {
                    FirebaseFirestore.getInstance().collection("users").document(uid).collection("alliance_invites").document(inviteId).get()
                            .addOnSuccessListener(inviteDoc -> {
                                if (inviteDoc.exists()) {
                                    var invite = inviteDoc.toObject(com.example.habittrackerrpg.data.model.AllianceInvite.class);
                                    if (invite != null) {
                                        invite.setId(inviteDoc.getId());
                                        allianceRepository.acceptAllianceInvite(invite, user);
                                        Toast.makeText(MainActivity.this, "You have joined the alliance!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    return;
                }

                db.collection("alliances").document(user.getAllianceId()).get().addOnSuccessListener(allianceDoc -> {
                    if (!allianceDoc.exists()) return;

                    Alliance oldAlliance = allianceDoc.toObject(Alliance.class);
                    oldAlliance.setId(allianceDoc.getId());

                    FirebaseFirestore.getInstance().collection("users").document(uid).collection("alliance_invites").document(inviteId).get()
                            .addOnSuccessListener(inviteDoc -> {
                                if (inviteDoc.exists()) {
                                    var invite = inviteDoc.toObject(com.example.habittrackerrpg.data.model.AllianceInvite.class);
                                    if (invite != null) {
                                        invite.setId(inviteDoc.getId());
                                        if (oldAlliance.getLeaderId().equals(user.getId())) {
                                            showDisbandConfirmationDialog(invite, user, oldAlliance, allianceRepository);
                                        } else {
                                            showLeaveConfirmationDialog(invite, user, allianceRepository);
                                        }
                                    }
                                }
                            });
                });
            }
        });
    }

    private void showLeaveConfirmationDialog(com.example.habittrackerrpg.data.model.AllianceInvite invite, com.example.habittrackerrpg.data.model.User user, AllianceRepository repo) {
        new AlertDialog.Builder(this)
                .setTitle("Join new alliance?")
                .setMessage("You are already in an alliance. Do you want to leave it to join '" + invite.getAllianceName() + "'?")
                .setPositiveButton("Yes, leave and join", (dialog, which) -> {
                    repo.acceptInviteAndLeaveOldAlliance(invite, user);
                    Toast.makeText(this, "You have joined " + invite.getAllianceName(), Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
    private void showDisbandConfirmationDialog(com.example.habittrackerrpg.data.model.AllianceInvite invite, com.example.habittrackerrpg.data.model.User user, com.example.habittrackerrpg.data.model.Alliance oldAlliance, AllianceRepository repo) {
        new AlertDialog.Builder(this)
                .setTitle("You are a Leader!")
                .setMessage("To join '" + invite.getAllianceName() + "', you must disband your current alliance. All members will be removed. Do you want to proceed?")
                .setPositiveButton("Yes, disband and join", (dialog, which) -> {
                    repo.disbandAllianceAndJoinNew(invite, user, oldAlliance);
                    Toast.makeText(this, "Alliance disbanded. You have joined " + invite.getAllianceName(), Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }
}