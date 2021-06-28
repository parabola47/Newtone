package com.parabola.newtone.ui.fragment.playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.adapter.TrackAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.RecentlyAddedPlaylistPresenter;
import com.parabola.newtone.mvp.view.RecentlyAddedPlaylistView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver;
import com.parabola.newtone.ui.fragment.Scrollable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static java.util.Objects.requireNonNull;

public final class RecentlyAddedPlaylistFragment extends BaseSwipeToBackFragment
        implements RecentlyAddedPlaylistView, Scrollable {

    private final TrackAdapter tracklistAdapter = new TrackAdapter();

    @InjectPresenter RecentlyAddedPlaylistPresenter presenter;


    @BindView(R.id.tracks_list) RecyclerView tracklistView;
    @BindView(R.id.main) TextView playlistTitle;
    @BindView(R.id.additional_info) TextView songsCount;
    private DividerItemDecoration itemDecoration;


    public RecentlyAddedPlaylistFragment() {
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.list_track, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);


        tracklistView.setAdapter(tracklistAdapter);
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        tracklistAdapter.setOnItemClickListener(position ->
                presenter.onClickTrackItem(tracklistAdapter.getAll(), position));
        tracklistAdapter.setOnItemLongClickListener(this::showTrackContextMenu);

        playlistTitle.setText(R.string.playlist_recently_added);

        return root;
    }

    @OnClick(R.id.action_bar)
    public void onClickActionBar() {
        smoothScrollToTop();
    }

    @Override
    protected void onClickBackButton() {
        presenter.onClickBack();
    }

    @Override
    protected void onEndSlidingAnimation() {
        presenter.onEnterSlideAnimationEnded();
    }

    @Override
    public void refreshTracks(List<Track> trackList) {
        tracklistAdapter.replaceAll(trackList);
        String tracksCount = getResources()
                .getQuantityString(R.plurals.tracks_count, trackList.size(), trackList.size());
        songsCount.setText(tracksCount);
    }

    @Override
    public void setItemViewSettings(TrackItemView viewSettings) {
        tracklistAdapter.setViewSettings(viewSettings);
    }

    @Override
    public void setItemDividerShowing(boolean showed) {
        tracklistView.removeItemDecoration(itemDecoration);

        if (showed)
            tracklistView.addItemDecoration(itemDecoration);
    }

    @Override
    public void removeTrack(int trackId) {
        tracklistAdapter.removeWithCondition(track -> track.getId() == trackId);
        String tracksCount = getResources()
                .getQuantityString(R.plurals.tracks_count, tracklistAdapter.size(), tracklistAdapter.size());
        songsCount.setText(tracksCount);
    }

    @Override
    public void setCurrentTrack(int trackId) {
        tracklistAdapter.setSelectedCondition(track -> track.getId() == trackId);
    }


    @ProvidePresenter
    RecentlyAddedPlaylistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new RecentlyAddedPlaylistPresenter(appComponent);
    }

    private void showTrackContextMenu(int position) {
        Track selectedTrack = tracklistAdapter.get(position);
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
        dialog.setOnShowListener(d -> tracklistAdapter.setContextSelected(position));
        dialog.setOnDismissListener(d -> tracklistAdapter.clearContextSelected());
        getLifecycle().addObserver(new DialogDismissLifecycleObserver(dialog));
        dialog.show();
    }

    private void handleSelectedMenu(MenuItem menuItem, Track selectedTrack, int itemPosition) {
        switch (menuItem.getItemId()) {
            case R.id.play:
                List<Track> tracks = tracklistAdapter.getAll();
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
    public void smoothScrollToTop() {
        LinearLayoutManager layoutManager = requireNonNull((LinearLayoutManager) tracklistView.getLayoutManager());
        int firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int screenItemsCount = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition();

        if (firstItemPosition > screenItemsCount * 3) {
            tracklistView.scrollToPosition(screenItemsCount * 3);
        }

        tracklistView.smoothScrollToPosition(0);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

}
