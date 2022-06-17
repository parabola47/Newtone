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
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import com.parabola.domain.utils.TracklistTool
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.ListPopupWindowAdapter
import com.parabola.newtone.adapter.TrackAdapter
import com.parabola.newtone.databinding.ListTrackBinding
import com.parabola.newtone.mvp.presenter.FavouritesPlaylistPresenter
import com.parabola.newtone.mvp.view.FavouritesPlaylistView
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.ui.fragment.Scrollable
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class FavoritesPlaylistFragment : BaseSwipeToBackFragment(),
    FavouritesPlaylistView, Scrollable {

    @InjectPresenter
    lateinit var presenter: FavouritesPlaylistPresenter

    private var _binding: ListTrackBinding? = null
    private val binding get() = _binding!!

    private val tracklistAdapter = TrackAdapter()
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

        rootBinding.main.setText(R.string.playlist_favourites)

        initDragSwitcherButton()

        binding.tracksList.adapter = tracklistAdapter
        itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        tracklistAdapter.setOnItemClickListener { position: Int ->
            presenter.onClickTrackItem(tracklistAdapter.all, position)
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

        val backgroundTintList = ContextCompat
            .getColorStateList(requireContext(), R.color.drag_button_background_color_selector)
        ViewCompat.setBackgroundTintList(dragSwitcherButton, backgroundTintList)
        val imageTintList = ContextCompat
            .getColorStateList(requireContext(), R.color.colorNewtoneIconTint)
        ImageViewCompat.setImageTintList(dragSwitcherButton, imageTintList)
    }

    override fun onClickBackButton() {
        presenter.onClickBack()
    }

    override fun onEndSlidingAnimation() {
        presenter.onEnterSlideAnimationEnded()
    }

    override fun setPlaylistChangerActivation(activate: Boolean) {
        dragSwitcherButton.isSelected = activate
        tracklistAdapter.setMoveItemIconVisibility(activate)

        if (activate) {
            tracklistAdapter.setOnItemLongClickListener(null)
            tracklistAdapter.setOnSwipeItemListener { position: Int ->
                val removedTrackId = tracklistAdapter[position].id
                tracklistAdapter.remove(position)
                presenter.onRemoveItem(removedTrackId)
            }
            tracklistAdapter.setOnMoveItemListener(presenter::onMoveItem)
        } else {
            tracklistAdapter.setOnItemLongClickListener(this::showTrackContextMenu)
            tracklistAdapter.setOnSwipeItemListener(null)
            tracklistAdapter.setOnMoveItemListener(null)
        }
    }

    override fun refreshTracks(trackList: List<Track>) {
        val tracksCount = resources
            .getQuantityString(R.plurals.tracks_count, trackList.size, trackList.size)
        rootBinding.additionalInfo.text = tracksCount
        if (!TracklistTool.isTracklistsIdentical(trackList, tracklistAdapter.all)) {
            tracklistAdapter.replaceAll(trackList)
        }
    }

    override fun setItemViewSettings(viewSettings: TrackItemView) {
        tracklistAdapter.setViewSettings(viewSettings)
    }

    override fun setItemDividerShowing(showed: Boolean) {
        binding.tracksList.removeItemDecoration(itemDecoration)
        if (showed) binding.tracksList.addItemDecoration(itemDecoration)
    }

    override fun setCurrentTrack(trackId: Int) {
        tracklistAdapter.setSelectedCondition { track: Track -> track.id == trackId }
    }

    @ProvidePresenter
    fun providePresenter(): FavouritesPlaylistPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return FavouritesPlaylistPresenter(appComponent)
    }

    private fun showTrackContextMenu(position: Int) {
        val selectedTrack = tracklistAdapter[position]
        val menuAdapter = ListPopupWindowAdapter(requireContext(), R.menu.track_menu)

        menuAdapter.setMenuVisibility { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.add_to_favorites, R.id.remove_from_playlist -> return@setMenuVisibility false
                else -> return@setMenuVisibility true
            }
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                getString(R.string.track_menu_title, selectedTrack.artistName, selectedTrack.title)
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
            R.id.remove_from_favourites -> presenter.onClickMenuRemoveFromFavourites(selectedTrack.id)
            R.id.share_track -> presenter.onClickMenuShareTrack(selectedTrack)
            R.id.additional_info -> presenter.onClickMenuAdditionalInfo(selectedTrack.id)
            R.id.delete_track -> presenter.onClickMenuDeleteTrack(selectedTrack.id)
        }
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
