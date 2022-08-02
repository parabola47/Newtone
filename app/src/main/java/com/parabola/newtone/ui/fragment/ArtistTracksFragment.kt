package com.parabola.newtone.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.parabola.newtone.mvp.presenter.ArtistTracksPresenter
import com.parabola.newtone.mvp.view.ArtistTracksView
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.ui.dialog.SortingDialog
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class ArtistTracksFragment : BaseSwipeToBackFragment(),
    ArtistTracksView, Sortable, Scrollable {

    @InjectPresenter
    lateinit var presenter: ArtistTracksPresenter

    private var _binding: ListTrackBinding? = null
    private val binding get() = _binding!!


    private lateinit var artistTxt: TextView
    private lateinit var tracksCountTxt: TextView


    private val tracksAdapter = TrackAdapter()
    private lateinit var itemDecoration: DividerItemDecoration


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = ListTrackBinding.inflate(inflater, container, false)
        rootBinding.container.addView(binding.root)

        artistTxt = rootBinding.main
        tracksCountTxt = rootBinding.additionalInfo

        binding.tracksList.adapter = tracksAdapter
        itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        tracksAdapter.setOnItemClickListener { position ->
            presenter.onClickTrackItem(tracksAdapter.all, position)
        }
        tracksAdapter.setOnItemLongClickListener(::showTrackContextMenu)
        rootBinding.actionBar.setOnClickListener { smoothScrollToTop() }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showTrackContextMenu(position: Int) {
        val selectedTrack = tracksAdapter[position]
        val menuAdapter = ListPopupWindowAdapter(requireContext(), R.menu.track_menu)
        menuAdapter.setMenuVisibility { menuItem ->
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
            .setAdapter(menuAdapter) { _, which ->
                handleSelectedMenu(
                    menuAdapter.getItem(which),
                    selectedTrack,
                    position
                )
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

    override fun onClickBackButton() {
        presenter.onClickBack()
    }

    override fun onEndSlidingAnimation() {
        presenter.onEnterSlideAnimationEnded()
    }

    override fun refreshTracks(tracks: List<Track>) {
        tracksAdapter.replaceAll(tracks)
    }

    override fun setItemViewSettings(viewSettings: TrackItemView) {
        tracksAdapter.setViewSettings(viewSettings)
    }

    override fun setItemDividerShowing(showed: Boolean) {
        binding.tracksList.removeItemDecoration(itemDecoration)

        if (showed)
            binding.tracksList.addItemDecoration(itemDecoration)
    }

    override fun setSectionShowing(enable: Boolean) {
        tracksAdapter.setSectionEnabled(enable)
    }

    override fun setArtistName(artistName: String) {
        artistTxt.text = artistName
    }

    override fun setTracksCountTxt(tracksCountStr: String) {
        tracksCountTxt.text = tracksCountStr
    }

    override fun setCurrentTrack(trackId: Int) {
        tracksAdapter.setSelectedCondition { track: Track -> track.id == trackId }
    }


    @ProvidePresenter
    fun providePresenter(): ArtistTracksPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        val artistId = requireArguments().getInt("artistId")
        return ArtistTracksPresenter(appComponent, artistId)
    }


    override fun getListType(): String {
        return SortingDialog.ARTIST_TRACKS_SORTING
    }

    override fun smoothScrollToTop() {
        val layoutManager = binding.tracksList.layoutManager as LinearLayoutManager
        val firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        val screenItemsCount =
            layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition()

        if (firstItemPosition > screenItemsCount * 3) {
            binding.tracksList.scrollToPosition(screenItemsCount * 3)
        }

        binding.tracksList.smoothScrollToPosition(0)
    }

}
