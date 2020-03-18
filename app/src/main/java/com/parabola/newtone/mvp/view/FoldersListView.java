package com.parabola.newtone.mvp.view;

import com.parabola.domain.model.Folder;

import java.util.List;

import moxy.MvpView;
import moxy.viewstate.strategy.AddToEndSingleStrategy;
import moxy.viewstate.strategy.StateStrategyType;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface FoldersListView extends MvpView {
    void refreshFolders(List<Folder> folders);
}
