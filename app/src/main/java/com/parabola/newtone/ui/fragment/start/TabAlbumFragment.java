package com.parabola.newtone.ui.fragment.start;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.domain.model.Album;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.AlbumAdapter;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.TabAlbumPresenter;
import com.parabola.newtone.mvp.view.TabAlbumView;
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver;
import com.parabola.newtone.ui.dialog.SortingDialog;
import com.parabola.newtone.ui.fragment.Scrollable;
import com.parabola.newtone.ui.fragment.Sortable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType.GRID;
import static com.parabola.newtone.util.AndroidTool.calculateAlbumColumnCount;
import static java.util.Objects.requireNonNull;

public final class TabAlbumFragment extends MvpAppCompatFragment
        implements TabAlbumView, Sortable, Scrollable {
    private static final String LOG_TAG = TabAlbumFragment.class.getSimpleName();

    private final AlbumAdapter albumsAdapter = new AlbumAdapter();

    @BindView(R.id.albums_list) RecyclerView albumsList;
    private DividerItemDecoration itemDecoration;

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
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        albumsAdapter.setOnItemClickListener(position -> presenter.onItemClick(albumsAdapter.get(position).getId()));
        albumsAdapter.setOnItemLongClickListener(this::showAlbumContextMenu);

        return layout;
    }

    private void showAlbumContextMenu(int position) {
        Album selectedAlbum = albumsAdapter.get(position);
        ListPopupWindowAdapter menuAdapter = new ListPopupWindowAdapter(requireContext(), R.menu.album_menu);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.album_menu_title, selectedAlbum.getArtistName(), selectedAlbum.getTitle()))
                .setAdapter(menuAdapter, (d, which) ->
                        handleSelectedMenu(menuAdapter.getItem(which), selectedAlbum))
                .create();
        dialog.setOnShowListener(d -> albumsAdapter.setContextSelected(position));
        dialog.setOnDismissListener(d -> albumsAdapter.clearContextSelected());
        getLifecycle().addObserver(new DialogDismissLifecycleObserver(dialog));
        dialog.show();
    }

    private void handleSelectedMenu(MenuItem menuItem, Album selectedAlbum) {
        switch (menuItem.getItemId()) {
            case R.id.shuffle:
                presenter.onClickMenuShuffle(selectedAlbum.getId());
                break;
            case R.id.add_to_playlist:
                presenter.onClickMenuAddToPlaylist(selectedAlbum.getId());
                break;
        }
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
    public void setAlbumViewSettings(AlbumItemView viewSettings) {
        int spanCount = viewSettings.viewType == GRID
                ? calculateAlbumColumnCount(requireActivity())
                : 1;
        albumsAdapter.setViewSettings(viewSettings, spanCount);
    }

    @Override
    public void setItemDividerShowing(boolean showed) {
        albumsList.removeItemDecoration(itemDecoration);

        if (showed)
            albumsList.addItemDecoration(itemDecoration);
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
