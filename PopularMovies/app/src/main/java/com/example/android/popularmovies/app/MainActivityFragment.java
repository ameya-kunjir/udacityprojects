/*
 * Copyright (C) 2016 The Android Popular Movies Project
 */

package com.example.android.popularmovies.app;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private MovieAdapter mMovieAdapter;
    private GridView mGridView;

    //creating sample data for movies
    /*Movie[] movies = {
            new Movie("Underworld: Blood Wars","/nHXiMnWUAUba2LZ0dFkNDVdvJ1o.jpg","Underworld: Blood Wars follows Vampire deat" , 4.1F , new Date())
    };*/

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

       mMovieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());
        //mMovieAdapter = new MovieAdapter(getActivity(), Arrays.asList(movies));

        mGridView = (GridView) rootView.findViewById(R.id.gridview_movie_poster);

        mGridView.setAdapter(mMovieAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Movie movie = mMovieAdapter.getItem(position);
                /* passing data for selected movie from GridView
                   to DetailActivity  */
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, movie.toString());
                startActivity(intent);
            }
        });

        return rootView;
    }


    /**
     * Querying Network Data on Activity Start up
     */
    @Override
    public void onStart() {
        super.onStart();
        //checking if network connection is available
        if (isOnline()) {
            updateMovieData();
        } else {
            Toast.makeText(getActivity(), "No Network Connection!", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Method to display error message in Toast
     */
    public void displayErrorInToast(String errorMessage) {
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_LONG).show();
    }


    /**
     * Method to check internet connectivity
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /*
    Fetch Movie Data using AsyncTask
     */
    private void updateMovieData() {
        FetchMoviesTask moviesTask = new FetchMoviesTask();
        String sortOrder = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.pref_sort_order_key), getString(R.string.pref_sort_order_default));
        moviesTask.execute(sortOrder);
    }

    /*
     Class FectMoviesTask fetches network data using Background thread
     by inheriting AsyncTask
     */
    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        public static final String SORT_ORDER_POPULAR = "popular";
        public static final String SORT_ORDER_TOP_RATED = "top_rated";


        @Override
        protected List<Movie> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            List<Movie> parsedMoviesList = null;
            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;
            URL url = null;
            try {
                // Construct the URL for the MovieDB api query based on selected sort order

                if (params[0].equalsIgnoreCase(SORT_ORDER_POPULAR)) {
                    url = new URL(getString(R.string.movie_db_popular_uri));
                } else if (params[0].equalsIgnoreCase(SORT_ORDER_TOP_RATED)) {
                    url = new URL(getString(R.string.movie_db_top_rated_uri));
                }

                // Create the request to MovieDB API, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error:" + e.getMessage(), e);
                // displaying error message to user in Toast
                displayErrorInToast(e.getMessage());
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                        displayErrorInToast(e.getMessage());
                        //e.printStackTrace();
                    }
                }
            }

            try {
                parsedMoviesList = getMovieDataFromJson(moviesJsonStr);

            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                displayErrorInToast(e.getMessage());
                //e.printStackTrace();
            }
            return parsedMoviesList;
        }

        /*
             Method for Parsing JSON string to extract Movies Data
        */
        private List<Movie> getMovieDataFromJson(String moviesJsonStr) throws JSONException {
            List<Movie> parsedMoviesList = new ArrayList<Movie>();
            // These are the names of the JSON objects that need to be extracted.
            final String JSON_RESULTS = "results";
            final String POSTER_PATH = "poster_path";
            final String MOVIE_TITLE = "title";
            final String MOVIE_OVERVIEW = "overview";
            final String MOVIE_RATING = "vote_average";
            final String MOVIE_RELEASE_DATE = "release_date";
            Movie movie = null;
            String movieTitle = null;
            String movieOverview = null;
            String movieRating = null;
            String movieReleaseDate = null;
            String moviePosterPath = null;
            Float movieRate = null;
            Date relDate = null;
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

            JSONObject moviesJson = new JSONObject(moviesJsonStr);

            JSONArray movieArray = moviesJson.getJSONArray(JSON_RESULTS);

            //paring all json objects sequentially
            // assigning parse data into movie object

            for (int i = 0; i < movieArray.length(); i++) {
                JSONObject movieInfo = movieArray.getJSONObject(i);
                movieTitle = movieInfo.getString(MOVIE_TITLE);
                movieOverview = movieInfo.getString(MOVIE_OVERVIEW);
                moviePosterPath = movieInfo.getString(POSTER_PATH);
                movieReleaseDate = movieInfo.getString(MOVIE_RELEASE_DATE);
                movieRating = movieInfo.getString(MOVIE_RATING);
                movieRate = Float.parseFloat(movieRating);
                try {
                    relDate = df.parse(movieReleaseDate);



                } catch (ParseException e) {
                    Log.e(LOG_TAG, e.getMessage());
                    displayErrorInToast(e.getMessage());
                    //e.printStackTrace();
                }
                //parsedc movie object is added into list
                movie = new Movie(movieTitle, moviePosterPath, movieOverview, movieRate, relDate);
                parsedMoviesList.add(movie);
            }

            Log.i(LOG_TAG, parsedMoviesList.toString());

            return parsedMoviesList;

        }


        /*
          Pass network data on to a main thread
         */

        @Override
        protected void onPostExecute(List<Movie> resultList) {

            if (resultList != null) {

                mMovieAdapter = new MovieAdapter(getActivity(), resultList);
                mGridView.setAdapter(mMovieAdapter);

            }

        }


    }


}
