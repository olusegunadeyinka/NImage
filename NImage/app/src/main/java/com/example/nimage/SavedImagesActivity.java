package com.example.nimage;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class SavedImagesActivity extends AppCompatActivity {

    private ListView listView;
    private SQLiteDatabase database;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_images);

        listView = findViewById(R.id.listView);

        // Initialize the database
        database = openOrCreateDatabase("Images", MODE_PRIVATE, null);

        // Fetch saved images from the database
        Cursor cursor = database.rawQuery("SELECT * FROM images", null);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = packageInfo.versionName;
            getSupportActionBar().setTitle("Saved Images - v" + version);
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
                    startActivity(new Intent(SavedImagesActivity.this, MainActivity.class));
                } else if (id == R.id.nav_saved_images_activity) {
                    // Navigate to SavedImagesActivity
                    startActivity(new Intent(SavedImagesActivity.this, SavedImagesActivity.class));
                }
                else if (id == R.id.nav_delete_images_activity) {
                    // Navigate to SavedImagesActivity
                    startActivity(new Intent(SavedImagesActivity.this, DeleteImagesActivity.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                String date = item.split("\n")[0].split(": ")[1];
                String url = item.split("\n")[1].split(": ")[1];

                Intent intent = new Intent(SavedImagesActivity.this, ImageDetailsActivity.class);
                intent.putExtra("date", date);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        // Create an adapter for the ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        // Populate the ListView
        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                String url = cursor.getString(1);
                adapter.add("Date: " + date + "\nURL: " + url);
            } while (cursor.moveToNext());
        }

        cursor.close();
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
                .setMessage("This activity displays a list of images that have been saved to the database.\n" +
                        "Click on an item in the list to view detailed information about the image in the Image Details Activity.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

}
