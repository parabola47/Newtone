package com.parabola.newtone.ui.fragment.playlist

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import com.parabola.domain.utils.TracklistTool.isTracklistsIdentical
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.ListPopupWindowAdapter
import com.parabola.newtone.adapter.TrackAdapter
import com.parabola.newtone.databinding.ListTrackBinding
import com.parabola.newtone.mvp.presenter.PlaylistPresenter
import com.parabola.newtone.mvp.view.PlaylistView
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.ui.fragment.Scrollable
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class PlaylistFragment : BaseSwipeToBackFragment(),
    PlaylistView, Scrollable {

    @InjectPresenter
    lateinit var presenter: PlaylistPresenter

    private var _binding: ListTrackBinding? = null
    private val binding get() = _binding!!

    private val tracksAdapter = TrackAdapter()
    private lateinit var itemDecoration: DividerItemDecoration

    private lateinit var dragSwitcherButton: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = ListTrackBinding.inflate(inflater, container, false)
        rootBinding.container.addView(binding.root)

        initDragSwitcherButton()

        binding.tracksList.adapter = tracksAdapter
        itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        tracksAdapter.setOnItemClickListener { position: Int ->
            presenter.onClickTrackItem(tracksAdapter.all, position)
        }
        rootBinding.actionBar.setOnClickListener { smoothScrollToTop() }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun initDragSwitcherButton() {
        dragSwitcherButton = AppCompatImageButton(requireContext())
        dragSwitcherButton.setImageResource(R.drawable.ic_drag)
        val imageSize = resources.getDimension(R.dimen.playlist_fragment_drag_switcher_size).toInt()
        dragSwitcherButton.layoutParams = LinearLayout.LayoutParams(imageSize, imageSize)
        rootBinding.actionBar.addView(dragSwitcherButton)
        dragSwitcherButton.setOnClickListener {
            if (binding.tracksList.scrollState == RecyclerView.SCROLL_STATE_IDLE)
                presenter.onClickDragSwitcher()
        }

        val backgroundTintList = ContextCompat.getColorStateList(
            requireContext(),
            R.color.drag_button_background_color_selector
        )
        ViewCompat.setBackgroundTintList(dragSwitcherButton, backgroundTintList)
        val imageTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.colorNewtoneIconTint)
        ImageViewCompat.setImageTintList(dragSwitcherButton, imageTintList)
    }

    private fun showTrackContextMenu(position: Int) {
        val selectedTrack = tracksAdapter[position]
        val menuAdapter = ListPopupWindowAdapter(requireContext(), R.menu.track_menu)

        menuAdapter.setMenuVisibility { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.add_to_favorites -> return@setMenuVisibility !selectedTrack.isFavourite
                R.id.remove_from_favourites -> return@setMenuVisibility selectedTrack.isFavourite
                R.id.add_to_playlist -> return@setMenuVisibility false
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
            R.id.remove_from_playlist -> presenter.onClickMenuRemoveFromCurrentPlaylist(
                selectedTrack.id
            )
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

    @ProvidePresenter
    fun providePresenter(): PlaylistPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        val playlistId = requireArguments().getInt(SELECTED_PLAYLIST_ID)

        return PlaylistPresenter(appComponent, playlistId)
    }

    override fun setPlaylistChangerActivation(activate: Boolean) {
        dragSwitcherButton.isSelected = activate
        tracksAdapter.setMoveItemIconVisibility(activate)

        if (activate) {
            tracksAdapter.setOnItemLongClickListener(null)
            tracksAdapter.setOnSwipeItemListener { position: Int ->
                val removedTrackId = tracksAdapter[position].id
                tracksAdapter.remove(position)
                presenter.onRemoveItem(removedTrackId)
            }
            tracksAdapter.setOnMoveItemListener(presenter::onMoveItem)
        } else {
            tracksAdapter.setOnItemLongClickListener(this::showTrackContextMenu)
            tracksAdapter.setOnSwipeItemListener(null)
            tracksAdapter.setOnMoveItemListener(null)
        }
    }

    override fun setTracksCount(playlistSize: Int) {
        rootBinding.additionalInfo.text =
            resources.getQuantityString(R.plurals.tracks_count, playlistSize, playlistSize)
    }

    override fun setPlaylistTitle(playlistTitle: String) {
        rootBinding.main.text = playlistTitle
    }

    override fun refreshTracks(tracks: List<Track>) {
        if (!isTracklistsIdentical(tracks, tracksAdapter.all)) {
            tracksAdapter.replaceAll(tracks)
        }
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

    companion object {
        private const val SELECTED_PLAYLIST_ID = "playlist id"

        //todo убрать аннотацию, когда MainRouterImpl будет переведён на котлин
        @JvmStatic
        fun newInstance(playlistId: Int) =
            PlaylistFragment().apply {
                arguments = bundleOf(Pair(SELECTED_PLAYLIST_ID, playlistId))
            }
    }
}
