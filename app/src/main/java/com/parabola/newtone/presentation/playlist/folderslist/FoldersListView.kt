package com.parabola.newtone.presentation.playlist.folderslist

import com.parabola.domain.model.Folder
import moxy.MvpView
import moxy.viewstate.strategy.alias.AddToEndSingle

@AddToEndSingle
interface FoldersListView : MvpView {
    fun refreshFolders(folders: List<Folder>)

    fun setItemDividerShowing(showed: Boolean)
}
