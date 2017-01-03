/*
 * Copyright (C) 2016 The Android Popular Movies Project
 */

package com.example.android.popularmovies.app;

import java.util.Date;

/*
 *  used to hold attributes of Movie received from MovieDB
 */
public class Movie {

    String movieTitle;
    String moviePosterURI;
    String movieOverview;
    float userRating;
    Date movieReleaseDate;

    public Movie(String mTitle, String mPosUri, String mOverview, float rating, Date mRelDt) {
        this.movieTitle = mTitle;
        this.moviePosterURI = mPosUri;
        this.movieOverview = mOverview;
        this.userRating = rating;
        this.movieReleaseDate = mRelDt;
    }

    @Override
    public String toString() {
        return this.movieTitle + "#" + this.moviePosterURI + "#" + this.movieOverview + "#" + this.userRating + "#" + this.movieReleaseDate;
    }

}
