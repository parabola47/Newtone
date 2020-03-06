package com.parabola.newtone.ui.fragment.playlist;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.parabola.domain.model.Track;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.BaseAdapter;
import com.parabola.newtone.adapter.TrackAdapter;
import com.parabola.newtone.mvp.presenter.RecentlyAddedPlaylistPresenter;
import com.parabola.newtone.mvp.view.RecentlyAddedPlaylistView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class RecentlyAddedPlaylistFragment extends BaseSwipeToBackFragment
        implements RecentlyAddedPlaylistView {

    private final BaseAdapter<Track> tracklistAdapter = new TrackAdapter();
    @InjectPresenter RecentlyAddedPlaylistPresenter presenter;


    @BindView(R.id.tracks_list) RecyclerView tracklistView;
    @BindView(R.id.main) TextView playlistTitle;
    @BindView(R.id.additional_info) TextView songsCount;


    public RecentlyAddedPlaylistFragment() {
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.list_track, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);


        tracklistView.setAdapter((RecyclerView.Adapter) tracklistAdapter);
        tracklistView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        tracklistAdapter.setItemClickListener(position -> presenter.onClickTrackItem(
                tracklistAdapter.getAll(), position));
        tracklistAdapter.setItemLongClickListener(this::showTrackContextMenu);

        playlistTitle.setText(R.string.playlist_recently_added);

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

    @Override
    public void refreshTracks(List<Track> trackList) {
        tracklistAdapter.replaceAll(trackList);
        String tracksCount = getResources()
                .getQuantityString(R.plurals.tracks_count, trackList.size(), trackList.size());
        songsCount.setText(tracksCount);
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

    @Override
    public void clearCurrentItem() {
        tracklistAdapter.clearSelected();
    }

    @Override
    public void clearItems() {
        tracklistAdapter.clear();
    }

    @ProvidePresenter
    RecentlyAddedPlaylistPresenter providePresenter() {
        return new RecentlyAddedPlaylistPresenter(MainApplication.getComponent());
    }

    private void showTrackContextMenu(ViewGroup rootView, float x, float y, int itemPosition) {
        PopupMenu popupMenu = createPopupMenu(rootView, x, y, itemPosition);
        Track selectedTrack = tracklistAdapter.get(itemPosition);

        if (selectedTrack.isFavourite())
            popupMenu.getMenu().findItem(R.id.add_to_favorites).setVisible(false);
        else popupMenu.getMenu().findItem(R.id.remove_from_favourites).setVisible(false);

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.play:
                    List<Track> tracks = tracklistAdapter.getAll();
                    presenter.onClickMenuPlay(tracks, itemPosition);
                    return true;
                case R.id.add_to_playlist:
                    presenter.onClickMenuAddToPlaylist(selectedTrack.getId());
                    return true;
                case R.id.add_to_favorites:
                    presenter.onClickMenuAddToFavourites(selectedTrack.getId());
                    return true;
                case R.id.remove_from_favourites:
                    presenter.onClickMenuRemoveFromFavourites(selectedTrack.getId());
                    return true;
                case R.id.share_track:
                    presenter.onClickMenuShareTrack(selectedTrack);
                    return true;
                case R.id.additional_info:
                    presenter.onClickMenuAdditionalInfo(selectedTrack.getId());
                    return true;
                case R.id.delete_track:
                    AlertDialog dialog = new AlertDialog.Builder(requireContext())
                            .setTitle(R.string.track_menu_delete_dialog_title)
                            .setMessage(R.string.track_menu_delete_dialog_message)
                            .setPositiveButton(R.string.dialog_delete, (d, w) -> presenter.onClickMenuDeleteTrack(selectedTrack.getId()))
                            .setNegativeButton(R.string.dialog_cancel, null)
                            .create();
                    dialog.show();
                    return true;
                default: return false;
            }
        });

        popupMenu.show();
    }

    private PopupMenu createPopupMenu(ViewGroup rootView, float x, float y, int itemPosition) {
        final View tmpView = new View(requireContext());
        tmpView.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
        tmpView.setBackgroundColor(Color.TRANSPARENT);
        tmpView.setX(x);
        tmpView.setY(y);

        rootView.addView(tmpView);
        rootView.setBackgroundColor(getResources().getColor(R.color.colorTrackContextMenuBackground));

        PopupMenu popupMenu = new PopupMenu(requireContext(), tmpView, Gravity.CENTER);
        popupMenu.inflate(R.menu.track_menu);
        popupMenu.setOnDismissListener(menu -> {
            rootView.removeView(tmpView);
            tracklistAdapter.invalidateItem(itemPosition);
        });

        return popupMenu;
    }

}
