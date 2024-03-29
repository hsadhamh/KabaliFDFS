package factor.app.fdfs.adapters;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import factor.app.fdfs.models.MovieInfo;
import factor.app.fdfs.R;
import factor.app.fdfs.providers.Typefaces;

/**
 * Created by hassanhussain on 7/16/2016.
 */
public class MoviesSpinnerAdapter extends ArrayAdapter<MovieInfo> {

    Context mContext;
    ArrayList<MovieInfo> mListMovies = new ArrayList<>();
    LayoutInflater mInflater;

    public MoviesSpinnerAdapter(Context context, int textViewResourceId, ArrayList<MovieInfo> objects) {
        super(context, textViewResourceId, objects);

        this.mContext = context;
        this.mListMovies.addAll(objects);
        mInflater = (LayoutInflater.from(context));
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Log.d("getView", "position view :" + i);
        MovieInfo movie = getItem(i);
        if(movie != null) {
            if(view == null){
                view = mInflater.inflate(R.layout.layout_spinner_item, null);
                view.setTag(new MoviesHolder(view));
            }
            Object obj =  view.getTag();
            if(obj instanceof MoviesHolder) {
                MoviesHolder holder = (MoviesHolder) obj;
                String movieShow = "<b>" + movie.getName() + "</b>, " + "<i>" +
                        (movie.isRunning() ? "[Running Now]" : "[" + movie.getReleaseDate() + "]") + "</i>";
                movieShow += "; " + movie.getLanguages().toString();
                holder.mTxt.setText(Html.fromHtml(movieShow));
            }
        }
        return view;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        Log.d("getDropDownView", "poistion drop :" + position);
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.layout_spinner_item, null);
        }
        TextView mTxt = (TextView)convertView.findViewById(R.id.id_txt_show_movie);
        mTxt.setTypeface(Typefaces.getRobotoMedium(mContext));
        MovieInfo movie = getItem(position);

        String movieShow = "<b>" + movie.getName() + "</b>, " + "<i>"
                + (movie.isRunning() ? "[Running Now]" : "[" + movie.getReleaseDate() + "]") + "</i>";
        movieShow += "; " + movie.getLanguages().toString();
        mTxt.setText(Html.fromHtml(movieShow));
        return convertView;
    }

    class MoviesHolder {
        @BindView(R.id.id_txt_show_movie)
        TextView mTxt;

        MoviesHolder(View v){
            ButterKnife.bind(this, v);
            mTxt.setTypeface(Typefaces.getRobotoMedium(mContext));
        }
    }
}