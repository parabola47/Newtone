package com.parabola.newtone.ui.fragment.playlist;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.PlaylistPresenter;
import com.parabola.newtone.mvp.view.PlaylistView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class PlaylistFragment extends BaseSwipeToBackFragment
        implements PlaylistView {

    @InjectPresenter PlaylistPresenter presenter;

    private final BaseAdapter<Track> tracksAdapter = new TrackAdapter();


    @BindView(R.id.tracks_list) RecyclerView tracksList;
    @BindView(R.id.additional_info) TextView songsCountTxt;
    @BindView(R.id.main) TextView playlistTitleTxt;


    private static final String SELECTED_PLAYLIST_ID = "playlist id";

    public static PlaylistFragment newInstance(int playlistId) {
        Bundle args = new Bundle();
        args.putInt(SELECTED_PLAYLIST_ID, playlistId);

        PlaylistFragment fragment = new PlaylistFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.list_track, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);

        tracksList.setAdapter((RecyclerView.Adapter) tracksAdapter);
        tracksList.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        tracksAdapter.setItemClickListener(position -> presenter.onClickTrackItem(tracksAdapter.getAll(), position));
        tracksAdapter.setItemLongClickListener(this::showTrackContextMenu);

        return root;
    }


    public void showTrackContextMenu(ViewGroup rootView, float x, float y, int itemPosition) {
        PopupMenu popupMenu = createPopupMenu(rootView, x, y, itemPosition);

        Track selectedTrack = tracksAdapter.get(itemPosition);

        if (selectedTrack.isFavourite())
            popupMenu.getMenu().findItem(R.id.add_to_favorites).setVisible(false);
        else popupMenu.getMenu().findItem(R.id.remove_from_favourites).setVisible(false);


        popupMenu.getMenu().findItem(R.id.remove_from_playlist).setVisible(true);
        popupMenu.getMenu().findItem(R.id.add_to_playlist).setVisible(false);

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.play:
                    List<Track> tracks = tracksAdapter.getAll();
                    presenter.onClickMenuPlay(tracks, itemPosition);
                    return true;
                case R.id.remove_from_playlist:
                    presenter.onClickMenuRemoveFromCurrentPlaylist(selectedTrack.getId());
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
            tracksAdapter.invalidateItem(itemPosition);
        });

        return popupMenu;
    }

    @Override
    protected void onClickBackButton() {
        presenter.onClickBack();
    }

    @ProvidePresenter
    public PlaylistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        int playlistId = requireArguments().getInt(SELECTED_PLAYLIST_ID);

        return new PlaylistPresenter(appComponent, playlistId);
    }

    @Override
    public void setTracksCount(int playlistSize) {
        songsCountTxt.setText(getResources().getQuantityString(R.plurals.tracks_count, playlistSize, playlistSize));
    }

    @Override
    public void setPlaylistTitle(String playlistTitle) {
        playlistTitleTxt.setText(playlistTitle);
    }

    @Override
    public void refreshTracks(List<Track> tracks) {
        tracksAdapter.replaceAll(tracks);
    }

    @Override
    public void setCurrentTrack(int trackId) {
        tracksAdapter.setSelectedCondition(track -> track.getId() == trackId);
    }
}
