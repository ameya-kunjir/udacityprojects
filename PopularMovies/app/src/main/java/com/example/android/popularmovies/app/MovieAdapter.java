/*
 * Copyright (C) 2016 The Android Popular Movies Project
 */

package com.example.android.popularmovies.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Defines custom adapter which defines layout
 * for each data element
 */

public class MovieAdapter extends ArrayAdapter<Movie> {

    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private String baseUri;
    private Context contxt;

    public MovieAdapter(Activity context, List<Movie> moviesList) {
        super(context, 0, moviesList);
        this.contxt = context;
        this.baseUri = context.getString(R.string.base_url);
    }

    // set layout for each data element
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Movie movie = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_movie_poster, parent, false);
        }

        ImageView moviePosterView = (ImageView) convertView.findViewById(R.id.movie_poster_image);
        Picasso.with(contxt).load(baseUri + movie.moviePosterURI.substring(1)).into(moviePosterView);

      //  Log.v(LOG_TAG, "Movie Poster URI:" + baseUri + movie.moviePosterURI.substring(1));

        return moviePosterView;
    }

}
