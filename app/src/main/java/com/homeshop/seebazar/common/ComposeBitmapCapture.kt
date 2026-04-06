package com.homeshop.seebazar.common

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Renders [content] in an off-window [ComposeView], measures it, and returns a bitmap of the result.
 * Used to share the receipt card as a PNG.
 */
suspend fun captureComposableAsBitmap(
    activity: ComponentActivity,
    content: @Composable () -> Unit,
): Bitmap = suspendCancellableCoroutine { cont ->
    val composeView = ComposeView(activity).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        setViewTreeLifecycleOwner(activity)
        setViewTreeSavedStateRegistryOwner(activity)
        setContent { content() }
    }

    val root = activity.findViewById<ViewGroup>(android.R.id.content)
    val widthPx = activity.resources.displayMetrics.widthPixels
    val widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.AT_MOST)
    val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)

    val params = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    )
    root.addView(composeView, params)

    cont.invokeOnCancellation {
        activity.runOnUiThread {
            (composeView.parent as? ViewGroup)?.removeView(composeView)
        }
    }

    fun finishError(t: Throwable) {
        activity.runOnUiThread {
            (composeView.parent as? ViewGroup)?.removeView(composeView)
        }
        cont.resumeWith(Result.failure(t))
    }

    fun measureAndDraw() {
        try {
            composeView.measure(widthSpec, heightSpec)
            composeView.layout(0, 0, composeView.measuredWidth, composeView.measuredHeight)
            val w = composeView.width.takeIf { it > 0 } ?: composeView.measuredWidth
            val h = composeView.height.takeIf { it > 0 } ?: composeView.measuredHeight
            if (w <= 0 || h <= 0) {
                finishError(IllegalStateException("Receipt capture size invalid: ${w}x$h"))
                return
            }
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            composeView.draw(canvas)
            (composeView.parent as? ViewGroup)?.removeView(composeView)
            cont.resume(bitmap)
        } catch (t: Throwable) {
            finishError(t)
        }
    }

    // Two frames: first composition pass, then measure/layout.
    composeView.post {
        composeView.post { measureAndDraw() }
    }
}
