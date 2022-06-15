package com.parabola.newtone.ui.fragment.start

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.domain.model.Album
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.AlbumAdapter
import com.parabola.newtone.adapter.ListPopupWindowAdapter
import com.parabola.newtone.databinding.FragmentTabAlbumBinding
import com.parabola.newtone.mvp.presenter.TabAlbumPresenter
import com.parabola.newtone.mvp.view.TabAlbumView
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.ui.dialog.SortingDialog
import com.parabola.newtone.ui.fragment.Scrollable
import com.parabola.newtone.ui.fragment.Sortable
import com.parabola.newtone.util.AndroidTool
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class TabAlbumFragment : MvpAppCompatFragment(),
    TabAlbumView, Sortable, Scrollable {

    @InjectPresenter
    lateinit var presenter: TabAlbumPresenter

    private var _binding: FragmentTabAlbumBinding? = null
    private val binding get() = _binding!!

    private val albumsAdapter = AlbumAdapter()
    private lateinit var itemDecoration: DividerItemDecoration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabAlbumBinding.inflate(inflater, container, false)

        binding.albumsList.adapter = albumsAdapter
        itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        albumsAdapter.setOnItemClickListener { position: Int ->
            presenter.onItemClick(
                albumsAdapter[position].id
            )
        }
        albumsAdapter.setOnItemLongClickListener { position: Int -> showAlbumContextMenu(position) }

        return binding.root
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
            .setAdapter(menuAdapter) { _: DialogInterface, which: Int ->
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

    @ProvidePresenter
    fun providePresenter(): TabAlbumPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return TabAlbumPresenter(appComponent)
    }

    fun scrollTo(albumId: Int) {
        for (i in 0 until albumsAdapter.size()) {
            if (albumsAdapter[i].id == albumId) {
                binding.albumsList.scrollToPosition(i)
                return
            }
        }
    }

    override fun refreshAlbums(albums: List<Album>) {
        albumsAdapter.replaceAll(albums)
    }

    override fun setSectionShowing(enable: Boolean) {
        albumsAdapter.setSectionEnabled(enable)
    }

    override fun setAlbumViewSettings(viewSettings: AlbumItemView) {
        val spanCount =
            if (viewSettings.viewType == AlbumViewType.GRID)
                AndroidTool.calculateAlbumColumnCount(requireActivity())
            else 1
        albumsAdapter.setViewSettings(viewSettings, spanCount)
    }

    override fun setItemDividerShowing(showed: Boolean) {
        binding.albumsList.removeItemDecoration(itemDecoration)

        if (showed) binding.albumsList.addItemDecoration(itemDecoration)
    }

    override fun getListType(): String = SortingDialog.ALL_ALBUMS_SORTING

    override fun smoothScrollToTop() {
        val layoutManager = binding.albumsList.layoutManager as LinearLayoutManager
        val firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
        val screenItemsCount =
            layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition()

        if (firstItemPosition > screenItemsCount * 3) {
            binding.albumsList.scrollToPosition(screenItemsCount * 3)
        }

        binding.albumsList.smoothScrollToPosition(0)
    }

}
