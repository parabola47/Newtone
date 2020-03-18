package com.parabola.newtone.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.CreatePlaylistPresenter;
import com.parabola.newtone.mvp.view.CreatePlaylistView;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class CreatePlaylistDialog extends BaseDialogFragment
        implements CreatePlaylistView {

    @BindView(R.id.playlist_title) EditText playlistTitleEdt;

    @InjectPresenter CreatePlaylistPresenter presenter;

    public static CreatePlaylistDialog newInstance() {
        return new CreatePlaylistDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.dialog_create_playlist, container, false);
        ButterKnife.bind(this, layout);

        layout.findViewById(R.id.cancel).setOnClickListener(v -> presenter.onClickCancel());
        layout.findViewById(R.id.ok).setOnClickListener(v -> presenter.onClickCreatePlaylist(playlistTitleEdt.getText().toString()));

        return layout;
    }


    @Override
    public void setPlaylistTitleIsEmptyError() {
        String emptyTitleError = getString(R.string.error_playlist_title_is_empty);
        playlistTitleEdt.setError(emptyTitleError);
    }

    @Override
    public void showToast(String toastText) {
        Toast.makeText(requireContext(), toastText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void closeScreen() {
        dismiss();
    }

    @ProvidePresenter
    public CreatePlaylistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new CreatePlaylistPresenter(appComponent);
    }
}
