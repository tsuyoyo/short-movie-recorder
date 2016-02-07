package tsuyogoro.shortmovierecorder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieItemsAdapter.IMovieItemListener {

    private static final String TEMP_VIDEO_PATH = "temp_videos";

    private static final int MEDIA_TYPE_IMAGE = 0;

    private static final int MEDIA_TYPE_VIDEO = 1;

    private static final int REQUEST_CODE = 100;

    private MovieItemsAdapter mMovieItemsAdapter;

    @Bind(R.id.movie_item_list)
    RecyclerView itemList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.bind(this);

        File tmpDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), ".MyCameraApp");
        if (tmpDir.exists() && tmpDir.isDirectory()) {
            for (String tmpFileName : tmpDir.list()) {
                new File(tmpFileName).delete();
            }
        }

        List<MovieItemsAdapter.MovieItem> movieItems = new ArrayList<>();
        movieItems.add(new MovieItemsAdapter.MovieItem(
                getOutputMediaFile(MEDIA_TYPE_VIDEO, "01").toString(), "Cut 01"));

        movieItems.add(new MovieItemsAdapter.MovieItem(
                getOutputMediaFile(MEDIA_TYPE_VIDEO, "02").toString(), "Cut 02"));
        movieItems.add(new MovieItemsAdapter.MovieItem(
                getOutputMediaFile(MEDIA_TYPE_VIDEO, "03").toString(), "Cut 03"));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        itemList.setLayoutManager(layoutManager);

        mMovieItemsAdapter = new MovieItemsAdapter(movieItems, this);
        itemList.setAdapter(mMovieItemsAdapter);
    }

    @Override
    public void onItemClicked(MovieItemsAdapter.MovieItem movieItem) {
        Intent intent = new Intent(MainActivity.this, RecorderActivity.class);
        intent.putExtra(RecorderActivity.PARAM_KEY_RECORDING_LENGTH_SECOND, 5);
        intent.putExtra(RecorderActivity.PARAM_KEY_SAVE_PATH, movieItem.savedPath);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_CODE == requestCode) {
            mMovieItemsAdapter.notifyDataSetChanged();
        }
    }

    // Referred http://developer.android.com/intl/ja/guide/topics/media/camera.html#saving-media
    private File getOutputMediaFile(int type, String fileNameSuffix) {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), ".MyCameraApp");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + fileNameSuffix + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + fileNameSuffix + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

}
