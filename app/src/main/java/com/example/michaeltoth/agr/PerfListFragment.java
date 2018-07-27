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

import java.util.List;

public class PerfListFragment extends Fragment {
    private RecyclerView mHymnRecyclerView;
    private PerfListFragment.PerfAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hymn_list,container,false);
        mHymnRecyclerView = (RecyclerView) view.findViewById(R.id.hymn_recycler_view);
        mHymnRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;
    }

    private class PerfHolder extends RecyclerView.ViewHolder {

        public PerfHolder(LayoutInflater inflater,ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_perf,parent,false));
        }

    }

    private void updateUI() {
        HymnBook hymnBook = HymnBook.get(getActivity());
        List<Hymn> hymns  = hymnBook.getmPerfs();
        mAdapter = new PerfListFragment.PerfAdapter(hymns);
        mHymnRecyclerView.setAdapter(mAdapter);
    }

    private class PerfAdapter extends RecyclerView.Adapter<PerfListFragment.PerfHolder> {
        private List<Hymn> mHymns;

        public PerfAdapter(List<Hymn> hymns) {
            mHymns = hymns;
        }

        @NonNull
        @Override
        public PerfListFragment.PerfHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new PerfListFragment.PerfHolder(layoutInflater,viewGroup);
        }

        @Override
        public void onBindViewHolder(@NonNull PerfListFragment.PerfHolder hymnHolder, int i) {

        }

        @Override
        public int getItemCount() {
            return mHymns.size();
        }
    }

}
