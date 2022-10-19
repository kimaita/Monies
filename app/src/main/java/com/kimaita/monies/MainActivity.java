package com.kimaita.monies;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
import static androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode;
import static com.kimaita.monies.ui.HomeFragment.LOADER;
import static com.kimaita.monies.utils.MessageUtils.defineCursorLoader;
import static com.kimaita.monies.utils.MessageUtils.loadMpesaTransactions;
import static com.kimaita.monies.utils.PrefManager.THEME;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kimaita.monies.models.Message;
import com.kimaita.monies.utils.MessageExtractor;
import com.kimaita.monies.utils.PrefManager;
import com.kimaita.monies.viewmodels.MoneyViewModel;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 1;
    NavController navController;
    PrefManager prefManager;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    SharedPreferences sharedPreferences;
    MoneyViewModel moneyViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        // Checking for first time launch
        prefManager = new PrefManager(this);
        if (prefManager.isFirstTimeLaunch()) {
            checkPermissions();
        }
        if (prefManager.isNightMode()) {
            setDefaultNightMode(MODE_NIGHT_YES);
        }
        setContentView(R.layout.activity_main);
        moneyViewModel = new ViewModelProvider(this).get(MoneyViewModel.class);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        navController = Objects.requireNonNull(navHostFragment).getNavController();
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(R.id.navigation_home, R.id.navigation_analysis, R.id.settingsFragment)
                        .build();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationUI.setupWithNavController(
                toolbar, navController, appBarConfiguration);

        NavigationUI.setupWithNavController(bottomNav, navController);

        if (checkPermissions()) {
            LoaderManager.getInstance(this).initLoader(LOADER, null, loaderCallbacks(this, moneyViewModel, prefManager));
        }else{
            requestSMSPermission();
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        listener =
                (sharedPreferences1, s) -> {
                    if (s.equals(THEME)) {
                        if (sharedPreferences1.getBoolean(THEME, false))
                            setDefaultNightMode(MODE_NIGHT_YES);
                        else
                            setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    }
                };
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    private boolean checkPermissions() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestSMSPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_SMS)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to read M-PESA messages. If denied, M-PESA transactions will not be included.")
                    .setPositiveButton("Proceed", (dialog, which) -> ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE))
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS}, SMS_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Denied. No mobile money transactions will be shown", LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Fetching M-PESA Transactions...", LENGTH_LONG).show();
                LoaderManager.getInstance(this).initLoader(LOADER, null, loaderCallbacks(this, moneyViewModel, prefManager));
            }
            prefManager.setFirstTimeLaunch(false);
        }
    }

    public static LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks(@NonNull Context context, @NonNull MoneyViewModel viewModel, @NonNull PrefManager prefManager) {

        return new LoaderManager.LoaderCallbacks<Cursor>() {
            @NonNull
            @Override
            public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
                if (id == LOADER) {
                    return defineCursorLoader(context, prefManager.getMessageId());
                }
                return null;
            }

            @Override
            public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
                if (!data.isClosed()) {
                    List<String> messages = loadMpesaTransactions(data, prefManager);
                    viewModel.insertMessage(messages.parallelStream().map(s -> new MessageExtractor().getMessage(s)).toArray(Message[]::new));
                }
            }

            @Override
            public void onLoaderReset(@NonNull Loader<Cursor> loader) {

            }


        };
    }

    @Override
    public void onResume() {
        super.onResume();
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

}