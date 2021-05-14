package com.example.basic_video_chat_flutter

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.NonNull
import com.opentok.android.BaseVideoRenderer
import com.opentok.android.OpentokError
import com.opentok.android.Publisher
import com.opentok.android.PublisherKit
import com.opentok.android.PublisherKit.PublisherListener
import com.opentok.android.Session
import com.opentok.android.Session.SessionListener
import com.opentok.android.Subscriber
import com.opentok.android.SubscriberKit
import com.opentok.android.SubscriberKit.SubscriberListener
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import com.opentok.android.Stream

class MainActivity : FlutterActivity() {

    private var session:Session? = null
    private var publisher:Publisher? = null
    private var subscriber:Subscriber? = null

    private lateinit var opentokVideoPlatformView: OpentokVideoPlatformView

    private val sessionListener: SessionListener = object: SessionListener {
        override fun onConnected(session: Session) {
            // Connected to session
            Log.d("MainActivity", "Connected to session ${session.sessionId}")

            publisher = Publisher.Builder(this@MainActivity).build().apply {
                setPublisherListener(publisherListener)
                renderer?.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL)

                view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                opentokVideoPlatformView.publisherContainer.addView(view)
            }

            notifyFlutter(SdkState.LOGGED_IN)
            session.publish(publisher)
        }

        override fun onDisconnected(session: Session) {
            notifyFlutter(SdkState.LOGGED_OUT)
        }

        override fun onStreamReceived(session: Session, stream: Stream) {
            Log.d(
                "MainActivity",
                "onStreamReceived: New Stream Received " + stream.streamId + " in session: " + session.sessionId
            )
            if (subscriber == null) {
                subscriber = Subscriber.Builder(this@MainActivity, stream).build().apply {
                    renderer?.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL)
                    setSubscriberListener(subscriberListener)
                    session.subscribe(this)

                    opentokVideoPlatformView.subscriberContainer.addView(view)
                }
            }
        }

        override fun onStreamDropped(session: Session, stream: Stream) {
            Log.d(
                "MainActivity",
                "onStreamDropped: Stream Dropped: " + stream.streamId + " in session: " + session.sessionId
            )

            if (subscriber != null) {
                subscriber = null

                opentokVideoPlatformView.subscriberContainer.removeAllViews()
            }
        }

        override fun onError(session: Session, opentokError: OpentokError) {
            Log.d("MainActivity", "Session error: " + opentokError.message)
            notifyFlutter(SdkState.ERROR)
        }
    }

    private val publisherListener: PublisherListener = object : PublisherListener {
        override fun onStreamCreated(publisherKit: PublisherKit, stream: Stream) {
            Log.d("MainActivity", "onStreamCreated: Publisher Stream Created. Own stream " + stream.streamId)
        }

        override fun onStreamDestroyed(publisherKit: PublisherKit, stream: Stream) {
            Log.d("MainActivity", "onStreamDestroyed: Publisher Stream Destroyed. Own stream " + stream.streamId)
        }

        override fun onError(publisherKit: PublisherKit, opentokError: OpentokError) {
            Log.d("MainActivity", "PublisherKit onError: " + opentokError.message)
            notifyFlutter(SdkState.ERROR)
        }
    }

    var subscriberListener: SubscriberListener = object : SubscriberListener {
        override fun onConnected(subscriberKit: SubscriberKit) {
            Log.d("MainActivity", "onConnected: Subscriber connected. Stream: " + subscriberKit.stream.streamId)
        }

        override fun onDisconnected(subscriberKit: SubscriberKit) {
            Log.d("MainActivity", "onDisconnected: Subscriber disconnected. Stream: " + subscriberKit.stream.streamId)
            notifyFlutter(SdkState.LOGGED_OUT)
        }

        override fun onError(subscriberKit: SubscriberKit, opentokError: OpentokError) {
            Log.d("MainActivity", "SubscriberKit onError: " + opentokError.message)
            notifyFlutter(SdkState.ERROR)
        }
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        opentokVideoPlatformView = OpentokVideoFactory.getViewInstance(this)

        flutterEngine
            .platformViewsController
            .registry
            // opentok-video-container is a custom platform-view-type
            .registerViewFactory("opentok-video-container", OpentokVideoFactory())

        addFlutterChannelListener()
    }

    private fun addFlutterChannelListener() {
        MethodChannel(flutterEngine?.dartExecutor?.binaryMessenger, "com.vonage").setMethodCallHandler { call, result ->

            when (call.method) {
                "initSession" -> {
                    val apiKey = requireNotNull(call.argument<String>("apiKey"))
                    val sessionId = requireNotNull(call.argument<String>("sessionId"))
                    val token = requireNotNull(call.argument<String>("token"))

                    notifyFlutter(SdkState.WAIT)
                    initSession(apiKey, sessionId, token)
                    result.success("")
                }
                "swapCamera" -> {
                    swapCamera()
                    result.success("")
                }
                "toggleAudio" -> {
                    val publishAudio = requireNotNull(call.argument<Boolean>("publishAudio"))
                    toggleAudio(publishAudio)
                    result.success("")
                }
                "toggleVideo" -> {
                    val publishVideo = requireNotNull(call.argument<Boolean>("publishVideo"))
                    toggleVideo(publishVideo)
                    result.success("")
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    private fun initSession(apiKey:String, sessionId:String, token:String) {
        session = Session.Builder(this, apiKey, sessionId).build()
        session?.setSessionListener(sessionListener)
        session?.connect(token)
    }

    private fun swapCamera() {
        publisher?.cycleCamera()
    }

    private fun toggleAudio(publishAudio: Boolean) {
        publisher?.publishAudio = publishAudio
    }

    private fun toggleVideo(publishVideo: Boolean) {
        publisher?.publishVideo = publishVideo
    }

    private fun notifyFlutter(state: SdkState) {
        Handler(Looper.getMainLooper()).post {
            MethodChannel(flutterEngine?.dartExecutor?.binaryMessenger, "com.vonage")
                .invokeMethod("updateState", state.toString())
        }
    }
}

enum class SdkState {
    LOGGED_OUT,
    LOGGED_IN,
    WAIT,
    ERROR
}