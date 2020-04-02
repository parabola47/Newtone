package com.parabola.newtone.ui.fragment.start;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import com.parabola.newtone.ui.fragment.Scrollable;
import com.parabola.newtone.ui.fragment.Sortable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.parabola.newtone.util.AndroidTool.getScreenWidthDp;
import static java.util.Objects.requireNonNull;

public final class TabAlbumFragment extends MvpAppCompatFragment
        implements TabAlbumView, Sortable, Scrollable {

    private final AlbumAdapter albumsAdapter = new AlbumAdapter();
    private DividerItemDecoration verticalItemDecoration;

    @BindView(R.id.albums_list) RecyclerView albumsList;

    @InjectPresenter TabAlbumPresenter presenter;

    public TabAlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        verticalItemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
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
                albumsAdapter.showAsList();
                albumsList.addItemDecoration(verticalItemDecoration);
                break;
            case GRID:
                albumsList.removeItemDecoration(verticalItemDecoration);
                albumsAdapter.showAsGrid(calculateSpanCount());
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

    @Override
    public void smoothScrollToTop() {
        LinearLayoutManager layoutManager = requireNonNull((LinearLayoutManager) albumsList.getLayoutManager());
        int firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int screenItemsCount = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition();

        if (firstItemPosition > screenItemsCount * 3) {
            albumsList.scrollToPosition(screenItemsCount * 3);
        }

        albumsList.smoothScrollToPosition(0);
    }

}
