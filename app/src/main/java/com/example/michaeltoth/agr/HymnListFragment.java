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
        return view;
    }

    private class HymnHolder extends RecyclerView.ViewHolder {

        public HymnHolder(LayoutInflater inflater,ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_hymn,parent,false));
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

        }

        @Override
        public int getItemCount() {
            return mHymns.size();
        }
    }
}
