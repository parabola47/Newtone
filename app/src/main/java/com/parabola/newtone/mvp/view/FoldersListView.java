package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Folder;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.alias.AddToEndSingle;

@AddToEndSingle
public interface FoldersListView extends MvpView {

    void refreshFolders(List<Folder> folders);
    void setItemDividerShowing(boolean showed);
}
