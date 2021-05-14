package com.example.basic_video_chat_flutter

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class OpentokVideoFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    companion object {
        private lateinit var view: OpentokVideoPlatformView

        fun getViewInstance(context: Context): OpentokVideoPlatformView {
            if(!this::view.isInitialized) {
                view = OpentokVideoPlatformView(context)
            }

            return view
        }
    }

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        return getViewInstance(context)
    }
}

class OpentokVideoPlatformView(context: Context) : PlatformView {
    private val videoContainer: OpenTokVideoContainer = OpenTokVideoContainer(context)

    val subscriberContainer get() = videoContainer.subscriberContainer
    val publisherContainer get() = videoContainer.publisherContainer

    override fun getView(): View {
        return videoContainer
    }

    override fun dispose() {}
}

class OpenTokVideoContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyle, defStyleRes) {

    var subscriberContainer: FrameLayout
        private set

    var publisherContainer: FrameLayout
        private set

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.view_video, this, true)
        subscriberContainer = view.findViewById(R.id.subscriber_container)
        publisherContainer = view.findViewById(R.id.publisher_container)
    }
}