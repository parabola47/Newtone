package com.parabola.newtone.ui.fragment;

import static java.util.Objects.requireNonNull;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.adapter.TrackAdapter;
import com.parabola.newtone.databinding.ListTrackBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.AlbumPresenter;
import com.parabola.newtone.mvp.view.AlbumView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver;
import com.parabola.newtone.ui.dialog.SortingDialog;

import java.util.List;

import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class AlbumFragment extends BaseSwipeToBackFragment
        implements AlbumView, Sortable, Scrollable {

    @InjectPresenter AlbumPresenter presenter;

    private ListTrackBinding binding;

    private TextView albumTitleTxt;
    private TextView artistNameTxt;
    private ShapeableImageView albumCover;

    private final TrackAdapter tracksAdapter = new TrackAdapter();
    private DividerItemDecoration itemDecoration;

    private static final String ALBUM_ID_ARG_KEY = "albumId";

    public static AlbumFragment newInstance(int albumId) {
        Bundle args = new Bundle();
        args.putInt(ALBUM_ID_ARG_KEY, albumId);

        AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public AlbumFragment() {
        // Required empty public constructor
    }

    public int getAlbumId() {
        return requireArguments().getInt(ALBUM_ID_ARG_KEY);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        binding = ListTrackBinding.inflate(inflater, container, false);
        getRootBinding().container.addView(binding.getRoot());

        albumTitleTxt = getRootBinding().main;
        artistNameTxt = getRootBinding().additionalInfo;
        albumCover = getRootBinding().image;

        albumCover.setVisibility(View.VISIBLE);
        albumCover.setShapeAppearanceModel(albumCover.getShapeAppearanceModel().toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, getResources().getDimension(R.dimen.album_fragment_cover_corner_size))
                .build());

        binding.tracksList.setAdapter(tracksAdapter);
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        tracksAdapter.setOnItemClickListener(position -> presenter.onClickTrackItem(tracksAdapter.getAll(), position));
        tracksAdapter.setOnItemLongClickListener(this::showTrackContextMenu);
        getRootBinding().actionBar.setOnClickListener(v -> smoothScrollToTop());

        return root;
    }


    private void showTrackContextMenu(int position) {
        Track selectedTrack = tracksAdapter.get(position);
        ListPopupWindowAdapter menuAdapter = new ListPopupWindowAdapter(requireContext(), R.menu.track_menu);

        menuAdapter.setMenuVisibility(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.add_to_favorites:
                    return !selectedTrack.isFavourite();
                case R.id.remove_from_favourites:
                    return selectedTrack.isFavourite();
                case R.id.remove_from_playlist:
                    return false;
                default: return true;
            }
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.track_menu_title, selectedTrack.getArtistName(), selectedTrack.getTitle()))
                .setAdapter(menuAdapter, (d, which) ->
                        handleSelectedMenu(menuAdapter.getItem(which), selectedTrack, position))
                .create();
        dialog.setOnShowListener(d -> tracksAdapter.setContextSelected(position));
        dialog.setOnDismissListener(d -> tracksAdapter.clearContextSelected());
        getLifecycle().addObserver(new DialogDismissLifecycleObserver(dialog));
        dialog.show();
    }


    private void handleSelectedMenu(MenuItem menuItem, Track selectedTrack, int itemPosition) {
        switch (menuItem.getItemId()) {
            case R.id.play:
                List<Track> tracks = tracksAdapter.getAll();
                presenter.onClickMenuPlay(tracks, itemPosition);
                break;
            case R.id.add_to_playlist:
                presenter.onClickMenuAddToPlaylist(selectedTrack.getId());
                break;
            case R.id.add_to_favorites:
                presenter.onClickMenuAddToFavourites(selectedTrack.getId());
                break;
            case R.id.remove_from_favourites:
                presenter.onClickMenuRemoveFromFavourites(selectedTrack.getId());
                break;
            case R.id.share_track:
                presenter.onClickMenuShareTrack(selectedTrack);
                break;
            case R.id.additional_info:
                presenter.onClickMenuAdditionalInfo(selectedTrack.getId());
                break;
            case R.id.delete_track:
                presenter.onClickMenuDeleteTrack(selectedTrack.getId());
                break;
        }
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
    public AlbumPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        int albumId = requireArguments().getInt(ALBUM_ID_ARG_KEY);

        return new AlbumPresenter(appComponent, albumId);
    }

    @Override
    public void setAlbumTitle(String albumTitle) {
        albumTitleTxt.setText(albumTitle);
    }

    @Override
    public void setAlbumArtist(String artistName) {
        artistNameTxt.setText(artistName);
    }

    @Override
    public void setAlbumArt(Object artCover) {
        if (artCover != null)
            albumCover.setImageBitmap((Bitmap) artCover);
        else albumCover.setImageResource(R.drawable.album_default);
    }


    @Override
    public void refreshTracks(List<Track> tracks) {
        tracksAdapter.replaceAll(tracks);
    }

    @Override
    public void setItemViewSettings(TrackItemView itemViewSettings) {
        tracksAdapter.setViewSettings(itemViewSettings);
    }

    @Override
    public void setItemDividerShowing(boolean showed) {
        binding.tracksList.removeItemDecoration(itemDecoration);

        if (showed)
            binding.tracksList.addItemDecoration(itemDecoration);
    }


    @Override
    public void setCurrentTrack(int trackId) {
        tracksAdapter.setSelectedCondition(track -> track.getId() == trackId);
    }

    @Override
    public void showToast(String toastMessage) {
        Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getListType() {
        return SortingDialog.ALBUM_TRACKS_SORTING;
    }

    @Override
    public void smoothScrollToTop() {
        LinearLayoutManager layoutManager = requireNonNull((LinearLayoutManager) binding.tracksList.getLayoutManager());
        int firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int screenItemsCount = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition();

        if (firstItemPosition > screenItemsCount * 3) {
            binding.tracksList.scrollToPosition(screenItemsCount * 3);
        }

        binding.tracksList.smoothScrollToPosition(0);
    }
}
