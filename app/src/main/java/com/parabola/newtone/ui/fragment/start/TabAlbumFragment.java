package com.parabola.newtone.ui.fragment.start;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Album;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumViewType;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.AlbumAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.TabAlbumPresenter;
import com.parabola.newtone.mvp.view.TabAlbumView;
import com.parabola.newtone.ui.dialog.SortingDialog;
import com.parabola.newtone.ui.fragment.Sortable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.parabola.newtone.util.AndroidTool.getScreenWidthDp;

public final class TabAlbumFragment extends MvpAppCompatFragment
        implements TabAlbumView, Sortable {

    private final AlbumAdapter albumsAdapter = new AlbumAdapter();

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

        albumsList.setAdapter(albumsAdapter);

        albumsAdapter.setItemClickListener(position -> {
            int albumId = albumsAdapter.get(position).getId();
            presenter.onItemClick(albumId);
        });

        return layout;
    }

    @ProvidePresenter
    public TabAlbumPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new TabAlbumPresenter(appComponent);
    }

    public void scrollTo(int albumId) {
        for (int i = 0; i < albumsAdapter.size(); i++) {
            if (albumsAdapter.get(i).getId() == albumId) {
                albumsList.scrollToPosition(i);
                return;
            }
        }
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
    public void setViewType(AlbumViewType viewType) {
        switch (viewType) {
            case LIST:
                albumsList.post(() -> albumsAdapter.showAsList());
                break;
            case GRID:
                albumsList.post(() -> albumsAdapter.showAsGrid(calculateSpanCount()));
                break;
            default: throw new IllegalArgumentException(viewType.toString());
        }
    }

    private int calculateSpanCount() {
        float widthDp = getScreenWidthDp(requireContext(), requireActivity().getWindowManager());

        int columnsCount = 2;
        if (widthDp > 500) columnsCount = ((int) widthDp / 200);

        return columnsCount;
    }

    @Override
    public String getListType() {
        return SortingDialog.ALL_ALBUMS_SORTING;
    }
}
