package com.allplayers.android;

import java.util.ArrayList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.actionbarsherlock.app.ActionBar;
import com.allplayers.android.activities.AllplayersSherlockActivity;
import com.allplayers.objects.AlbumData;
import com.allplayers.objects.PhotoData;
import com.allplayers.rest.RestApiV1;
import com.devspark.sidenavigation.SideNavigationView;
import com.devspark.sidenavigation.SideNavigationView.Mode;

public class AlbumPhotosActivity extends AllplayersSherlockActivity {
    private ArrayList<PhotoData> photoList;
    private ArrayAdapter blankAdapter = null;
    private PhotoAdapter photoAdapter;
    private GridView grid;
    private ActionBar actionbar;
    private ProgressBar loading;

    /**
     * Called when the activity is first created, this sets up some variables,
     * creates the Action Bar, and sets up the Side Navigation Menu.
     * @param savedInstanceState: Saved data from the last instance of the
     * activity.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.albumdisplay);
        loading = (ProgressBar) findViewById(R.id.progress_indicator);

        final AlbumData album = (new Router(this)).getIntentAlbum();
        photoList = new ArrayList<PhotoData>();
        grid = (GridView) findViewById(R.id.gridview);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (photoList.get(position) != null) {
                    // Display the group page for the selected group
                    Intent intent = (new Router(AlbumPhotosActivity.this)).getPhotoPagerActivityIntent(photoList.get(position));
                    intent.putExtra("album title", album.getTitle());
                    startActivity(intent);
                }
            }
        });

        actionbar = getSupportActionBar();
        actionbar.setIcon(R.drawable.menu_icon);
        actionbar.setTitle(album.getTitle());

        sideNavigationView = (SideNavigationView)findViewById(R.id.side_navigation_view);
        sideNavigationView.setMenuItems(R.menu.side_navigation_menu);
        sideNavigationView.setMenuClickCallback(this);
        sideNavigationView.setMode(Mode.LEFT);

        new GetAlbumPhotosByAlbumIdTask().execute(album);
    }

    /**
     * Creates an array adapter to store the album's photos.
     */
    public void setAdapter() {

        if (photoList.isEmpty()) {
            String[] values = new String[] {"no photos to display"};

            blankAdapter = new ArrayAdapter<String>(AlbumPhotosActivity.this,
                                                    android.R.layout.simple_list_item_1, values);
            grid.setAdapter(blankAdapter);
        } else {
            photoAdapter = new PhotoAdapter(getApplicationContext());
            grid.setAdapter(photoAdapter);
        }
    }

    /**
     * Aggregates the albums photos specified by a json string into a map.
     * @param jsonResult The album's photos.
     */
    public void updateAlbumPhotos(String jsonResult) {

        PhotosMap photos = new PhotosMap(jsonResult);
        photoList.addAll(photos.getPhotoData());

        setAdapter();

        if (photoList.size() != 0) {
            photoAdapter.addAll(photoList);
        }
    }

    /**
     * Gets the photos from an album specified by its album ID using a rest call.
     */
    public class GetAlbumPhotosByAlbumIdTask extends AsyncTask<AlbumData, Void, String> {

        /**
         * Gets the photos in a specified album using a rest call.
         */
        protected String doInBackground(AlbumData... album) {

            return RestApiV1.getAlbumPhotosByAlbumId(album[0].getUUID(), 0, 0);
        }

        /**
         * Calls a method to organize the fetched photos into a map.
         */
        protected void onPostExecute(String jsonResult) {

            updateAlbumPhotos(jsonResult);
            loading.setVisibility(View.GONE);
        }
    }
}
