package com.parabola.newtone.ui.fragment.start;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.parabola.domain.model.Album;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.AlbumAdapter;
import com.parabola.newtone.adapter.BaseAdapter;
import com.parabola.newtone.mvp.presenter.TabAlbumPresenter;
import com.parabola.newtone.mvp.view.TabAlbumView;
import com.parabola.newtone.ui.dialog.SortingDialog;
import com.parabola.newtone.ui.fragment.Sortable;
import com.parabola.newtone.util.AndroidTool;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class TabAlbumFragment extends MvpAppCompatFragment
        implements TabAlbumView, Sortable {

    private BaseAdapter<Album> albumsAdapter;

    @BindView(R.id.albums_list) RecyclerView albumsList;

    @InjectPresenter TabAlbumPresenter presenter;

    public TabAlbumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_tab_album, container, false);
        ButterKnife.bind(this, layout);

        albumsAdapter = new AlbumAdapter();
        albumsList.setAdapter((RecyclerView.Adapter) albumsAdapter);
        albumsList.post(() -> {
            int widthPx = albumsList.getWidth();
            float widthDp = AndroidTool.convertPixelsToDp(widthPx, layout.getContext());
            int columnsCount = 2;
            if (widthDp > 500) {
                columnsCount = ((int) widthDp / 200);
            }
            ((GridLayoutManager) albumsList.getLayoutManager()).setSpanCount(columnsCount);
        });

        albumsAdapter.setItemClickListener(position -> {
            int albumId = albumsAdapter.get(position).getId();
            presenter.onItemClick(albumId);
        });

        return layout;
    }

    @ProvidePresenter
    public TabAlbumPresenter providePresenter() {
        return new TabAlbumPresenter(MainApplication.getComponent());
    }

    @Override
    public void refreshAlbums(List<Album> albums) {
        albumsAdapter.replaceAll(albums);
    }

    @Override
    public void setSectionShowing(boolean enable) {
        albumsAdapter.setSectionEnabled(enable);
    }

    @Override
    public String getListType() {
        return SortingDialog.ALL_ALBUMS_SORTING;
    }
}
