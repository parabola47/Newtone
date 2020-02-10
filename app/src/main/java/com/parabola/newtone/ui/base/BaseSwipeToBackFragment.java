package com.parabola.newtone.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.Nullable;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.parabola.newtone.R;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

public abstract class BaseSwipeToBackFragment extends MvpAppCompatFragment {
    private static final String LOG_TAG = BaseSwipeToBackFragment.class.getSimpleName();

    private SlidrInterface slidrInterface;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_base, container, false);

        layout.findViewById(R.id.back_btn).setOnClickListener(v -> onClickBackButton());

        return layout;
    }

    @Nullable
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);

        anim.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (enter) {
                    onEndSlidingAnimation();
                }
            }
        });

        return anim;
    }

    protected abstract void onClickBackButton();

    @Override
    public void onResume() {
        super.onResume();
        if (slidrInterface == null) {
            slidrInterface = Slidr.replace(requireView().findViewById(R.id.content_container),
                    new SlidrConfig.Builder()
                            .position(SlidrPosition.LEFT)
                            .build());
        }
    }

    protected void onEndSlidingAnimation() {
        //callback
    }
}