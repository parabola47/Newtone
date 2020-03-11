package com.parabola.newtone.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.parabola.domain.model.Album;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumViewType;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.AlbumAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.ArtistPresenter;
import com.parabola.newtone.mvp.view.ArtistView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.dialog.SortingDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.parabola.newtone.util.AndroidTool.convertPixelsToDp;

public final class ArtistFragment extends BaseSwipeToBackFragment
        implements ArtistView, Sortable {
    private static final String TAG = ArtistFragment.class.getSimpleName();

    @InjectPresenter ArtistPresenter presenter;

    @BindView(R.id.all_tracks_bar) View allTracksBar;

    @BindView(R.id.tracks_count) TextView tracksCountTxt;
    @BindView(R.id.albums_list) RecyclerView albumsList;

    @BindView(R.id.main) TextView artistNameTxt;
    @BindView(R.id.additional_info) TextView albumsCountTxt;


    private AlbumAdapter albumsAdapter = new AlbumAdapter();

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

        albumsAdapter = new AlbumAdapter();
        albumsList.setAdapter(albumsAdapter);

        allTracksBar.setOnClickListener(view -> presenter.onClickAllTracks());
        albumsAdapter.setItemClickListener(position -> {
            int albumId = albumsAdapter.get(position).getId();
            presenter.onAlbumItemClick(albumId);
        });

        return root;
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
    public void setViewType(AlbumViewType viewType) {
        switch (viewType) {
            case LIST:
                albumsAdapter.showAsList();
                break;
            case GRID:
                albumsList.post(() -> {
                    float widthDp = convertPixelsToDp(albumsList.getWidth(), requireContext());

                    int columnsCount = 2;
                    if (widthDp > 500) {
                        columnsCount = ((int) widthDp / 200);
                    }

                    albumsAdapter.showAsGrid(columnsCount);
                });
                break;
            default: throw new IllegalArgumentException(viewType.toString());
        }
    }

    @Override
    public String getListType() {
        return SortingDialog.ARTIST_ALBUMS_SORTING;
    }
}
