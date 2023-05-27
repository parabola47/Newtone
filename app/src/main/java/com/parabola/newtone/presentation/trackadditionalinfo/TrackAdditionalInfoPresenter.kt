package com.parabola.newtone.presentation.trackadditionalinfo

import com.parabola.domain.repository.TrackRepository
import com.parabola.newtone.di.app.AppComponent
import io.reactivex.internal.functions.Functions
import io.reactivex.internal.observers.ConsumerSingleObserver
import moxy.InjectViewState
import moxy.MvpPresenter
import javax.inject.Inject

@InjectViewState
class TrackAdditionalInfoPresenter(
    appComponent: AppComponent,
    private val trackId: Int,
) : MvpPresenter<TrackAdditionalInfoView>() {

    @Inject
    lateinit var trackRepo: TrackRepository

    init {
        appComponent.inject(this)
    }


    override fun onFirstViewAttach() {
        trackRepo.getById(trackId).subscribe(
            ConsumerSingleObserver(
                viewState::setTrack,
                Functions.ERROR_CONSUMER
            )
        )
    }

}
