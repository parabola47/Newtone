package com.parabola.newtone.ui.fragment.playlist

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
import com.parabola.newtone.mvp.presenter.RecentlyAddedPlaylistPresenter
import com.parabola.newtone.mvp.view.RecentlyAddedPlaylistView
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.ui.fragment.Scrollable
import com.parabola.newtone.util.scrollUp
import com.parabola.newtone.util.smoothScrollToTop
import com.parabola.newtone.util.visibleItemsCount
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class RecentlyAddedPlaylistFragment : BaseSwipeToBackFragment(),
    RecentlyAddedPlaylistView, Scrollable {

    @InjectPresenter
    lateinit var presenter: RecentlyAddedPlaylistPresenter

    private var _binding: ListTrackBinding? = null
    private val binding get() = _binding!!

    private val tracklistAdapter = TrackAdapter()
    private lateinit var itemDecoration: DividerItemDecoration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = ListTrackBinding.inflate(inflater, container, false)
        rootBinding.container.addView(binding.root)

        binding.tracksList.adapter = tracklistAdapter
        itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        tracklistAdapter.setOnItemClickListener { position: Int ->
            presenter.onClickTrackItem(tracklistAdapter.all, position)
        }
        tracklistAdapter.setOnItemLongClickListener(this::showTrackContextMenu)

        rootBinding.main.setText(R.string.playlist_recently_added)
        rootBinding.actionBar.setOnClickListener { smoothScrollToTop() }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onClickBackButton() {
        presenter.onClickBack()
    }

    override fun onEndSlidingAnimation() {
        presenter.onEnterSlideAnimationEnded()
    }

    override fun refreshTracks(trackList: List<Track>) {
        tracklistAdapter.replaceAll(trackList)
        val tracksCount = resources
            .getQuantityString(R.plurals.tracks_count, trackList.size, trackList.size)
        rootBinding.additionalInfo.text = tracksCount
    }

    override fun setItemViewSettings(viewSettings: TrackItemView) {
        tracklistAdapter.setViewSettings(viewSettings)
    }

    override fun setItemDividerShowing(showed: Boolean) {
        binding.tracksList.removeItemDecoration(itemDecoration)
        if (showed) binding.tracksList.addItemDecoration(itemDecoration)
    }

    override fun removeTrack(trackId: Int) {
        tracklistAdapter.removeWithCondition { track: Track -> track.id == trackId }
        val tracksCount = resources
            .getQuantityString(
                R.plurals.tracks_count,
                tracklistAdapter.size(),
                tracklistAdapter.size()
            )
        rootBinding.additionalInfo.text = tracksCount
    }

    override fun setCurrentTrack(trackId: Int) {
        tracklistAdapter.setSelectedCondition { track: Track -> track.id == trackId }
    }

    @ProvidePresenter
    fun providePresenter(): RecentlyAddedPlaylistPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return RecentlyAddedPlaylistPresenter(appComponent)
    }

    private fun showTrackContextMenu(position: Int) {
        val selectedTrack = tracklistAdapter[position]
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
                getString(
                    R.string.track_menu_title,
                    selectedTrack.artistName,
                    selectedTrack.title
                )
            )
            .setAdapter(menuAdapter) { _: DialogInterface, which: Int ->
                handleSelectedMenu(
                    menuAdapter.getItem(which),
                    selectedTrack,
                    position
                )
            }
            .create()
        dialog.setOnShowListener { tracklistAdapter.setContextSelected(position) }
        dialog.setOnDismissListener { tracklistAdapter.clearContextSelected() }
        lifecycle.addObserver(DialogDismissLifecycleObserver(dialog))
        dialog.show()
    }

    private fun handleSelectedMenu(menuItem: MenuItem, selectedTrack: Track, itemPosition: Int) {
        when (menuItem.itemId) {
            R.id.play -> {
                val tracks = tracklistAdapter.all
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

    override fun smoothScrollToTop() {
        val fastScrollMinimalPosition =
            (binding.tracksList.layoutManager as LinearLayoutManager).visibleItemsCount() * 3
        binding.tracksList.scrollUp(fastScrollMinimalPosition)
        binding.tracksList.smoothScrollToTop()
    }
}
