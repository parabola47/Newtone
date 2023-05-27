package com.parabola.newtone.presentation.main.tracks

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.ListPopupWindowAdapter
import com.parabola.newtone.adapter.TrackAdapter
import com.parabola.newtone.databinding.ListTrackBinding
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.presentation.SortingDialog
import com.parabola.newtone.ui.fragment.Scrollable
import com.parabola.newtone.ui.fragment.Sortable
import com.parabola.newtone.util.scrollUp
import com.parabola.newtone.util.smoothScrollToTop
import com.parabola.newtone.util.visibleItemsCount
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class TabTrackFragment : MvpAppCompatFragment(),
    TabTrackView, Sortable, Scrollable {

    @InjectPresenter
    lateinit var presenter: TabTrackPresenter

    private var _binding: ListTrackBinding? = null
    private val binding get() = _binding!!

    private val tracksAdapter = TrackAdapter()
    private lateinit var itemDecoration: DividerItemDecoration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ListTrackBinding.inflate(inflater, container, false)

        binding.tracksList.adapter = tracksAdapter
        itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        tracksAdapter.setOnItemClickListener { position: Int ->
            presenter.onClickTrackItem(tracksAdapter.all, position)
        }
        tracksAdapter.setOnItemLongClickListener { position: Int -> showTrackContextMenu(position) }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun scrollToCurrentTrack() {
        val selectedTrackPosition = tracksAdapter.selectedPosition
        if (selectedTrackPosition.isPresent) {
            binding.tracksList.scrollToPosition(selectedTrackPosition.asInt)
        }
    }

    private fun showTrackContextMenu(position: Int) {
        val selectedTrack = tracksAdapter[position]
        val menuAdapter = ListPopupWindowAdapter(requireContext(), R.menu.track_menu)

        menuAdapter.setMenuVisibility { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.add_to_favorites -> return@setMenuVisibility !selectedTrack.isFavourite
                R.id.remove_from_favourites -> return@setMenuVisibility selectedTrack.isFavourite
                R.id.remove_from_playlist -> return@setMenuVisibility false
                else -> return@setMenuVisibility true
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                getString(R.string.track_menu_title, selectedTrack.artistName, selectedTrack.title)
            )
            .setAdapter(menuAdapter) { _: DialogInterface?, which: Int ->
                handleSelectedMenu(menuAdapter.getItem(which), selectedTrack, position)
            }
            .create()
        dialog.setOnShowListener { tracksAdapter.setContextSelected(position) }
        dialog.setOnDismissListener { tracksAdapter.clearContextSelected() }
        lifecycle.addObserver(DialogDismissLifecycleObserver(dialog))
        dialog.show()
    }

    private fun handleSelectedMenu(menuItem: MenuItem, selectedTrack: Track, itemPosition: Int) {
        when (menuItem.itemId) {
            R.id.play -> {
                val tracks = tracksAdapter.all
                presenter.onClickMenuPlay(tracks, itemPosition)
            }
            R.id.add_to_playlist -> presenter.onClickMenuAddToPlaylist(selectedTrack.id)
            R.id.add_to_favorites -> presenter.onClickMenuAddToFavourites(selectedTrack.id)
            R.id.remove_from_favourites -> presenter.onClickMenuRemoveFromFavourites(selectedTrack.id)
            R.id.share_track -> presenter.onClickMenuShareTrack(selectedTrack)
            R.id.additional_info -> presenter.onClickMenuAdditionalInfo(selectedTrack.id)
            R.id.delete_track -> presenter.onClickMenuDeleteTrack(selectedTrack.id)
        }
    }

    @ProvidePresenter
    fun providePresenter(): TabTrackPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return TabTrackPresenter(appComponent)
    }

    override fun refreshTracks(tracks: List<Track>) {
        tracksAdapter.replaceAll(tracks)
    }

    override fun setItemViewSettings(viewSettings: TrackItemView) {
        tracksAdapter.setViewSettings(viewSettings)
    }

    override fun setItemDividerShowing(showed: Boolean) {
        binding.tracksList.removeItemDecoration(itemDecoration)

        if (showed) binding.tracksList.addItemDecoration(itemDecoration)
    }

    override fun setCurrentTrack(trackId: Int) {
        tracksAdapter.setSelectedCondition { track: Track -> track.id == trackId }
    }

    override fun setSectionShowing(enable: Boolean) {
        tracksAdapter.setSectionEnabled(enable)
    }

    override fun removeTrack(trackId: Int) {
        tracksAdapter.removeWithCondition { track: Track -> track.id == trackId }
    }


    override val listType = SortingDialog.ALL_TRACKS_SORTING


    override fun smoothScrollToTop() {
        val fastScrollMinimalPosition =
            (binding.tracksList.layoutManager as LinearLayoutManager).visibleItemsCount() * 3
        binding.tracksList.scrollUp(fastScrollMinimalPosition)
        binding.tracksList.smoothScrollToTop()
    }

}
