package com.parabola.newtone.uikit

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import kotlin.math.max

/**
 * ViewPager для отображения в [DialogFragment].<p>
 * Высчитывает свою высоту на основе внутреннего [View] с максимальной высотой
 */
class WrapContentHeightViewPager(
    context: Context,
    attrs: AttributeSet?,
) : ViewPager(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var resultHeightMeasureSpec = heightMeasureSpec
        val wrapHeight = MeasureSpec.getMode(resultHeightMeasureSpec) == MeasureSpec.AT_MOST
        if (wrapHeight) {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child != null) {
                    child.measure(
                        widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    )
                    val height = child.measuredHeight
                    resultHeightMeasureSpec = max(
                        resultHeightMeasureSpec,
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
                    )
                }
            }
        }
        super.onMeasure(widthMeasureSpec, resultHeightMeasureSpec)
    }


}
