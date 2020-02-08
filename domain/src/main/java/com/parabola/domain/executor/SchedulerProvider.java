package com.parabola.domain.executor;

import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;

public interface SchedulerProvider {
    @NonNull
    Scheduler computation();

    @NonNull
    Scheduler newThread();

    @NonNull
    Scheduler io();

    @NonNull
    Scheduler ui();
}
