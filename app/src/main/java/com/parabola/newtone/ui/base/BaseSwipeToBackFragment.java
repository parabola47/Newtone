package com.parabola.newtone.ui.base;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.parabola.newtone.R;
import com.parabola.newtone.databinding.FragmentBaseBinding;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import moxy.MvpAppCompatFragment;

public abstract class BaseSwipeToBackFragment extends MvpAppCompatFragment {
    private static final String LOG_TAG = BaseSwipeToBackFragment.class.getSimpleName();

    private SlidrInterface slidrInterface;

    private FragmentBaseBinding fragmentBaseBinding;

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentBaseBinding = FragmentBaseBinding.inflate(inflater, container, false);
        fragmentBaseBinding.backBtn.setOnClickListener(v -> onClickBackButton());

        return fragmentBaseBinding.getRoot();
    }

    protected final FragmentBaseBinding getRootBinding() {
        return fragmentBaseBinding;
    }

    @Nullable
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation anim;
        try {
            anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
        } catch (Resources.NotFoundException ex) {
            return super.onCreateAnimation(transit, enter, nextAnim);
        }

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
        if (slidrInterface == null
                || (getRetainInstance() && requireView().findViewById(R.id.content_container) != null)) {
            SlidrConfig config = new SlidrConfig.Builder().position(SlidrPosition.LEFT).build();
            slidrInterface = Slidr.replace(requireView().findViewById(R.id.content_container), config);
        }
    }

    protected void onEndSlidingAnimation() {
        //callback
    }
}
