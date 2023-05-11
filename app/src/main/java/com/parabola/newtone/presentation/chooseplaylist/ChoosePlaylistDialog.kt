package com.parabola.newtone.presentation.chooseplaylist

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.domain.model.Playlist
import com.parabola.domain.model.Playlist.TrackItem
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.databinding.DialogPlaylistChooseBinding
import io.reactivex.Observable
import moxy.MvpAppCompatDialogFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

private const val TRACK_IDS_BUNDLE_KEY = "track_ids"

class ChoosePlaylistDialog : MvpAppCompatDialogFragment(),
    ChoosePlaylistView {

    @InjectPresenter
    lateinit var presenter: ChoosePlaylistPresenter

    private var playlistAdapter: PlaylistListViewAdapter? = null


    companion object {
        fun newInstance(vararg trackIds: Int) = ChoosePlaylistDialog().apply {
            arguments = bundleOf(TRACK_IDS_BUNDLE_KEY to trackIds)
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogPlaylistChooseBinding
            .inflate(LayoutInflater.from(requireContext()))

        if (playlistAdapter == null)
            playlistAdapter =
                PlaylistListViewAdapter(requireContext(), R.layout.item_playlist_lv, ArrayList())

        if (binding.playlists.adapter == null)
            binding.playlists.adapter = playlistAdapter
        if (binding.playlists.onItemClickListener == null)
            binding.playlists.onItemClickListener =
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    presenter.onClickPlaylistItem(
                        playlistAdapter!!.getItem(position)!!.id
                    )
                }

        binding.createNewPlaylistButton.setOnClickListener { presenter.onClickCreateNewPlaylist() }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.choose_playlist_dialog_title)
            .setView(binding.root)
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }


    @ProvidePresenter
    fun providePresenter(): ChoosePlaylistPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        val trackIds = requireArguments().getIntArray(TRACK_IDS_BUNDLE_KEY)!!

        return ChoosePlaylistPresenter(appComponent, trackIds)
    }


    override fun refreshPlaylists(playlists: List<Playlist>) {
        playlistAdapter?.let {
            it.clear()
            it.addAll(playlists)
            it.notifyDataSetChanged()
        }

    }

    override fun closeScreen() {
        dismiss()
    }


    private inner class PlaylistListViewAdapter(
        context: Context,
        @LayoutRes resource: Int,
        playlists: List<Playlist>
    ) : ArrayAdapter<Playlist>(context, resource, playlists) {

        override fun getView(
            position: Int,
            convertView: View?,
            parent: ViewGroup,
        ): View {
            val row = convertView
                ?: LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_playlist_lv, parent, false)
            val playlist = getItem(position)!!

            val title = row.findViewById<TextView>(R.id.title)
            val tracksCount = row.findViewById<TextView>(R.id.tracks_count)
            val playlistHasTrackImg = row.findViewById<ImageView>(R.id.playlistHasTrackImg)

            title.text = playlist.title
            val trackCountStr = resources.getQuantityString(
                R.plurals.tracks_count,
                playlist.size(),
                playlist.size()
            )
            tracksCount.text = trackCountStr

            // если в плейлист добавляется один трек, и если этот трек уже есть в плейлисте,
            // то информация о его присутствии отмечается галочкой
            var playlistContainThisTrack = false
            val trackIds = requireArguments().getIntArray(TRACK_IDS_BUNDLE_KEY)!!
            if (trackIds.size == 1) {
                playlistContainThisTrack = Observable.fromIterable(playlist.playlistTracks)
                    .map(TrackItem::getTrackId)
                    .any { trackId -> trackIds[0] == trackId }
                    .blockingGet()
            }
            playlistHasTrackImg.visibility =
                if (playlistContainThisTrack) View.VISIBLE else View.GONE

            return row
        }
    }

}
