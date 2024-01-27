package com.example.nimage;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.InputStream;
import java.net.URL;

public class ImageDetailsActivity extends AppCompatActivity {

    private TextView dateTextView, urlTextView;
    private ImageView imageView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = packageInfo.versionName;
            getSupportActionBar().setTitle("Image Details - v" + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_main_activity) {
                    // Navigate to MainActivity
                    startActivity(new Intent(ImageDetailsActivity.this, MainActivity.class));
                } else if (id == R.id.nav_saved_images_activity) {
                    // Navigate to SavedImagesActivity
                    startActivity(new Intent(ImageDetailsActivity.this, SavedImagesActivity.class));
                }
                else if (id == R.id.nav_delete_images_activity) {
                    // Navigate to SavedImagesActivity
                    startActivity(new Intent(ImageDetailsActivity.this, DeleteImagesActivity.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        dateTextView = findViewById(R.id.dateTextView);
        urlTextView = findViewById(R.id.urlTextView);
        imageView = findViewById(R.id.imageView);

        // Get the date and URL from the intent
        Intent intent = getIntent();
        String date = intent.getStringExtra("date");
        String url = intent.getStringExtra("url");

        // Create a new instance of ImageDetailsFragment
        ImageDetailsFragment fragment = new ImageDetailsFragment();

        // Pass the date and URL to the fragment
        Bundle args = new Bundle();
        args.putString("date", date);
        args.putString("url", url);
        fragment.setArguments(args);

        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, fragment).commit();
    }

    private class FetchImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show ProgressBar before fetching image
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap bitmap = null;
            try {
                InputStream in = new URL(url).openStream();
                bitmap = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Hide ProgressBar after fetching image
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);

            // Display the fetched image
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageBitmap(result);
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
                .setMessage("This activity is launched from the Saved Images Activity.\n" +
                        "It displays detailed information about a selected image, including the date, URL, and the image itself.\n" +
                        "The date and URL are passed from the Saved Images Activity.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

}
