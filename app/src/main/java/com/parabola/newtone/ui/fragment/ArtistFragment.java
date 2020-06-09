package com.parabola.newtone.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.domain.model.Album;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.AlbumAdapter;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.ArtistPresenter;
import com.parabola.newtone.mvp.view.ArtistView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver;
import com.parabola.newtone.ui.dialog.SortingDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType.GRID;
import static com.parabola.newtone.util.AndroidTool.calculateAlbumColumnCount;

public final class ArtistFragment extends BaseSwipeToBackFragment
        implements ArtistView, Sortable, Scrollable {
    private static final String TAG = ArtistFragment.class.getSimpleName();

    @InjectPresenter ArtistPresenter presenter;

    @BindView(R.id.tracks_count) TextView tracksCountTxt;
    @BindView(R.id.albums_list) RecyclerView albumsList;
    private DividerItemDecoration itemDecoration;

    @BindView(R.id.main) TextView artistNameTxt;
    @BindView(R.id.additional_info) TextView albumsCountTxt;


    private final AlbumAdapter albumsAdapter = new AlbumAdapter();

    private static final String ARTIST_ID_ARG_KEY = "artistId";

    public static ArtistFragment newInstance(int artistId) {
        Bundle args = new Bundle();
        args.putInt(ARTIST_ID_ARG_KEY, artistId);

        ArtistFragment fragment = new ArtistFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public ArtistFragment() {
        // Required empty public constructor
    }

    public int getArtistId() {
        return requireArguments().getInt(ARTIST_ID_ARG_KEY);
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_artist, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);

        albumsList.setAdapter(albumsAdapter);
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        albumsAdapter.setOnItemClickListener(position -> presenter.onAlbumItemClick(albumsAdapter.get(position).getId()));
        albumsAdapter.setOnItemLongClickListener(this::showAlbumContextMenu);

        return root;
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

    @OnClick(R.id.action_bar)
    public void onClickActionBar() {
        smoothScrollToTop();
    }


    @OnClick(R.id.all_tracks_bar)
    public void onClickAllTracksBar() {
        presenter.onClickAllTracks();
    }

    @Override
    protected void onClickBackButton() {
        presenter.onClickBack();
    }

    @Override
    protected void onEndSlidingAnimation() {
        presenter.onEnterSlideAnimationEnded();
    }

    @ProvidePresenter
    public ArtistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        int artistId = requireArguments().getInt(ARTIST_ID_ARG_KEY);

        return new ArtistPresenter(appComponent, artistId);
    }

    @Override
    public void setArtistName(String artistName) {
        artistNameTxt.setText(artistName);
    }

    @Override
    public void setTracksCount(int tracksCount) {
        String tracksCountFormatted = getResources()
                .getQuantityString(R.plurals.tracks_count, tracksCount, tracksCount);
        tracksCountTxt.setText(tracksCountFormatted);
    }

    @Override
    public void setAlbumsCount(int albumsCount) {
        String albumsCountFormatted = getResources()
                .getQuantityString(R.plurals.albums_count, albumsCount, albumsCount);
        albumsCountTxt.setText(albumsCountFormatted);
    }

    @Override
    public void refreshAlbums(List<Album> albums) {
        albumsAdapter.replaceAll(albums);
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
        return SortingDialog.ARTIST_ALBUMS_SORTING;
    }


    @Override
    public void smoothScrollToTop() {
        ((NestedScrollView) requireView().findViewById(R.id.artistView)).smoothScrollTo(0, 0);
    }

}
