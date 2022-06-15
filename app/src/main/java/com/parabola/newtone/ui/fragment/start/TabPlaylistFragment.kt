package com.parabola.newtone.ui.fragment.start

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.domain.model.Playlist
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.ListPopupWindowAdapter
import com.parabola.newtone.adapter.PlaylistAdapter
import com.parabola.newtone.databinding.FragmentTabPlaylistBinding
import com.parabola.newtone.databinding.ItemSystemPlaylistBinding
import com.parabola.newtone.mvp.presenter.TabPlaylistPresenter
import com.parabola.newtone.mvp.view.TabPlaylistView
import com.parabola.newtone.ui.base.BaseDialogFragment
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.ui.fragment.Scrollable
import com.parabola.newtone.ui.fragment.start.TabPlaylistFragment.SystemPlaylistAdapter.SystemPlaylistViewHolder
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class TabPlaylistFragment : MvpAppCompatFragment(),
    TabPlaylistView, Scrollable {

    @InjectPresenter
    lateinit var presenter: TabPlaylistPresenter

    private var _binding: FragmentTabPlaylistBinding? = null
    private val binding get() = _binding!!

    private val playlistAdapter = PlaylistAdapter()
    private val sysPlaylistAdapter = SystemPlaylistAdapter()

    private var itemDecoration: DividerItemDecoration? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabPlaylistBinding.inflate(inflater, container, false)

        binding.playlists.adapter = playlistAdapter
        binding.sysPlaylists.adapter = sysPlaylistAdapter
        itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        playlistAdapter.setOnItemClickListener { position: Int ->
            presenter.onClickPlaylistItem(playlistAdapter[position].id)
        }
        playlistAdapter.setOnItemLongClickListener { position: Int ->
            showPlaylistContextMenu(position)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showPlaylistContextMenu(position: Int) {
        val playlist = playlistAdapter[position]
        val menuAdapter = ListPopupWindowAdapter(requireContext(), R.menu.playlist_menu)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(playlist.title)
            .setAdapter(menuAdapter) { _: DialogInterface?, which: Int ->
                handleSelectedMenu(menuAdapter.getItem(which), playlist.id)
            }
            .create()
        dialog.setOnShowListener { playlistAdapter.setContextSelected(position) }
        dialog.setOnDismissListener { d: DialogInterface? -> playlistAdapter.clearContextSelected() }
        lifecycle.addObserver(DialogDismissLifecycleObserver(dialog))
        dialog.show()
    }

    private fun handleSelectedMenu(menuItem: MenuItem, selectedPlaylistId: Int) {
        when (menuItem.itemId) {
            R.id.rename -> presenter.onClickMenuRename(selectedPlaylistId)
            R.id.shuffle -> presenter.onClickMenuShuffle(selectedPlaylistId)
            R.id.delete -> {
                val dialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.delete_playlist_title)
                    .setMessage(R.string.delete_playlist_desc)
                    .setPositiveButton(R.string.dialog_delete) { _: DialogInterface?, _: Int ->
                        presenter.onClickMenuDelete(selectedPlaylistId)
                    }
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .create()
                val dialogFragment: DialogFragment = BaseDialogFragment.build(dialog)
                dialogFragment.show(requireActivity().supportFragmentManager, null)
            }
        }
    }

    @ProvidePresenter
    fun providePresenter(): TabPlaylistPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return TabPlaylistPresenter(appComponent)
    }

    override fun refreshPlaylists(playlists: List<Playlist>) {
        playlistAdapter.replaceAll(playlists)
        binding.myPlaylistsTxt.visibility = if (playlists.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun setItemDividerShowing(showed: Boolean) {
        binding.sysPlaylists.removeItemDecoration(itemDecoration!!)
        binding.playlists.removeItemDecoration(itemDecoration!!)

        if (showed) {
            binding.sysPlaylists.addItemDecoration(itemDecoration!!)
            binding.playlists.addItemDecoration(itemDecoration!!)
        }
    }

    override fun smoothScrollToTop() {
        (requireView() as NestedScrollView).smoothScrollTo(0, 0)
    }

    inner class SystemPlaylistAdapter : RecyclerView.Adapter<SystemPlaylistViewHolder>() {
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): SystemPlaylistViewHolder {
            val binding = ItemSystemPlaylistBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            return SystemPlaylistViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SystemPlaylistViewHolder, position: Int) {
            when (holder.adapterPosition) {
                0 -> {
                    holder.binding.icon.setImageResource(R.drawable.ic_favourite_white)
                    holder.binding.title.setText(R.string.playlist_favourites)
                    holder.binding.root.setOnClickListener { presenter.onClickFavourites() }
                }
                1 -> {
                    holder.binding.icon.setImageResource(R.drawable.ic_recent_add)
                    holder.binding.title.setText(R.string.playlist_recently_added)
                    holder.binding.root.setOnClickListener { presenter.onClickRecentlyAdded() }
                }
                2 -> {
                    holder.binding.icon.setImageResource(R.drawable.ic_queue)
                    holder.binding.title.setText(R.string.playlist_queue)
                    holder.binding.root.setOnClickListener { presenter.onClickQueue() }
                }
                3 -> {
                    holder.binding.icon.setImageResource(R.drawable.ic_folder)
                    holder.binding.title.setText(R.string.playlist_folders)
                    holder.binding.root.setOnClickListener { presenter.onClickFolders() }
                }
                else -> {}
            }
        }

        override fun getItemCount(): Int {
            return 4
        }

        inner class SystemPlaylistViewHolder(val binding: ItemSystemPlaylistBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

}
