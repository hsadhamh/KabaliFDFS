package factor.app.fdfs.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.rey.material.widget.ImageView;
import com.rey.material.widget.ProgressView;
import com.rey.material.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import factor.app.fdfs.events.OpenBookMyShowEvent;
import factor.app.fdfs.models.CinemaInfo;
import factor.app.fdfs.models.MovieInTheatresInfo;
import factor.app.fdfs.R;
import factor.app.fdfs.providers.Typefaces;

/**
 * Created by hassanhussain on 7/16/2016.
 */
public class ListTheatresAdapter extends RecyclerView.Adapter<ListTheatresAdapter.TheatreHolder> {
    MovieInTheatresInfo mListMovieInTheatres = null;

    public ListTheatresAdapter(MovieInTheatresInfo list){ mListMovieInTheatres = list; }

    @Override
    public TheatreHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_movie_checker, parent, false);
        return new TheatreHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TheatreHolder holder, int position) {
        CinemaInfo movie = mListMovieInTheatres.getCinemas().get(position);
        holder.theatreName.setText(movie.getCinemaName());
        if(movie.getCinemaStatus() == null || movie.getCinemaStatus().isEmpty())
            holder.status.setText("Will be updated soon...");
        else
            holder.status.setText(movie.getCinemaStatus());
        if(movie.isBookingOpen()) {
            holder.statusView.setVisibility(View.GONE);
            holder.imgTick.setVisibility(View.VISIBLE);
        }
        else {
            holder.statusView.setVisibility(View.VISIBLE);
            holder.imgTick.setVisibility(View.GONE);
        }

        holder.mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.statusView.getVisibility() == View.GONE){
                    //  open book my show
                    EventBus.getDefault().post(new OpenBookMyShowEvent());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListMovieInTheatres.getCinemas().size();
    }

    class TheatreHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.id_txt_theatre_name)
        TextView theatreName;
        @BindView(R.id.id_txt_theatre_info)
        TextView status;

        @BindView(R.id.id_progress_theatre)
        ProgressView statusView;
        @BindView(R.id.id_tick_complete)
        ImageView imgTick;

        @BindView(R.id.id_view_holder)
        LinearLayout mLayout;

        public TheatreHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            theatreName.setTypeface(Typefaces.getRobotoMedium(itemView.getContext()));
            status.setTypeface(Typefaces.getRobotoMedium(itemView.getContext()));
        }
    }
}
