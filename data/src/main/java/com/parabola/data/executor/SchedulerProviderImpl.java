package com.parabola.data.executor;

import com.parabola.domain.executor.SchedulerProvider;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SchedulerProviderImpl implements SchedulerProvider {

    public Scheduler computation() {
        return Schedulers.computation();
    }


    public Scheduler io() {
        return Schedulers.io();
    }


    public Scheduler newThread() {
        return Schedulers.newThread();
    }


    public Scheduler ui() {
        return AndroidSchedulers.mainThread();
    }
}