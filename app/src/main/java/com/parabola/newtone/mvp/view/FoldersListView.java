package com.parabola.newtone.mvp.view;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.parabola.domain.model.Folder;

import java.util.List;

@StateStrategyType(AddToEndSingleStrategy.class)
public interface FoldersListView extends MvpView {
    void refreshFolders(List<Folder> folders);
}
