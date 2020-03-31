package com.parabola.newtone.mvp;

import java.util.Iterator;
import java.util.List;

import moxy.MvpView;
import moxy.viewstate.ViewCommand;
import moxy.viewstate.strategy.StateStrategy;

public class CustomAddToEndSingleStrategy implements StateStrategy {

    @Override
    public <View extends MvpView> void beforeApply(final List<ViewCommand<View>> currentState,
                                                   final ViewCommand<View> incomingCommand) {
        Iterator<ViewCommand<View>> iterator = currentState.iterator();

        while (iterator.hasNext()) {
            ViewCommand<View> entry = iterator.next();

            if (entry == null || entry.getClass() == incomingCommand.getClass()) {
                iterator.remove();
                break;
            }
        }

        currentState.add(incomingCommand);
    }

    @Override
    public <View extends MvpView> void afterApply(final List<ViewCommand<View>> currentState,
                                                  final ViewCommand<View> incomingCommand) {
        // pass
    }
}
