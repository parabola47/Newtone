package com.parabola.newtone.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.CreatePlaylistPresenter;
import com.parabola.newtone.mvp.view.CreatePlaylistView;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static java.util.Objects.requireNonNull;

public final class CreatePlaylistDialog extends MvpAppCompatDialogFragment
        implements CreatePlaylistView {
    private static final String LOG_TAG = CreatePlaylistDialog.class.getSimpleName();

    @BindView(R.id.editTextView) EditText playlistTitleEdt;

    @InjectPresenter CreatePlaylistPresenter presenter;

    public static CreatePlaylistDialog newInstance() {
        return new CreatePlaylistDialog();
    }

    public CreatePlaylistDialog() {
        setRetainInstance(true);
    }


    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View customView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_text_container, null);
        ButterKnife.bind(this, customView);
        playlistTitleEdt.requestFocus();

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.create_playlist_title)
                .setView(customView)
                .setPositiveButton(R.string.dialog_create, null)
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view ->
                    presenter.onClickCreatePlaylist(playlistTitleEdt.getText().toString()));
        });

        return dialog;
    }


    @Override
    public void focusOnInputField() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        requireNonNull(imm).toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    public void onDestroyView() {
        playlistTitleEdt.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        requireNonNull(inputMethodManager).toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        super.onDestroyView();
    }


    @Override
    public void showPlaylistTitleIsEmptyError() {
        String errorText = getString(R.string.error_playlist_title_is_empty);
        playlistTitleEdt.setError(errorText);
    }

    @Override
    public void showPlaylistTitleAlreadyExistsError() {
        String errorText = getString(R.string.toast_playlist_already_exist);
        playlistTitleEdt.setError(errorText);
    }

    @Override
    public void showPlaylistCreatedToast(String playlistTitle) {
        String toastText = getString(R.string.toast_playlist_created, playlistTitle);
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
