package com.parabola.newtone.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.domain.model.Playlist;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.DialogPlaylistChooseBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.ChoosePlaylistPresenter;
import com.parabola.newtone.mvp.view.ChoosePlaylistView;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import moxy.MvpAppCompatDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class ChoosePlaylistDialog extends MvpAppCompatDialogFragment
        implements ChoosePlaylistView {

    @InjectPresenter ChoosePlaylistPresenter presenter;

    private PlaylistListViewAdapter playlistAdapter;

    private static final String TRACK_IDS_BUNDLE_KEY = "track ids";
    private int[] trackIds;


    public ChoosePlaylistDialog() {
    }


    public static ChoosePlaylistDialog newInstance(int... trackIds) {
        Bundle args = new Bundle();
        args.putIntArray(TRACK_IDS_BUNDLE_KEY, trackIds);

        ChoosePlaylistDialog fragment = new ChoosePlaylistDialog();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DialogPlaylistChooseBinding binding = DialogPlaylistChooseBinding
                .inflate(LayoutInflater.from(requireContext()));

        if (playlistAdapter == null)
            playlistAdapter = new PlaylistListViewAdapter(requireContext(), R.layout.item_playlist_lv, new ArrayList<>());
        if (binding.playlists.getAdapter() == null)
            binding.playlists.setAdapter(playlistAdapter);
        if (binding.playlists.getOnItemClickListener() == null)
            binding.playlists.setOnItemClickListener((parent, view, position, id) ->
                    presenter.onClickPlaylistItem(playlistAdapter.getItemNN(position).getId()));

        binding.createNewPlaylistButton.setOnClickListener(v -> presenter.onClickCreateNewPlaylist());

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.choose_playlist_dialog_title)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
    }


    @ProvidePresenter
    ChoosePlaylistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        trackIds = requireArguments().getIntArray(TRACK_IDS_BUNDLE_KEY);
        return new ChoosePlaylistPresenter(appComponent, trackIds);
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


            boolean playlistContainThisTrack = false;
            if (trackIds.length == 1) {
                playlistContainThisTrack = Observable.fromIterable(playlist.getPlaylistTracks())
                        .map(Playlist.TrackItem::getTrackId)
                        .any(trackId -> ChoosePlaylistDialog.this.trackIds[0] == trackId)
                        .blockingGet();
            }

            playlistHasTrackImg.setVisibility(playlistContainThisTrack ? View.VISIBLE : View.GONE);

            return row;
        }

    }

}
