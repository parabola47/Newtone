package com.parabola.newtone.util

import io.reactivex.Observable
import io.reactivex.ObservableSource

class Observables private constructor() {

    companion object {

        fun <T1, T2> combineLatest(
            source1: ObservableSource<out T1>,
            source2: ObservableSource<out T2>,
        ): Observable<Pair<T1, T2>> {
            return Observable.combineLatest(
                source1,
                source2,
            ) { t1, t2 -> t1 to t2 }
        }

    }

}
