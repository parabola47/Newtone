package com.parabola.newtone.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.mvp.presenter.DeletePlaylistPresenter;
import com.parabola.newtone.mvp.view.DeletePlaylistView;

public final class DeletePlaylistDialog extends BaseDialogFragment
        implements DeletePlaylistView {

    private static final String PLAYLIST_ID_BUNDLE_KEY = "playlist id";


    @InjectPresenter DeletePlaylistPresenter presenter;


    public static DeletePlaylistDialog newInstance(int playlistId) {
        Bundle args = new Bundle();
        args.putInt(PLAYLIST_ID_BUNDLE_KEY, playlistId);

        DeletePlaylistDialog fragment = new DeletePlaylistDialog();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.dialog_delete_playlist, container, false);

        layout.findViewById(R.id.cancel).setOnClickListener(v -> presenter.onClickCancel());
        layout.findViewById(R.id.delete).setOnClickListener(v -> presenter.onClickDelete());

        return layout;
    }

    @ProvidePresenter
    public DeletePlaylistPresenter providePresenter() {
        int playlistId = requireArguments().getInt(PLAYLIST_ID_BUNDLE_KEY);

        return new DeletePlaylistPresenter(MainApplication.getComponent(), playlistId);
    }

    @Override
    public void closeScreen() {
        dismiss();
    }
}