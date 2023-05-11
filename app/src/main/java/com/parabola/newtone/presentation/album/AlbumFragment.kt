package com.parabola.newtone.presentation.album

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.ListPopupWindowAdapter
import com.parabola.newtone.adapter.TrackAdapter
import com.parabola.newtone.databinding.ListTrackBinding
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.ui.dialog.SortingDialog
import com.parabola.newtone.ui.fragment.Scrollable
import com.parabola.newtone.ui.fragment.Sortable
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter


private const val ALBUM_ID_ARG_KEY = "albumId"


class AlbumFragment : BaseSwipeToBackFragment(),
    AlbumView, Sortable, Scrollable {

    @InjectPresenter
    lateinit var presenter: AlbumPresenter

    private var _binding: ListTrackBinding? = null
    private val binding get() = _binding!!


    private lateinit var albumTitleTxt: TextView
    private lateinit var artistNameTxt: TextView
    private lateinit var albumCover: ShapeableImageView


    private val tracksAdapter = TrackAdapter()
    private lateinit var itemDecoration: DividerItemDecoration

    val albumId: Int
        get() = requireArguments().getInt(ALBUM_ID_ARG_KEY)


    companion object {
        fun newInstance(albumId: Int) = AlbumFragment().apply {
            arguments = bundleOf(ALBUM_ID_ARG_KEY to albumId)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = ListTrackBinding.inflate(inflater, container, false)
        rootBinding.container.addView(binding.root)

        albumTitleTxt = rootBinding.main
        artistNameTxt = rootBinding.additionalInfo
        albumCover = rootBinding.image

        albumCover.visibility = View.VISIBLE
        albumCover.shapeAppearanceModel = albumCover.shapeAppearanceModel.toBuilder()
            .setAllCorners(
                CornerFamily.ROUNDED,
                resources.getDimension(R.dimen.album_fragment_cover_corner_size)
            )
            .build()

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


    @ProvidePresenter
    fun providePresenter(): AlbumPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        val albumId = requireArguments().getInt(ALBUM_ID_ARG_KEY)

        return AlbumPresenter(appComponent, albumId)
    }


    override fun setAlbumTitle(albumTitle: String) {
        albumTitleTxt.text = albumTitle
    }

    override fun setAlbumArtist(artistName: String) {
        artistNameTxt.text = artistName
    }

    override fun setAlbumArt(artCover: Any?) {
        if (artCover != null) albumCover.setImageBitmap(artCover as Bitmap)
        else albumCover.setImageResource(R.drawable.album_default)
    }

    override fun refreshTracks(tracks: List<Track>) {
        tracksAdapter.replaceAll(tracks)
    }

    override fun setItemViewSettings(itemViewSettings: TrackItemView) {
        tracksAdapter.setViewSettings(itemViewSettings)
    }

    override fun setItemDividerShowing(showed: Boolean) {
        binding.tracksList.removeItemDecoration(itemDecoration)

        if (showed)
            binding.tracksList.addItemDecoration(itemDecoration)
    }

    override fun setCurrentTrack(trackId: Int) {
        tracksAdapter.setSelectedCondition { track: Track -> track.id == trackId }
    }


    override val listType = SortingDialog.ALBUM_TRACKS_SORTING


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
