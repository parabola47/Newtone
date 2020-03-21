package com.parabola.newtone.ui.fragment.start;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Artist;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ArtistAdapter;
import com.parabola.newtone.adapter.BaseAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.TabArtistPresenter;
import com.parabola.newtone.mvp.view.TabArtistView;
import com.parabola.newtone.ui.dialog.SortingDialog;
import com.parabola.newtone.ui.fragment.Sortable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class TabArtistFragment extends MvpAppCompatFragment
        implements TabArtistView, Sortable {

    @BindView(R.id.artists_list) RecyclerView artistsList;

    @InjectPresenter TabArtistPresenter presenter;

    private final BaseAdapter<Artist> artistsAdapter = new ArtistAdapter();

    public TabArtistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_tab_artist, container, false);
        ButterKnife.bind(this, layout);

        artistsList.setAdapter((RecyclerView.Adapter) artistsAdapter);
        artistsList.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        artistsAdapter.setItemClickListener(position -> {
            int artistId = artistsAdapter.get(position).getId();
            presenter.onItemClick(artistId);
        });

        return layout;
    }


    @ProvidePresenter
    public TabArtistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new TabArtistPresenter(appComponent);
    }

    public void scrollTo(int artistId) {
        for (int i = 0; i < artistsAdapter.size(); i++) {
            if (artistsAdapter.get(i).getId() == artistId) {
                artistsList.scrollToPosition(i);
                return;
            }
        }
    }

    @Override
    public void refreshArtists(List<Artist> artists) {
        artistsAdapter.replaceAll(artists);
    }

    @Override
    public void setSectionShowing(boolean enable) {
        artistsAdapter.setSectionEnabled(enable);
    }

    @Override
    public String getListType() {
        return SortingDialog.ALL_ARTISTS_SORTING;
    }
}
