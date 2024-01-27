package com.example.nimage;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private Context context;
    private Cursor cursor;

    public ImageAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView dateTextView;
        public TextView urlTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            urlTextView = itemView.findViewById(R.id.urlTextView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (!cursor.moveToPosition(position)) {
            return;
        }

        try {
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String url = cursor.getString(cursor.getColumnIndexOrThrow("url"));

            holder.dateTextView.setText("Date: " + date);
            holder.urlTextView.setText("URL: " + url);
        } catch (IllegalArgumentException e) {
            // The exception
        }
    }


    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (cursor != null) {
            cursor.close();
        }

        cursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }
    }
}
