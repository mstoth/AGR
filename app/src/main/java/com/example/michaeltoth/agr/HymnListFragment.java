package com.example.michaeltoth.agr;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.List;

import static android.support.v4.content.ContextCompat.getSystemService;


public class HymnListFragment extends Fragment {
    private RecyclerView mHymnRecyclerView;
    private HymnAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hymn_list,container,false);
        mHymnRecyclerView = (RecyclerView) view.findViewById(R.id.hymn_recycler_view);
        mHymnRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();

//        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//        Display display = wm.getDefaultDisplay();
//        int height=display.getHeight();
//        ViewGroup.LayoutParams p = view.getLayoutParams();
//        int h = p.height;
//        p.height  = (int)(height/5);
//        view.setLayoutParams(p);

        return view;
    }

    private class HymnHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleView;
        private Hymn mHymn;

        public HymnHolder(LayoutInflater inflater,ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_hymn,parent,false));
            itemView.setOnClickListener(this);
            mTitleView = (TextView) itemView.findViewById(R.id.hymn_title);
        }

        public void bind(Hymn hymn) {
            mHymn = hymn;
            mTitleView.setText(hymn.getTitle());
        }

        @Override
        public void onClick(View view) {
            Toast.makeText(getActivity(),mHymn.getTitle() + " clicked!",Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        HymnBook hymnBook = HymnBook.get(getActivity());
        List<Hymn> hymns  = hymnBook.getHymns();
        mAdapter = new HymnAdapter(hymns);
        mHymnRecyclerView.setAdapter(mAdapter);

    }

    private class HymnAdapter extends RecyclerView.Adapter<HymnHolder> {
        private List<Hymn> mHymns;

        public HymnAdapter(List<Hymn> hymns) {

            mHymns = hymns;
        }

        @NonNull
        @Override
        public HymnHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new HymnHolder(layoutInflater,viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull HymnHolder hymnHolder, int i) {
            Hymn hymn = mHymns.get(i);
            hymnHolder.bind(hymn);
        }

        @Override
        public int getItemCount() {
            return mHymns.size();
        }
    }
}
