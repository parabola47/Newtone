package com.parabola.newtone.presentation.playlist.createplaylist

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.databinding.EditTextContainerBinding
import moxy.MvpAppCompatDialogFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class CreatePlaylistDialog : MvpAppCompatDialogFragment(),
    CreatePlaylistView {

    @InjectPresenter
    lateinit var presenter: CreatePlaylistPresenter

    private lateinit var playlistTitleEdt: EditText


    companion object {
        fun newInstance() = CreatePlaylistDialog()
    }


    init {
        retainInstance = true
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = EditTextContainerBinding
            .inflate(LayoutInflater.from(requireContext()))
        playlistTitleEdt = binding.editTextView
        playlistTitleEdt.requestFocus()

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.create_playlist_title)
            .setView(binding.root)
            .setPositiveButton(R.string.dialog_create, null)
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
        dialog.setOnShowListener {
            val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                presenter.onClickCreatePlaylist(playlistTitleEdt.text.toString())
            }
        }

        return dialog
    }

    override fun focusOnInputField() {
        val imm = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    override fun onDestroyView() {
        playlistTitleEdt.clearFocus()
        val inputMethodManager = requireContext()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        super.onDestroyView()
    }


    override fun showPlaylistTitleIsEmptyError() {
        val errorText = getString(R.string.error_playlist_title_is_empty)
        playlistTitleEdt.error = errorText
    }

    override fun showPlaylistTitleAlreadyExistsError() {
        val errorText = getString(R.string.toast_playlist_already_exist)
        playlistTitleEdt.error = errorText
    }

    override fun closeScreen() {
        dismiss()
    }


    @ProvidePresenter
    fun providePresenter(): CreatePlaylistPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return CreatePlaylistPresenter(appComponent)
    }

}
