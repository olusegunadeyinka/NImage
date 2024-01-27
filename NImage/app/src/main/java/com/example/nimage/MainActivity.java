package com.example.nimage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
//https://api.nasa.gov/planetary/apod?api_key=mr54PjBKSEkxvqDXB6JzLBPNUgkLrfQtAQxYcdDA&date="

public class MainActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private Button fetchButton, saveButton;
    private TextView imageInfo;
    private ProgressBar progressBar;
    private SQLiteDatabase database;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String PREF_DATE = "date";
    private static final String PREF_URL = "url";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = packageInfo.versionName;
            getSupportActionBar().setTitle("Main Activity - v" + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }




        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_main_activity) {
                    // Navigate to MainActivity
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                } else if (id == R.id.nav_saved_images_activity) {
                    // Navigate to SavedImagesActivity
                    startActivity(new Intent(MainActivity.this, SavedImagesActivity.class));
                }
                else if (id == R.id.nav_delete_images_activity) {
                    // Navigate to SavedImagesActivity
                    startActivity(new Intent(MainActivity.this, DeleteImagesActivity.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });


        datePicker = findViewById(R.id.datePicker);
        fetchButton = findViewById(R.id.fetchButton);
        saveButton = findViewById(R.id.saveButton);
        imageInfo = findViewById(R.id.imageInfo);
        progressBar = findViewById(R.id.progressBar);
        // Hide ProgressBar when activity is launched
        progressBar.setVisibility(View.GONE);
        // Initialize the database
        database = openOrCreateDatabase("Images", MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS images(date VARCHAR, url VARCHAR);");
        // Load saved image details from SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String date = prefs.getString(PREF_DATE, null);
        String url = prefs.getString(PREF_URL, null);
        if (date != null && url != null) {
            imageInfo.setText("Date: " + date + "\nURL: " + url);
            saveButton.setVisibility(View.VISIBLE);
        }
        fetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = datePicker.getYear() + "-" + (datePicker.getMonth() + 1) + "-" + datePicker.getDayOfMonth();
                new FetchImageTask().execute(date);
            }
        });
        imageInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = imageInfo.getText().toString().split("\n")[1].split(": ")[1];
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date = datePicker.getYear() + "-" + (datePicker.getMonth() + 1) + "-" + datePicker.getDayOfMonth();
                String url = imageInfo.getText().toString().split("\n")[1].split(": ")[1];

                database.execSQL("INSERT INTO images VALUES('" + date + "', '" + url + "');");
                saveButton.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Image saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class FetchImageTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            // Show ProgressBar before fetching image
            progressBar.setVisibility(View.VISIBLE);
            imageInfo.setText("");
            saveButton.setVisibility(View.GONE);
        }
        @Override
        protected String doInBackground(String... dates) {
            String date = dates[0];
            String urlString = "https://api.nasa.gov/planetary/apod?api_key=mr54PjBKSEkxvqDXB6JzLBPNUgkLrfQtAQxYcdDA&date=" + date;
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Hide ProgressBar after fetching image
            progressBar.setVisibility(View.GONE);
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String date = jsonObject.getString("date");
                    String url = jsonObject.getString("url");

                    // Save fetched image details to SharedPreferences
                    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString(PREF_DATE, date);
                    editor.putString(PREF_URL, url);
                    editor.apply();

                    imageInfo.setText("Date: " + date + "\nURL: " + url);
                    saveButton.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    imageInfo.setText("Error parsing API response.");
                }
            } else {
                imageInfo.setText("No data found.");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle navigation drawer toggle
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle help icon
        if (item.getItemId() == R.id.action_help) {
            showHelpDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    private void showHelpDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Help")
                .setMessage("Select a date using the date picker.\n" +
                        "Click on the “Fetch Image” button to retrieve an image from NASA’s web servers for the selected date.\n" +
                        "The date and URL of the fetched image will be displayed. If no image is found for the selected date, a message will be shown indicating this.\n" +
                        "Click on the “Save Image” button to save the fetched image’s details (date and URL) to the database for later viewing.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

}