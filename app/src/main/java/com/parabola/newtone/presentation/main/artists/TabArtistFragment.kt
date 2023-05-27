package com.parabola.newtone.presentation.main.artists

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.domain.model.Artist
import com.parabola.domain.settings.ViewSettingsInteractor.ArtistItemView
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.ArtistAdapter
import com.parabola.newtone.adapter.ListPopupWindowAdapter
import com.parabola.newtone.databinding.FragmentTabArtistBinding
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

class TabArtistFragment : MvpAppCompatFragment(),
    TabArtistView, Sortable, Scrollable {

    @InjectPresenter
    lateinit var presenter: TabArtistPresenter

    private var _binding: FragmentTabArtistBinding? = null
    private val binding get() = _binding!!

    private val artistsAdapter = ArtistAdapter()
    private lateinit var itemDecoration: DividerItemDecoration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabArtistBinding.inflate(inflater, container, false)

        binding.artistsList.adapter = artistsAdapter
        itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        artistsAdapter.setOnItemClickListener { position: Int ->
            presenter.onItemClick(artistsAdapter[position].id)
        }
        artistsAdapter.setOnItemLongClickListener { position: Int -> showArtistContextMenu(position) }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showArtistContextMenu(position: Int) {
        val selectedArtist = artistsAdapter[position]
        val menuAdapter = ListPopupWindowAdapter(requireContext(), R.menu.artist_menu)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(selectedArtist.name)
            .setAdapter(menuAdapter) { _: DialogInterface, which: Int ->
                handleSelectedMenu(menuAdapter.getItem(which), selectedArtist)
            }
            .create()
        dialog.setOnShowListener { artistsAdapter.setContextSelected(position) }
        dialog.setOnDismissListener { artistsAdapter.clearContextSelected() }
        lifecycle.addObserver(DialogDismissLifecycleObserver(dialog))
        dialog.show()
    }

    private fun handleSelectedMenu(menuItem: MenuItem, selectedArtist: Artist) {
        when (menuItem.itemId) {
            R.id.shuffle -> presenter.onClickMenuShuffle(selectedArtist.id)
            R.id.add_to_playlist -> presenter.onClickMenuAddToPlaylist(selectedArtist.id)
        }
    }

    @ProvidePresenter
    fun providePresenter(): TabArtistPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return TabArtistPresenter(appComponent)
    }

    fun scrollTo(artistId: Int) {
        for (i in 0 until artistsAdapter.size()) {
            if (artistsAdapter[i].id == artistId) {
                binding.artistsList.scrollToPosition(i)
                return
            }
        }
    }

    override fun refreshArtists(artists: List<Artist>) {
        artistsAdapter.replaceAll(artists)
    }

    override fun setItemViewSettings(viewSettings: ArtistItemView) {
        artistsAdapter.setViewSettings(viewSettings)
    }

    override fun setItemDividerShowing(showed: Boolean) {
        binding.artistsList.removeItemDecoration(itemDecoration)
        if (showed) binding.artistsList.addItemDecoration(itemDecoration)
    }

    override fun setSectionShowing(enable: Boolean) {
        artistsAdapter.setSectionEnabled(enable)
    }

    override val listType = SortingDialog.ALL_ARTISTS_SORTING

    override fun smoothScrollToTop() {
        val fastScrollMinimalPosition =
            (binding.artistsList.layoutManager as LinearLayoutManager).visibleItemsCount() * 3
        binding.artistsList.scrollUp(fastScrollMinimalPosition)
        binding.artistsList.smoothScrollToTop()
    }
}
