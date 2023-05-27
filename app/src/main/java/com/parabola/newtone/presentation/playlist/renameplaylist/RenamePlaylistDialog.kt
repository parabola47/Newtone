package com.parabola.newtone.presentation.playlist.renameplaylist

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.databinding.EditTextContainerBinding
import moxy.MvpAppCompatDialogFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter


private const val PLAYLIST_BUNDLE_KEY = "playlist id"


class RenamePlaylistDialog : MvpAppCompatDialogFragment(),
    RenamePlaylistView {

    @InjectPresenter
    lateinit var presenter: RenamePlaylistPresenter

    private lateinit var playlistTitleEdt: EditText


    init {
        retainInstance = true
    }


    companion object {
        fun newInstance(playlistId: Int) = RenamePlaylistDialog().apply {
            arguments = bundleOf(PLAYLIST_BUNDLE_KEY to playlistId)
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = EditTextContainerBinding
            .inflate(LayoutInflater.from(requireContext()))
        playlistTitleEdt = binding.editTextView
        playlistTitleEdt.requestFocus()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.rename_playlist_title)
            .setView(binding.root)
            .setPositiveButton(R.string.dialog_rename, null)
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()

        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                presenter.onClickRenamePlaylist(playlistTitleEdt.text.toString())
            }
        }

        return dialog
    }

    override fun onDestroyView() {
        playlistTitleEdt.clearFocus()
        val inputMethodManager = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        super.onDestroyView()
    }


    @ProvidePresenter
    fun providePresenter(): RenamePlaylistPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        val playlistId = requireArguments().getInt(PLAYLIST_BUNDLE_KEY)

        return RenamePlaylistPresenter(appComponent, playlistId)
    }


    override fun focusOnInputField() {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    override fun setPlaylistTitle(playlistTitle: String) {
        playlistTitleEdt.setText(playlistTitle)
    }

    override fun setTitleSelected() {
        playlistTitleEdt.setSelection(playlistTitleEdt.length())
        playlistTitleEdt.selectAll()
    }

    override fun showPlaylistTitleAlreadyExistsError() {
        val errorText = getString(R.string.rename_toast_playlist_already_exist)
        playlistTitleEdt.error = errorText
    }

    override fun closeScreen() {
        dismiss()
    }

    override fun showPlaylistTitleIsEmptyError() {
        playlistTitleEdt.error = getString(R.string.error_playlist_title_is_empty)
    }

}
