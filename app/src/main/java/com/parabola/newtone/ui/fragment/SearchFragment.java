package com.parabola.newtone.ui.fragment;

import static androidx.core.content.ContextCompat.getColor;
import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;

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
import com.parabola.newtone.databinding.FragmentSearchBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.SearchPresenter;
import com.parabola.newtone.mvp.view.SearchFragmentView;

import java.util.List;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class SearchFragment extends MvpAppCompatFragment
        implements SearchFragmentView {

    @InjectPresenter SearchPresenter presenter;

    private FragmentSearchBinding binding;

    private final ArtistAdapter artistAdapter = new ArtistAdapter();
    private final AlbumAdapter albumAdapter = new AlbumAdapter();
    private final TrackAdapter trackAdapter = new TrackAdapter();
    private final PlaylistAdapter playlistAdapter = new PlaylistAdapter();

    private DividerItemDecoration itemDecoration;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        binding.artistsView.setAdapter(artistAdapter);
        binding.albumsView.setAdapter(albumAdapter);
        binding.tracksView.setAdapter(trackAdapter);
        binding.playlistView.setAdapter(playlistAdapter);
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        binding.backBtn.setOnClickListener(v -> presenter.onClickBackButton());

        AlbumItemView newAlbumItemView = new AlbumItemView(AlbumViewType.LIST, 16, 16, 64, 4);
        albumAdapter.setViewSettings(newAlbumItemView, 1);

        artistAdapter.setOnItemClickListener(position -> {
            binding.searchView.clearFocus();
            int artistId = artistAdapter.get(position).getId();
            presenter.onClickArtistItem(artistId);
        });
        albumAdapter.setOnItemClickListener(position -> {
            binding.searchView.clearFocus();
            int albumId = albumAdapter.get(position).getId();
            presenter.onClickAlbumItem(albumId);
        });
        trackAdapter.setOnItemClickListener(position -> {
            binding.searchView.clearFocus();
            presenter.onClickTrackItem(trackAdapter.getAll(), position);
        });
        playlistAdapter.setOnItemClickListener(position -> {
            binding.searchView.clearFocus();
            int playlistId = playlistAdapter.get(position).getId();
            presenter.onClickPlaylistItem(playlistId);
        });

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                presenter.onQueryTextSubmit(query);
                binding.searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty())
                    presenter.onClearText();
                return false;
            }
        });
        int iconTint = getColor(requireContext(), R.color.colorActionBarIconTint);
        ((ImageView) binding.searchView.findViewById(R.id.search_close_btn)).setColorFilter(iconTint, PorterDuff.Mode.SRC_ATOP);
        int textColor = getColor(requireContext(), R.color.colorNewtoneSecondaryText);
        ((TextView) binding.searchView.findViewById(R.id.search_src_text)).setTextColor(textColor);
        int textHintColor = getColor(requireContext(), android.R.color.darker_gray);
        ((TextView) binding.searchView.findViewById(R.id.search_src_text)).setHintTextColor(textHintColor);

        requireActivity().getSupportFragmentManager().addOnBackStackChangedListener(onBackStackChangedListener);

        return binding.getRoot();
    }

    private final FragmentManager.OnBackStackChangedListener onBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            FragmentActivity activity = getActivity();

            if (activity != null && activity.getSupportFragmentManager().getPrimaryNavigationFragment() != SearchFragment.this) {
                binding.searchView.clearFocus();
            }
        }
    };


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
        binding.searchView.requestFocusFromTouch();
    }

    @Override
    public void refreshArtists(List<Artist> artists) {
        artistAdapter.replaceAll(artists);
        binding.artistListHeader.setVisibility(artists.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void refreshAlbums(List<Album> albums) {
        albumAdapter.replaceAll(albums);
        binding.albumListHeader.setVisibility(albums.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void refreshTracks(List<Track> tracks) {
        trackAdapter.replaceAll(tracks);
        binding.trackListHeader.setVisibility(tracks.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setTrackItemViewSettings(ViewSettingsInteractor.TrackItemView trackItemView) {
        trackAdapter.setViewSettings(trackItemView);
    }


    @Override
    public void setItemDividerShowing(boolean showed) {
        binding.artistsView.removeItemDecoration(itemDecoration);
        binding.albumsView.removeItemDecoration(itemDecoration);
        binding.tracksView.removeItemDecoration(itemDecoration);
        binding.playlistView.removeItemDecoration(itemDecoration);

        if (showed) {
            binding.artistsView.addItemDecoration(itemDecoration);
            binding.albumsView.addItemDecoration(itemDecoration);
            binding.tracksView.addItemDecoration(itemDecoration);
            binding.playlistView.addItemDecoration(itemDecoration);
        }
    }

    @Override
    public void refreshPlaylists(List<Playlist> playlists) {
        playlistAdapter.replaceAll(playlists);
        binding.playlistHeader.setVisibility(playlists.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void clearAllLists() {
        artistAdapter.clear();
        albumAdapter.clear();
        trackAdapter.clear();
        playlistAdapter.clear();
        binding.artistListHeader.setVisibility(View.GONE);
        binding.albumListHeader.setVisibility(View.GONE);
        binding.trackListHeader.setVisibility(View.GONE);
        binding.playlistHeader.setVisibility(View.GONE);
    }


    @Override
    public void setLoadDataProgressBarVisibility(boolean visible) {
        binding.loadDataProgressBarContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
