package com.parabola.newtone.presentation.search

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.parabola.domain.model.Album
import com.parabola.domain.model.Artist
import com.parabola.domain.model.Playlist
import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.AlbumAdapter
import com.parabola.newtone.adapter.ArtistAdapter
import com.parabola.newtone.adapter.PlaylistAdapter
import com.parabola.newtone.adapter.TrackAdapter
import com.parabola.newtone.databinding.FragmentSearchBinding
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class SearchFragment : MvpAppCompatFragment(),
    SearchFragmentView {

    @InjectPresenter
    lateinit var presenter: SearchPresenter

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val artistAdapter = ArtistAdapter()
    private val albumAdapter = AlbumAdapter()
    private val trackAdapter = TrackAdapter()
    private val playlistAdapter = PlaylistAdapter()

    private lateinit var itemDecoration: DividerItemDecoration


    companion object {
        fun newInstance() = SearchFragment()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.apply {
            artistsView.adapter = artistAdapter
            albumsView.adapter = albumAdapter
            tracksView.adapter = trackAdapter
            playlistView.adapter = playlistAdapter
            itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

            backBtn.setOnClickListener { presenter.onClickBackButton() }

            val newAlbumItemView = AlbumItemView(
                AlbumViewType.LIST,
                16, 16, 64, 4
            )
            albumAdapter.setViewSettings(newAlbumItemView, 1)

            artistAdapter.setOnItemClickListener { position ->
                searchView.clearFocus()
                val artistId = artistAdapter[position].id
                presenter.onClickArtistItem(artistId)
            }
            albumAdapter.setOnItemClickListener { position ->
                searchView.clearFocus()
                val albumId = albumAdapter[position].id
                presenter.onClickAlbumItem(albumId)
            }
            trackAdapter.setOnItemClickListener { position ->
                searchView.clearFocus()
                presenter.onClickTrackItem(trackAdapter.all, position)
            }
            playlistAdapter.setOnItemClickListener { position ->
                searchView.clearFocus()
                val playlistId = playlistAdapter[position].id
                presenter.onClickPlaylistItem(playlistId)
            }
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    presenter.onQueryTextSubmit(query)
                    searchView.clearFocus()
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isEmpty())
                        presenter.onClearText()
                    return false
                }
            })
            val iconTint = ContextCompat.getColor(requireContext(), R.color.colorActionBarIconTint)
            (searchView.findViewById<View>(R.id.search_close_btn) as ImageView).setColorFilter(
                iconTint,
                PorterDuff.Mode.SRC_ATOP
            )
            val textColor =
                ContextCompat.getColor(requireContext(), R.color.colorNewtoneSecondaryText)
            (searchView.findViewById<View>(R.id.search_src_text) as TextView).setTextColor(
                textColor
            )
            val textHintColor =
                ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
            (searchView.findViewById<View>(R.id.search_src_text) as TextView).setHintTextColor(
                textHintColor
            )

            requireActivity().supportFragmentManager
                .addOnBackStackChangedListener(onBackStackChangedListener)
        }

        return binding.root
    }

    private val onBackStackChangedListener = FragmentManager.OnBackStackChangedListener {
        if (activity?.supportFragmentManager?.primaryNavigationFragment !== this@SearchFragment) {
            binding.searchView.clearFocus()
        }
    }

    override fun onDestroyView() {
        requireActivity().supportFragmentManager
            .removeOnBackStackChangedListener(onBackStackChangedListener)

        val imm = requireActivity()
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)

        _binding = null
        super.onDestroyView()
    }


    @ProvidePresenter
    fun providePresenter(): SearchPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return SearchPresenter(appComponent)
    }


    override fun focusOnSearchView() {
        binding.searchView.requestFocusFromTouch()
    }


    override fun refreshArtists(artists: List<Artist>) {
        artistAdapter.replaceAll(artists)
        binding.artistListHeader.visibility =
            if (artists.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun refreshAlbums(albums: List<Album>) {
        albumAdapter.replaceAll(albums)
        binding.albumListHeader.visibility =
            if (albums.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun refreshTracks(tracks: List<Track>) {
        trackAdapter.replaceAll(tracks)
        binding.trackListHeader.visibility =
            if (tracks.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun refreshPlaylists(playlists: List<Playlist>) {
        playlistAdapter.replaceAll(playlists)
        binding.playlistHeader.visibility =
            if (playlists.isEmpty()) View.GONE else View.VISIBLE
    }


    override fun setTrackItemViewSettings(trackItemView: TrackItemView) {
        trackAdapter.setViewSettings(trackItemView)
    }

    override fun setItemDividerShowing(showed: Boolean) {
        binding.apply {
            artistsView.removeItemDecoration(itemDecoration)
            albumsView.removeItemDecoration(itemDecoration)
            tracksView.removeItemDecoration(itemDecoration)
            playlistView.removeItemDecoration(itemDecoration)

            if (showed) {
                artistsView.addItemDecoration(itemDecoration)
                albumsView.addItemDecoration(itemDecoration)
                tracksView.addItemDecoration(itemDecoration)
                playlistView.addItemDecoration(itemDecoration)
            }
        }
    }


    override fun clearAllLists() {
        artistAdapter.clear()
        albumAdapter.clear()
        trackAdapter.clear()
        playlistAdapter.clear()

        binding.artistListHeader.visibility = View.GONE
        binding.albumListHeader.visibility = View.GONE
        binding.trackListHeader.visibility = View.GONE
        binding.playlistHeader.visibility = View.GONE
    }

    override fun setLoadDataProgressBarVisibility(visible: Boolean) {
        binding.loadDataProgressBarContainer.visibility =
            if (visible) View.VISIBLE else View.GONE
    }

}
