package com.parabola.newtone.mvp.presenter;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.parabola.domain.repository.PermissionHandler;
import com.parabola.domain.repository.PermissionHandler.Type;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.view.StartView;
import com.parabola.newtone.ui.router.MainRouter;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

@InjectViewState
public final class StartPresenter extends MvpPresenter<StartView> {

    @Inject MainRouter router;
    @Inject PermissionHandler accessRepo;
    private Disposable storagePermissionObserver;

    public StartPresenter(AppComponent appComponent) {
        appComponent.inject(this);
    }

    @Override
    protected void onFirstViewAttach() {
        storagePermissionObserver = accessRepo.observePermissionUpdates(Type.FILE_STORAGE)
                .subscribe(hasStoragePermission -> getViewState().setPermissionPanelVisibility(!hasStoragePermission));
    }

    @Override
    public void onDestroy() {
        storagePermissionObserver.dispose();
    }

    public void onClickRequestPermission() {
        router.openRequestStoragePermissionScreen();
    }
}
