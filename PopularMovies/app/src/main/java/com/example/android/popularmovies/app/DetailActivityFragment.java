/*
 * Copyright (C) 2016 The Android Popular Movies Project
 */
package com.example.android.popularmovies.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        //Checks if any data is received from MainActivity via intent
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String movieData = intent.getStringExtra(Intent.EXTRA_TEXT);
            String[] movieDataSplitStr = movieData.split("#");
            TextView movieTitleTxtView = (TextView) rootView.findViewById(R.id.movie_title_text_view);
            movieTitleTxtView.setText(movieDataSplitStr[0]);
            ImageView movPosterImgView = (ImageView) rootView.findViewById(R.id.movie_poster_img_view);
            Picasso.with(getActivity()).load(getActivity().getString(R.string.base_url) + movieDataSplitStr[1]).into(movPosterImgView);
            TextView movieReleaseDate = (TextView) rootView.findViewById(R.id.movie_rel_date_text_view);
            String substr = movieDataSplitStr[4].substring(movieDataSplitStr[4].length()-4);
            movieReleaseDate.setText(substr);
            TextView movieRating = (TextView) rootView.findViewById(R.id.movie_rating_text_view);
            movieRating.setText("Rating: " + movieDataSplitStr[3]);
            TextView movieOverview = (TextView) rootView.findViewById(R.id.movie_overview_text_view);
            movieOverview.setText(movieDataSplitStr[2]);

        }

        return rootView;
    }
}
