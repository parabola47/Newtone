package com.parabola.newtone.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.domain.model.Playlist;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.ChoosePlaylistPresenter;
import com.parabola.newtone.mvp.view.ChoosePlaylistView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import moxy.MvpAppCompatDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class ChoosePlaylistDialog extends MvpAppCompatDialogFragment
        implements ChoosePlaylistView {


    private PlaylistListViewAdapter playlistAdapter;
    @InjectPresenter ChoosePlaylistPresenter presenter;

    @BindView(R.id.playlist) ListView playlists;

    private static final String SELECTED_TRACK_BUNDLE_KEY = "track id";
    private int trackId;


    public ChoosePlaylistDialog() {
    }


    public static ChoosePlaylistDialog newInstance(int selectedTrackId) {
        Bundle args = new Bundle();
        args.putInt(SELECTED_TRACK_BUNDLE_KEY, selectedTrackId);

        ChoosePlaylistDialog fragment = new ChoosePlaylistDialog();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View customView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_playlist_choose, null);
        ButterKnife.bind(this, customView);

        if (playlistAdapter == null)
            playlistAdapter = new PlaylistListViewAdapter(requireContext(), R.layout.item_playlist_lv, new ArrayList<>());
        if (playlists.getAdapter() == null)
            playlists.setAdapter(playlistAdapter);
        if (playlists.getOnItemClickListener() == null)
            playlists.setOnItemClickListener((parent, view, position, id) ->
                    presenter.onClickPlaylistItem(playlistAdapter.getItemNN(position).getId()));

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.choose_playlist_dialog_title)
                .setView(customView)
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
    }


    @OnClick(R.id.createNewPlaylistButton)
    public void onClickCreateNewPlaylist() {
        presenter.onClickCreateNewPlaylist();
    }


    @ProvidePresenter
    ChoosePlaylistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        trackId = requireArguments().getInt(SELECTED_TRACK_BUNDLE_KEY);
        return new ChoosePlaylistPresenter(appComponent, trackId);
    }

    @Override
    public void refreshPlaylists(List<Playlist> playlists) {
        playlistAdapter.clear();
        playlistAdapter.addAll(playlists);
        playlistAdapter.notifyDataSetChanged();
    }

    @Override
    public void closeScreen() {
        dismiss();
    }


    private class PlaylistListViewAdapter extends ArrayAdapter<Playlist> {

        public PlaylistListViewAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Playlist> playlists) {
            super(context, resource, playlists);
        }

        @NonNull
        public Playlist getItemNN(int position) {
            Playlist playlist = getItem(position);
            if (playlist == null) {
                throw new NullPointerException();
            }

            return playlist;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                row = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist_lv, parent, false);
            }
            Playlist playlist = getItemNN(position);

            TextView title = row.findViewById(R.id.title);
            TextView tracksCount = row.findViewById(R.id.tracks_count);
            ImageView playlistHasTrackImg = row.findViewById(R.id.playlistHasTrackImg);

            title.setText(playlist.getTitle());
            String trackCountStr = getResources().getQuantityString(R.plurals.tracks_count, playlist.size(), playlist.size());
            tracksCount.setText(trackCountStr);


            boolean playlistContainThisTrack = Observable.fromIterable(playlist.getPlaylistTracks())
                    .map(Playlist.TrackItem::getTrackId)
                    .any(trackId -> ChoosePlaylistDialog.this.trackId == trackId)
                    .blockingGet();

            playlistHasTrackImg.setVisibility(playlistContainThisTrack ? View.VISIBLE : View.GONE);

            return row;
        }

    }

}
