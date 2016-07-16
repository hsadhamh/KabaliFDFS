package factor.app.fdfs;


import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rey.material.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by hassanhussain on 7/16/2016.
 */
public class CinemasAdapter extends ArrayAdapter<CinemaInfo> {
    private SparseBooleanArray mSelectedItemsIds;
    Context mContext;
    ArrayList<CinemaInfo> mListMovies = new ArrayList<>();
    LayoutInflater mInflater;

    public CinemasAdapter(Context context, int textViewResourceId, ArrayList<CinemaInfo> objects) {
        super(context, textViewResourceId, objects);
        this.mContext = context;
        this.mListMovies.addAll(objects);
        mInflater = (LayoutInflater.from(context));
        mSelectedItemsIds = new SparseBooleanArray();
        for(int i=0; i<mListMovies.size(); i++)
            mSelectedItemsIds.put(i, false);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Log.d("getView", "position view :" + i);
        CinemaInfo movie = getItem(i);
        if(movie != null) {
            if(view == null){
                view = mInflater.inflate(R.layout.layout_list_item, null);
                view.setTag(new MoviesHolder(view));
            }
            Object obj =  view.getTag();
            if(obj instanceof MoviesHolder) {
                MoviesHolder holder = (MoviesHolder) obj;
                String movieShow = "<b>" + movie.getCinemaName() + "</b>";
                holder.mTxt.setText(Html.fromHtml(movieShow));
                if(mSelectedItemsIds.get(i))
                    holder.setCheckedState(true);
                else
                    holder.setCheckedState(false);
            }
        }
        return view;
    }

    class MoviesHolder {
        @BindView(R.id.id_txt_show_cinema)
        com.rey.material.widget.TextView mTxt;
        @BindView(R.id.id_check_box_item)
        CheckBox mCheck;

        MoviesHolder(View v){
            ButterKnife.bind(this, v);
        }

        public void setCheckedState(boolean b){
            mCheck.setChecked(b);
        }
    }

    public void toggleSelection(int position) {
        mSelectedItemsIds.put(position, !mSelectedItemsIds.get(position));
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
}
