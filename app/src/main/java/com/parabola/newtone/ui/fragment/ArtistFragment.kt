package com.parabola.newtone.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.domain.model.Album
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.AlbumAdapter
import com.parabola.newtone.adapter.ListPopupWindowAdapter
import com.parabola.newtone.databinding.FragmentArtistBinding
import com.parabola.newtone.mvp.presenter.ArtistPresenter
import com.parabola.newtone.mvp.view.ArtistView
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.ui.dialog.SortingDialog
import com.parabola.newtone.util.AndroidTool.calculateAlbumColumnCount
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter


private const val ARTIST_ID_ARG_KEY = "artistId"


class ArtistFragment : BaseSwipeToBackFragment(),
    ArtistView, Sortable, Scrollable {

    @InjectPresenter
    lateinit var presenter: ArtistPresenter

    private var _binding: FragmentArtistBinding? = null
    private val binding get() = _binding!!

    private lateinit var artistNameTxt: TextView
    private lateinit var albumsCountTxt: TextView

    private val albumsAdapter = AlbumAdapter()
    private lateinit var itemDecoration: DividerItemDecoration

    val artistId: Int
        get() = requireArguments().getInt(ARTIST_ID_ARG_KEY)


    companion object {
        fun newInstance(artistId: Int) = ArtistFragment().apply {
            arguments = bundleOf(ARTIST_ID_ARG_KEY to artistId)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentArtistBinding.inflate(inflater, container, false)
        rootBinding.container.addView(binding.root)

        artistNameTxt = rootBinding.main
        albumsCountTxt = rootBinding.additionalInfo

        binding.albumsList.adapter = albumsAdapter
        itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        albumsAdapter.setOnItemClickListener { position ->
            presenter.onAlbumItemClick(albumsAdapter[position].id)
        }
        albumsAdapter.setOnItemLongClickListener(::showAlbumContextMenu)
        binding.allTracksBar.setOnClickListener { presenter.onClickAllTracks() }
        rootBinding.actionBar.setOnClickListener { smoothScrollToTop() }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showAlbumContextMenu(position: Int) {
        val selectedAlbum = albumsAdapter[position]
        val menuAdapter = ListPopupWindowAdapter(requireContext(), R.menu.album_menu)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                getString(R.string.album_menu_title, selectedAlbum.artistName, selectedAlbum.title)
            )
            .setAdapter(menuAdapter) { _, which ->
                handleSelectedMenu(menuAdapter.getItem(which), selectedAlbum)
            }
            .create()
        dialog.setOnShowListener { albumsAdapter.setContextSelected(position) }
        dialog.setOnDismissListener { albumsAdapter.clearContextSelected() }
        lifecycle.addObserver(DialogDismissLifecycleObserver(dialog))
        dialog.show()
    }

    private fun handleSelectedMenu(menuItem: MenuItem, selectedAlbum: Album) {
        when (menuItem.itemId) {
            R.id.shuffle -> presenter.onClickMenuShuffle(selectedAlbum.id)
            R.id.add_to_playlist -> presenter.onClickMenuAddToPlaylist(selectedAlbum.id)
        }
    }

    override fun onClickBackButton() {
        presenter.onClickBack()
    }

    override fun onEndSlidingAnimation() {
        presenter.onEnterSlideAnimationEnded()
    }


    @ProvidePresenter
    fun providePresenter(): ArtistPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        val artistId = requireArguments().getInt(ARTIST_ID_ARG_KEY)

        return ArtistPresenter(appComponent, artistId)
    }


    override fun setArtistName(artistName: String) {
        artistNameTxt.text = artistName
    }

    override fun setTracksCount(tracksCount: Int) {
        val tracksCountFormatted = resources
            .getQuantityString(R.plurals.tracks_count, tracksCount, tracksCount)
        binding.tracksCount.text = tracksCountFormatted
    }

    override fun setAlbumsCount(albumsCount: Int) {
        val albumsCountFormatted = resources
            .getQuantityString(R.plurals.albums_count, albumsCount, albumsCount)
        albumsCountTxt.text = albumsCountFormatted
    }

    override fun refreshAlbums(albums: List<Album>) {
        albumsAdapter.replaceAll(albums)
    }

    override fun setAlbumViewSettings(albumViewSettings: AlbumItemView) {
        val spanCount =
            if (albumViewSettings.viewType == AlbumViewType.GRID)
                calculateAlbumColumnCount(requireActivity())
            else 1
        albumsAdapter.setViewSettings(albumViewSettings, spanCount)
    }

    override fun setItemDividerShowing(showed: Boolean) {
        binding.albumsList.removeItemDecoration(itemDecoration)

        if (showed)
            binding.albumsList.addItemDecoration(itemDecoration)
    }

    override fun getListType(): String {
        return SortingDialog.ARTIST_ALBUMS_SORTING
    }

    override fun smoothScrollToTop() {
        (requireView().findViewById<View>(R.id.artistView) as NestedScrollView).smoothScrollTo(0, 0)
    }

}
