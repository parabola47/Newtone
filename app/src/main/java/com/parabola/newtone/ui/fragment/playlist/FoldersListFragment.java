package com.parabola.newtone.ui.fragment.playlist;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.parabola.domain.model.Folder;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.BaseAdapter;
import com.parabola.newtone.adapter.FolderAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.FoldersListPresenter;
import com.parabola.newtone.mvp.view.FoldersListView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public final class FoldersListFragment extends BaseSwipeToBackFragment
        implements FoldersListView {

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
        foldersList.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        foldersAdapter.setItemClickListener(position ->
                presenter.onClickFolderItem(foldersAdapter.get(position).getAbsolutePath()));

        foldersTitle.setText(R.string.playlist_folders);

        return root;
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

}
