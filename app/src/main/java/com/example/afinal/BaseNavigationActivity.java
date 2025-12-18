package com.example.afinal;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

/**
 * Base Activity for all screens that need consistent navigation:
 * - Standard TopAppBar with Back + Home + Actions
 * - NavigationDrawer (optional, only for Dashboard)
 * - Bottom toolbar with FAB (optional, for main screens)
 */
public abstract class BaseNavigationActivity extends AppCompatActivity {
    
    protected Toolbar toolbarView;
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected BottomAppBar bottomAppBar;
    protected FloatingActionButton fab;
    
    private boolean isRootScreen = false;
    private boolean drawerEnabled = false;
    private boolean bottomBarEnabled = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Child classes will call setContentView() before calling setup methods
        
        // Setup OnBackPressedDispatcher for modern back handling
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerEnabled && drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    // Finish activity
                    finish();
                }
            }
        });
    }
    
    /**
     * Setup standard toolbar with Back + Home buttons
     * Note: Dashboard doesn't use traditional toolbar (uses hero card instead)
     * This is mainly for child screens
     * @param isRoot true if this is the root screen (Dashboard), false otherwise
     * @param title Title to display in toolbar
     */
    protected void setupToolbar(boolean isRoot, String title) {
        this.isRootScreen = isRoot;
        
        // Dashboard doesn't have toolbar - skip setup for root screen
        if (isRoot) {
            return;
        }
        
        // Try to find toolbar in layout (child screens should have this)
        int toolbarId = getResources().getIdentifier("toolbar", "id", getPackageName());
        if (toolbarId != 0) {
            toolbarView = findViewById(toolbarId);
        }
        
        if (toolbarView != null) {
            setSupportActionBar(toolbarView);
            
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
                
                // Show back button for non-root screens
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(android.R.drawable.ic_menu_revert);
            }
            
            // Set click listener for home/back
            toolbarView.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Go back
                    getOnBackPressedDispatcher().onBackPressed();
                }
            });
        }
        // If no toolbar found, that's OK - some screens might handle navigation differently
    }
    
    /**
     * Enable navigation drawer (only for Dashboard)
     */
    protected void enableDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        
        if (drawerLayout != null && navigationView != null) {
            drawerEnabled = true;
            
            // Set scrim color programmatically (semi-transparent black overlay)
            drawerLayout.setScrimColor(0x80000000);
            
            // Hide checkbox indicators by removing checkable behavior
            // NavigationView will still highlight selected items via background
            hideCheckboxIndicators();
            
            // Setup drawer toggle (only if toolbar exists)
            if (toolbarView != null) {
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        this, drawerLayout, toolbarView,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close);
                drawerLayout.addDrawerListener(toggle);
                toggle.syncState();
            }
            
            // Setup navigation menu clicks
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    
                    // Set selected state (highlight current item) - but don't show checkbox
                    item.setChecked(true);
                    
                    if (id == R.id.nav_dashboard) {
                        navigateToDashboard();
                    } else if (id == R.id.nav_topic) {
                        navigateToTopic();
                    } else if (id == R.id.nav_level) {
                        navigateToLevel();
                    } else if (id == R.id.nav_bookmarks) {
                        navigateToBookmarks();
                    } else if (id == R.id.nav_history) {
                        navigateToHistory();
                    } else if (id == R.id.nav_leaderboard) {
                        navigateToLeaderboard();
                    } else if (id == R.id.nav_settings) {
                        // TODO: Navigate to settings
                    }
                    
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
            });
            
            // Set initial selected item based on current activity
            setSelectedNavItem();
        }
    }
    
    /**
     * Hide checkbox indicators in NavigationView by finding and hiding the checkbox views
     */
    private void hideCheckboxIndicators() {
        if (navigationView == null) return;
        
        // Post to ensure view hierarchy is ready
        navigationView.post(new Runnable() {
            @Override
            public void run() {
                // Find all checkbox views in NavigationView and hide them
                findAndHideCheckboxes(navigationView);
            }
        });
    }
    
    /**
     * Recursively find and hide checkbox views
     */
    private void findAndHideCheckboxes(View view) {
        if (view instanceof android.widget.CheckBox) {
            view.setVisibility(View.GONE);
            return;
        }
        
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                findAndHideCheckboxes(group.getChildAt(i));
            }
        }
    }
    
    /**
     * Enable bottom toolbar with FAB
     * @param selectedTab Tab to highlight (0=Home, 1=Practice, 2=Mock, 3=More)
     */
    protected void enableBottomBar(int selectedTab) {
        bottomAppBar = findViewById(R.id.bottom_app_bar);
        fab = findViewById(R.id.fab); // FAB is optional now (removed from most screens)
        
        if (bottomAppBar != null) {
            bottomBarEnabled = true;
            
            // Set rounded top corners for BottomAppBar (Nomadstay style)
            try {
                float cornerRadius = getResources().getDimension(R.dimen.radius_card_large);
                com.google.android.material.shape.MaterialShapeDrawable background = 
                    (com.google.android.material.shape.MaterialShapeDrawable) bottomAppBar.getBackground();
                if (background != null) {
                    background.setShapeAppearanceModel(
                        background.getShapeAppearanceModel()
                            .toBuilder()
                            .setTopLeftCorner(com.google.android.material.shape.CornerFamily.ROUNDED, cornerRadius)
                            .setTopRightCorner(com.google.android.material.shape.CornerFamily.ROUNDED, cornerRadius)
                            .build()
                    );
                }
            } catch (Exception e) {
                // Ignore if shape appearance cannot be set
                android.util.Log.d("BaseNavigationActivity", "Could not set rounded corners: " + e.getMessage());
            }
            
            // FAB is no longer needed - removed per user request
            // Setup bottom bar menu clicks
            bottomAppBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    
                    if (id == R.id.bottom_nav_home) {
                        navigateToDashboard();
                        return true;
                    } else if (id == R.id.bottom_nav_practice) {
                        navigateToTopic();
                        return true;
                    } else if (id == R.id.bottom_nav_mock) {
                        navigateToLevel();
                        return true;
                    } else if (id == R.id.bottom_nav_ai) {
                        navigateToSmartPractice();
                        return true;
                    }
                    return false;
                }
            });
            
            // Highlight selected tab
            if (bottomAppBar.getMenu() != null && selectedTab < bottomAppBar.getMenu().size()) {
                MenuItem selectedItem = bottomAppBar.getMenu().getItem(selectedTab);
                if (selectedItem != null) {
                    selectedItem.setChecked(true);
                }
            }
        }
    }
    
    /**
     * Override to handle FAB click (Quick Mock Exam)
     */
    protected void onFabClick() {
        // Default: navigate to LevelActivity for quick mock exam
        navigateToLevel();
    }
    
    // Navigation helper methods - simplified to avoid instanceof errors
    protected void navigateToDashboard() {
        String currentClassName = this.getClass().getSimpleName();
        if (!"DashboardActivity".equals(currentClassName)) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
    
    protected void navigateToTopic() {
        String currentClassName = this.getClass().getSimpleName();
        if (!"TopicActivity".equals(currentClassName)) {
            startActivity(new Intent(this, TopicActivity.class));
        }
    }
    
    protected void navigateToLevel() {
        String currentClassName = this.getClass().getSimpleName();
        if (!"LevelActivity".equals(currentClassName)) {
            startActivity(new Intent(this, LevelActivity.class));
        }
    }
    
    protected void navigateToSmartPractice() {
        String currentClassName = this.getClass().getSimpleName();
        if (!"SmartPracticeActivity".equals(currentClassName)) {
            startActivity(new Intent(this, SmartPracticeActivity.class));
        }
    }
    
    protected void navigateToBookmarks() {
        String currentClassName = this.getClass().getSimpleName();
        if (!"BookmarksActivity".equals(currentClassName)) {
            startActivity(new Intent(this, BookmarksActivity.class));
        }
    }
    
    protected void navigateToHistory() {
        String currentClassName = this.getClass().getSimpleName();
        if (!"HistoryActivity".equals(currentClassName)) {
            startActivity(new Intent(this, HistoryActivity.class));
        }
    }
    
    protected void navigateToLeaderboard() {
        String currentClassName = this.getClass().getSimpleName();
        if (!"LeaderboardActivity".equals(currentClassName)) {
            startActivity(new Intent(this, LeaderboardActivity.class));
        }
    }
    
    /**
     * Set the selected navigation item based on current activity
     */
    protected void setSelectedNavItem() {
        if (navigationView == null) return;
        
        String currentClassName = this.getClass().getSimpleName();
        int selectedId = 0;
        
        if ("DashboardActivity".equals(currentClassName)) {
            selectedId = R.id.nav_dashboard;
        } else if ("TopicActivity".equals(currentClassName)) {
            selectedId = R.id.nav_topic;
        } else if ("LevelActivity".equals(currentClassName)) {
            selectedId = R.id.nav_level;
        } else if ("BookmarksActivity".equals(currentClassName)) {
            selectedId = R.id.nav_bookmarks;
        } else if ("HistoryActivity".equals(currentClassName)) {
            selectedId = R.id.nav_history;
        } else if ("LeaderboardActivity".equals(currentClassName)) {
            selectedId = R.id.nav_leaderboard;
        }
        
        if (selectedId != 0) {
            MenuItem item = navigationView.getMenu().findItem(selectedId);
            if (item != null) {
                item.setChecked(true);
            }
        }
    }
}
