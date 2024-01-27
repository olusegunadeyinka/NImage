package com.example.nimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.InputStream;
import java.net.URL;

public class ImageDetailsFragment extends Fragment {

    private TextView dateTextView, urlTextView;
    private ImageView imageView;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_details, container, false);

        dateTextView = view.findViewById(R.id.dateTextView);
        urlTextView = view.findViewById(R.id.urlTextView);
        imageView = view.findViewById(R.id.imageView);
        progressBar = view.findViewById(R.id.progressBar);

        // Get the date and URL from the arguments
        Bundle args = getArguments();
        String date = args.getString("date");
        String url = args.getString("url");

        // Display the date and URL
        dateTextView.setText("Date: " + date);
        urlTextView.setText("URL: " + url);

        // Fetch and display the image
        new FetchImageTask().execute(url);

        return view;
    }

    private class FetchImageTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Show ProgressBar before fetching image
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
            progressBar.setVisibility(View.GONE);

            // Display the fetched image
            imageView.setImageBitmap(result);
        }
    }
}
