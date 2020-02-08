package com.parabola.newtone.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.mvp.presenter.RenamePlaylistPresenter;
import com.parabola.newtone.mvp.view.RenamePlaylistView;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class RenamePlaylistDialog extends BaseDialogFragment
        implements RenamePlaylistView {

    @BindView(R.id.playlist_title) EditText playlistTitleEdt;


    @InjectPresenter RenamePlaylistPresenter presenter;


    private static final String PLAYLIST_BUNDLE_KEY = "playlist id";


    public static RenamePlaylistDialog newInstance(int playlistId) {
        Bundle args = new Bundle();
        args.putInt(PLAYLIST_BUNDLE_KEY, playlistId);

        RenamePlaylistDialog fragment = new RenamePlaylistDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.dialog_rename_playlist, container, false);
        ButterKnife.bind(this, layout);

        layout.findViewById(R.id.cancel).setOnClickListener(v -> presenter.onClickCancel());
        layout.findViewById(R.id.ok).setOnClickListener(v ->
                presenter.onClickRenamePlaylist(playlistTitleEdt.getText().toString()));

        return layout;
    }


    @ProvidePresenter
    RenamePlaylistPresenter provideRenamePlaylistPresenter() {
        int playlistId = requireArguments().getInt(PLAYLIST_BUNDLE_KEY);

        return new RenamePlaylistPresenter(MainApplication.getComponent(), playlistId);
    }

    @Override
    public void setPlaylistTitle(String playlistTitle) {
        playlistTitleEdt.setText(playlistTitle);
    }

    @Override
    public void setTitleSelected() {
        playlistTitleEdt.setSelection(playlistTitleEdt.length());
        playlistTitleEdt.selectAll();
    }

    @Override
    public void showToast(String toastText) {
        Toast.makeText(requireContext(), toastText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void closeScreen() {
        dismiss();
    }

    @Override
    public void setPlaylistTitleIsEmptyError() {
        playlistTitleEdt.setError(getString(R.string.error_playlist_title_is_empty));
    }
}
