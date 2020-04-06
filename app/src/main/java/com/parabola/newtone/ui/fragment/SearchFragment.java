package com.parabola.newtone.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Album;
import com.parabola.domain.model.Artist;
import com.parabola.domain.model.Playlist;
import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.AlbumAdapter;
import com.parabola.newtone.adapter.ArtistAdapter;
import com.parabola.newtone.adapter.PlaylistAdapter;
import com.parabola.newtone.adapter.TrackAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.SearchPresenter;
import com.parabola.newtone.mvp.view.SearchFragmentView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static java.util.Objects.requireNonNull;

public final class SearchFragment extends MvpAppCompatFragment
        implements SearchFragmentView {

    @InjectPresenter SearchPresenter presenter;

    @BindView(R.id.searchView) SearchView searchView;
    @BindView(R.id.loadDataProgressBarContainer) ViewGroup loadDataProgressBarContainer;


    @BindView(R.id.artistsView) RecyclerView artistsView;
    @BindView(R.id.artistListHeader) TextView artistListHeader;
    private final ArtistAdapter artistAdapter = new ArtistAdapter();


    @BindView(R.id.albumsView) RecyclerView albumsView;
    @BindView(R.id.albumListHeader) TextView albumListHeader;
    private final AlbumAdapter albumAdapter = new AlbumAdapter();


    @BindView(R.id.tracksView) RecyclerView tracksView;
    @BindView(R.id.trackListHeader) TextView trackListHeader;
    private final TrackAdapter trackAdapter = new TrackAdapter();


    @BindView(R.id.playlistView) RecyclerView playlistView;
    @BindView(R.id.playlistHeader) TextView playlistHeader;
    private final PlaylistAdapter playlistAdapter = new PlaylistAdapter();

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, layout);

        artistsView.setAdapter(artistAdapter);
        artistsView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        albumsView.setAdapter(albumAdapter);
        albumsView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        tracksView.setAdapter(trackAdapter);
        tracksView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        playlistView.setAdapter(playlistAdapter);
        playlistView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        AlbumItemView newAlbumItemView = new AlbumItemView(AlbumViewType.LIST, 16, 16, 64, 4);
        albumAdapter.setViewSettings(newAlbumItemView, 1);


        artistAdapter.setItemClickListener(position -> {
            searchView.clearFocus();
            int artistId = artistAdapter.get(position).getId();
            presenter.onClickArtistItem(artistId);
        });
        albumAdapter.setItemClickListener(position -> {
            searchView.clearFocus();
            int albumId = albumAdapter.get(position).getId();
            presenter.onClickAlbumItem(albumId);
        });
        trackAdapter.setItemClickListener(position -> {
            searchView.clearFocus();
            presenter.onClickTrackItem(trackAdapter.getAll(), position);
        });
        playlistAdapter.setItemClickListener(position -> {
            searchView.clearFocus();
            int playlistId = playlistAdapter.get(position).getId();
            presenter.onClickPlaylistItem(playlistId);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                presenter.onQueryTextSubmit(query);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty())
                    presenter.onClearText();
                return false;
            }
        });

        requireActivity().getSupportFragmentManager().addOnBackStackChangedListener(onBackStackChangedListener);

        return layout;
    }

    private final FragmentManager.OnBackStackChangedListener onBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            FragmentActivity activity = getActivity();

            if (activity != null && activity.getSupportFragmentManager().getPrimaryNavigationFragment() != SearchFragment.this) {
                searchView.clearFocus();
            }
        }
    };

    @OnClick(R.id.back_btn)
    public void onClickBackButton() {
        presenter.onClickBackButton();
    }

    @Override
    public void onDestroyView() {
        requireActivity().getSupportFragmentManager().removeOnBackStackChangedListener(onBackStackChangedListener);

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        requireNonNull(imm).hideSoftInputFromWindow(requireView().getWindowToken(), 0);
        super.onDestroyView();
    }


    @ProvidePresenter
    public SearchPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new SearchPresenter(appComponent);
    }


    @Override
    public void focusOnSearchView() {
        searchView.requestFocusFromTouch();
    }

    @Override
    public void refreshArtists(List<Artist> artists) {
        artistAdapter.replaceAll(artists);
        artistListHeader.setVisibility(artists.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void refreshAlbums(List<Album> albums) {
        albumAdapter.replaceAll(albums);
        albumListHeader.setVisibility(albums.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void refreshTracks(List<Track> tracks) {
        trackAdapter.replaceAll(tracks);
        trackListHeader.setVisibility(tracks.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setTrackItemViewSettings(ViewSettingsInteractor.TrackItemView trackItemView) {
        trackAdapter.setViewSettings(trackItemView);
    }

    @Override
    public void refreshPlaylists(List<Playlist> playlists) {
        playlistAdapter.replaceAll(playlists);
        playlistHeader.setVisibility(playlists.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void clearAllLists() {
        artistAdapter.clear();
        albumAdapter.clear();
        trackAdapter.clear();
        playlistAdapter.clear();
        artistListHeader.setVisibility(View.GONE);
        albumListHeader.setVisibility(View.GONE);
        trackListHeader.setVisibility(View.GONE);
        playlistHeader.setVisibility(View.GONE);
    }


    @Override
    public void setLoadDataProgressBarVisibility(boolean visible) {
        loadDataProgressBarContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
