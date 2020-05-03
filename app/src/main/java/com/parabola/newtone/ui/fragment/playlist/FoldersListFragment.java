package com.parabola.newtone.ui.fragment.playlist;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Folder;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.BaseAdapter;
import com.parabola.newtone.adapter.FolderAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.FoldersListPresenter;
import com.parabola.newtone.mvp.view.FoldersListView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.fragment.Scrollable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static java.util.Objects.requireNonNull;


public final class FoldersListFragment extends BaseSwipeToBackFragment
        implements FoldersListView, Scrollable {

    @InjectPresenter FoldersListPresenter presenter;

    public FoldersListFragment() {
        // Required empty public constructor
    }

    @BindView(R.id.folders_lv) RecyclerView foldersList;

    private final BaseAdapter<Folder> foldersAdapter = new FolderAdapter();


    @BindView(R.id.main) TextView foldersTitle;
    @BindView(R.id.additional_info) TextView foldersCount;


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_folders_list, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);


        foldersList.setAdapter((RecyclerView.Adapter) foldersAdapter);
        foldersAdapter.setOnItemClickListener(position ->
                presenter.onClickFolderItem(foldersAdapter.get(position).getAbsolutePath()));

        foldersTitle.setText(R.string.playlist_folders);

        return root;
    }

    @OnClick(R.id.action_bar)
    public void onClickActionBar() {
        smoothScrollToTop();
    }


    @Override
    public void refreshFolders(List<Folder> folders) {
        String foldersCountStr = getResources()
                .getQuantityString(R.plurals.folders_count, folders.size(), folders.size());
        foldersCount.setText(foldersCountStr);
        foldersAdapter.replaceAll(folders);
    }

    @Override
    protected void onClickBackButton() {
        presenter.onClickBack();
    }

    @Override
    protected void onEndSlidingAnimation() {
        presenter.onEnterSlideAnimationEnded();
    }

    @ProvidePresenter
    FoldersListPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new FoldersListPresenter(appComponent);
    }


    @Override
    public void smoothScrollToTop() {
        LinearLayoutManager layoutManager = requireNonNull((LinearLayoutManager) foldersList.getLayoutManager());
        int firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int screenItemsCount = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition();

        if (firstItemPosition > screenItemsCount * 3) {
            foldersList.scrollToPosition(screenItemsCount * 3);
        }

        foldersList.smoothScrollToPosition(0);
    }

}
