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
import com.parabola.domain.model.Folder
import com.parabola.domain.utils.TracklistTool
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.FolderAdapter
import com.parabola.newtone.adapter.ListPopupWindowAdapter
import com.parabola.newtone.databinding.FragmentFoldersListBinding
import com.parabola.newtone.mvp.presenter.FoldersListPresenter
import com.parabola.newtone.mvp.view.FoldersListView
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.ui.fragment.Scrollable
import com.parabola.newtone.util.scrollUp
import com.parabola.newtone.util.smoothScrollToTop
import com.parabola.newtone.util.visibleItemsCount
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class FoldersListFragment : BaseSwipeToBackFragment(),
    FoldersListView, Scrollable {

    @InjectPresenter
    lateinit var presenter: FoldersListPresenter

    private var _binding: FragmentFoldersListBinding? = null
    private val binding get() = _binding!!

    private val foldersAdapter = FolderAdapter()
    private lateinit var itemDecoration: DividerItemDecoration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentFoldersListBinding.inflate(inflater, container, false)
        rootBinding.container.addView(binding.root)

        rootBinding.main.setText(R.string.playlist_folders)

        binding.folderList.adapter = foldersAdapter
        itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        foldersAdapter.setOnItemClickListener { position: Int ->
            presenter.onClickFolderItem(foldersAdapter[position].absolutePath)
        }
        foldersAdapter.setOnItemLongClickListener(this::showFolderContextMenu)
        rootBinding.actionBar.setOnClickListener { smoothScrollToTop() }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showFolderContextMenu(position: Int) {
        val selectedFolder = foldersAdapter[position]
        val menuAdapter = ListPopupWindowAdapter(requireContext(), R.menu.folder_menu)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(selectedFolder.folderName)
            .setAdapter(menuAdapter) { _: DialogInterface, which: Int ->
                handleSelectedMenu(menuAdapter.getItem(which), selectedFolder)
            }
            .create()
        dialog.setOnShowListener { foldersAdapter.setContextSelected(position) }
        dialog.setOnDismissListener { foldersAdapter.clearContextSelected() }
        lifecycle.addObserver(DialogDismissLifecycleObserver(dialog))
        dialog.show()
    }

    private fun handleSelectedMenu(item: MenuItem, selectedFolder: Folder) {
        when (item.itemId) {
            R.id.shuffle -> presenter.onClickMenuShuffle(selectedFolder.absolutePath)
            R.id.add_to_playlist -> presenter.onClickMenuAddToPlaylist(selectedFolder.absolutePath)
            R.id.exclude_folder -> {
                val dialog = MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.folder_menu_exclude_folder_dialog_title)
                    .setMessage(
                        getString(
                            R.string.folder_menu_exclude_folder_dialog_desc,
                            selectedFolder.folderName
                        )
                    )
                    .setPositiveButton(R.string.dialog_exclude) { _: DialogInterface, _: Int ->
                        foldersAdapter.removeWithCondition(selectedFolder::equals)
                        presenter.onClickMenuExcludeFolder(selectedFolder.absolutePath)
                    }
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show()
                lifecycle.addObserver(DialogDismissLifecycleObserver(dialog))
            }
        }
    }

    override fun refreshFolders(folders: List<Folder>) {
        val foldersCountStr = resources
            .getQuantityString(R.plurals.folders_count, folders.size, folders.size)
        rootBinding.additionalInfo.text = foldersCountStr
        if (!TracklistTool.isFolderListsIdentical(foldersAdapter.all, folders)) {
            foldersAdapter.replaceAll(folders)
        }
    }

    override fun setItemDividerShowing(showed: Boolean) {
        binding.folderList.removeItemDecoration(itemDecoration)
        if (showed) binding.folderList.addItemDecoration(itemDecoration)
    }

    override fun onClickBackButton() {
        presenter.onClickBack()
    }

    override fun onEndSlidingAnimation() {
        presenter.onEnterSlideAnimationEnded()
    }

    @ProvidePresenter
    fun providePresenter(): FoldersListPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return FoldersListPresenter(appComponent)
    }

    override fun smoothScrollToTop() {
        val fastScrollMinimalPosition =
            (binding.folderList.layoutManager as LinearLayoutManager).visibleItemsCount() * 3
        binding.folderList.scrollUp(fastScrollMinimalPosition)
        binding.folderList.smoothScrollToTop()
    }
}
