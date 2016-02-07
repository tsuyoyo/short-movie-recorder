package tsuyogoro.shortmovierecorder;

import android.content.res.Resources;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieItemsAdapter extends RecyclerView.Adapter<MovieItemsAdapter.MovieItemViewHolder> {

    public static class MovieItem {
        public final String savedPath;

        public final String description;

        public MovieItem (String savedPath, String description) {
            this.savedPath = savedPath;
            this.description = description;
        }

        public boolean isReady() {
            return (new File(savedPath)).exists();
        }
    }

    public interface IMovieItemListener {
        void onItemClicked(MovieItem movieItem);
    }

    public final List<MovieItem> movieItems;

    private IMovieItemListener mListener;

    public MovieItemsAdapter(List<MovieItem> movieItems, IMovieItemListener listener) {
        this.movieItems = movieItems;
        mListener = listener;
    }

    @Override
    public MovieItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        parent.removeAllViews();

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.movie_item, null);
        return new MovieItemViewHolder(view, parent.getResources());
    }

    @Override
    public void onBindViewHolder(MovieItemViewHolder holder, int position) {
        holder.update(movieItems.get(position));
    }

    @Override
    public int getItemCount() {
        return movieItems.size();
    }

    public class MovieItemViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.image_item_add)
        public Button addBtn;

        @Bind(R.id.image_item_thumbnail)
        public VideoView preview;

        @Bind(R.id.iamge_item_description)
        public TextView descriptionView;

        private MovieItem mMovieItem;

        private final Resources mResources;

        public MovieItemViewHolder(View itemView, Resources resources) {
            super(itemView);
            mResources = resources;
            ButterKnife.bind(this, itemView);
        }

        public void update(MovieItem item) {

            mMovieItem = item;

            descriptionView.setText(mMovieItem.description);

            if (mMovieItem.isReady()) {
                addBtn.setVisibility(View.GONE);
                preview.setVisibility(View.VISIBLE);

                preview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mp.setVolume(0, 0);
                        preview.start();
                    }
                });
                preview.setVideoPath(item.savedPath);
                preview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.start();
                    }
                });

            } else {
                addBtn.setVisibility(View.VISIBLE);
                preview.setVisibility(View.GONE);
            }
        }

        @OnClick(R.id.image_item_add)
        public void onButtonClicked() {
            mListener.onItemClicked(mMovieItem);
        }
    }
}
