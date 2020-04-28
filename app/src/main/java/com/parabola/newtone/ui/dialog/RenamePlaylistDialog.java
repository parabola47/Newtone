package com.parabola.newtone.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.RenamePlaylistPresenter;
import com.parabola.newtone.mvp.view.RenamePlaylistView;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static java.util.Objects.requireNonNull;

public final class RenamePlaylistDialog extends MvpAppCompatDialogFragment
        implements RenamePlaylistView {

    @BindView(R.id.editTextView) EditText playlistTitleEdt;

    @InjectPresenter RenamePlaylistPresenter presenter;

    private static final String PLAYLIST_BUNDLE_KEY = "playlist id";

    public RenamePlaylistDialog() {
        setRetainInstance(true);
    }

    public static RenamePlaylistDialog newInstance(int playlistId) {
        Bundle args = new Bundle();
        args.putInt(PLAYLIST_BUNDLE_KEY, playlistId);

        RenamePlaylistDialog fragment = new RenamePlaylistDialog();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View customView = LayoutInflater.from(requireContext()).inflate(R.layout.edit_text_container, null);
        ButterKnife.bind(this, customView);
        playlistTitleEdt.requestFocus();

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.rename_playlist_title)
                .setView(customView)
                .setPositiveButton(R.string.dialog_rename, null)
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();

        dialog.setOnShowListener(d -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view ->
                    presenter.onClickRenamePlaylist(playlistTitleEdt.getText().toString()));
        });

        return dialog;
    }


    @ProvidePresenter
    RenamePlaylistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        int playlistId = requireArguments().getInt(PLAYLIST_BUNDLE_KEY);

        return new RenamePlaylistPresenter(appComponent, playlistId);
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
    public void setPlaylistTitle(String playlistTitle) {
        playlistTitleEdt.setText(playlistTitle);
    }

    @Override
    public void setTitleSelected() {
        playlistTitleEdt.setSelection(playlistTitleEdt.length());
        playlistTitleEdt.selectAll();
    }

    @Override
    public void showPlaylistTitleAlreadyExistsError() {
        String errorText = getString(R.string.rename_toast_playlist_already_exist);
        playlistTitleEdt.setError(errorText);
    }

    @Override
    public void closeScreen() {
        dismiss();
    }

    @Override
    public void showPlaylistTitleIsEmptyError() {
        playlistTitleEdt.setError(getString(R.string.error_playlist_title_is_empty));
    }
}
