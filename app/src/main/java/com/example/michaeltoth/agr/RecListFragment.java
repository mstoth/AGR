package com.example.michaeltoth.agr;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecListFragment extends Fragment {
    private RecyclerView mHymnRecyclerView;
    private RecListFragment.RecAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hymn_list,container,false);
        mHymnRecyclerView = (RecyclerView) view.findViewById(R.id.hymn_recycler_view);
        mHymnRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;
    }

    private class RecHolder extends RecyclerView.ViewHolder {
        private TextView mTitleView;
        private Hymn mHymn;

        public RecHolder(LayoutInflater inflater,ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_rec,parent,false));
            mTitleView = itemView.findViewById(R.id.rec_title);
        }

        public void bind(Hymn hymn) {
            mHymn = hymn;
            mTitleView.setText(hymn.getTitle());
        }
    }

    private void updateUI() {
        HymnBook hymnBook = HymnBook.get(getActivity());
        List<Hymn> hymns  = hymnBook.getRecs();
        mAdapter = new RecListFragment.RecAdapter(hymns);
        mHymnRecyclerView.setAdapter(mAdapter);
    }

    private class RecAdapter extends RecyclerView.Adapter<RecListFragment.RecHolder> {
        private List<Hymn> mHymns;

        public RecAdapter(List<Hymn> hymns) {
            mHymns = hymns;
        }

        @NonNull
        @Override
        public RecListFragment.RecHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new RecListFragment.RecHolder(layoutInflater,viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull RecListFragment.RecHolder hymnHolder, int i) {
            Hymn hymn = mHymns.get(i);
            hymnHolder.bind(hymn);
        }

        @Override
        public int getItemCount() {
            return mHymns.size();
        }
    }


}
