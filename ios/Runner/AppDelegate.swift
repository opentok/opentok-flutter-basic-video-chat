import UIKit
import Flutter
import OpenTok

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    enum SdkState: String {
        case loggedOut = "LOGGED_OUT"
        case loggedIn = "LOGGED_IN"
        case wait = "WAIT"
        case error = "ERROR"
    }
    
    var session: OTSession?
    var vonageChannel: FlutterMethodChannel?
    
    lazy var publisher: OTPublisher = {
        let settings = OTPublisherSettings()
        settings.name = UIDevice.current.name
        return OTPublisher(delegate: self, settings: settings)!
    }()
    
    var subscriber: OTSubscriber?
    
    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {
        addFlutterChannelListener()
        
        GeneratedPluginRegistrant.register(with: self)
        
        weak var registrar = self.registrar(forPlugin: "plugin-name")
        
        let factory = OpentokVideoFactory(messenger: registrar!.messenger())
        self.registrar(forPlugin: "<plugin-name>")!.register(
            factory,
            withId: "opentok-video-container")
        
        
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }
    
    func addFlutterChannelListener() {
        let controller = window?.rootViewController as! FlutterViewController
        
        vonageChannel = FlutterMethodChannel(name: "com.vonage",
                                             binaryMessenger: controller.binaryMessenger)
        vonageChannel?.setMethodCallHandler({ [weak self]
            (call: FlutterMethodCall, result: @escaping FlutterResult) -> Void in
            guard let self = self else { return }
            
            switch(call.method) {
            case "initSession":
                // ToDo: if
                if let arguments = call.arguments as? [String: String] {
                    
                    let apiKey = arguments["apiKey"]!
                    let sessionId = arguments["sessionId"]!
                    let token = arguments["token"]!
                    
                    self.initSession(apiKey: apiKey, sessionId: sessionId, token: token)
                }
                result("")
            case "swapCamera":
                self.swapCamera()
                result("")
            case "toggleAudio":
                if let arguments = call.arguments as? [String: Bool],
                   let publishAudio = arguments["publishAudio"] {
                    self.toggleAudio(publishAudio: publishAudio)
                }
                result("")
            case "toggleVideo":
                if let arguments = call.arguments as? [String: Bool],
                   let publishVideo = arguments["publishVideo"] {
                    self.toggleVideo(publishVideo: publishVideo)
                }
                result("")
            default:
                result(FlutterMethodNotImplemented)
            }
        })
    }
    
    func toggleAudio(publishAudio: Bool) {
        publisher.publishAudio = !publisher.publishAudio
    }
    
    func toggleVideo(publishVideo: Bool) {
        publisher.publishVideo = !publisher.publishVideo
    }
    
    func swapCamera() {
        if publisher.cameraPosition == .front {
            publisher.cameraPosition = .back
        } else {
            publisher.cameraPosition = .front
        }
    }
    
    func initSession(apiKey: String, sessionId: String, token: String) {
        var error: OTError?
        defer {
            // todo
        }
        
        notifyFlutter(state: SdkState.wait)
        session = OTSession(apiKey: apiKey, sessionId: sessionId, delegate: self)!
        session?.connect(withToken: token, error: &error)
    }
    
    func notifyFlutter(state: SdkState) {
        vonageChannel?.invokeMethod("updateState", arguments: state.rawValue)
    }
}

extension AppDelegate: OTSessionDelegate {
    func sessionDidConnect(_ sessionDelegate: OTSession) {
        print("The client connected to the session.")
        notifyFlutter(state: SdkState.loggedIn)
        
        var error: OTError?
        defer {
            // todo
        }
        
        self.session?.publish(self.publisher, error: &error)
        
        if let pubView = self.publisher.view {
            pubView.frame = CGRect(x: 0, y: 0, width: 200, height: 300)
            
            if OpentokVideoFactory.view == nil {
                OpentokVideoFactory.viewToAddPub = pubView
            } else {
                OpentokVideoFactory.view?.addPublisherView(pubView)
            }
        }
    }
    
    func sessionDidDisconnect(_ session: OTSession) {
        print("The client disconnected from the session.")
        notifyFlutter(state: SdkState.loggedOut)
    }
    
    func session(_ session: OTSession, didFailWithError error: OTError) {
        print("The client failed to connect to the session: \(error).")
    }
    
    func session(_ session: OTSession, streamCreated stream: OTStream) {
        print("A stream was created in the session.")
        var error: OTError?
        defer {
            // todo
        }
        subscriber = OTSubscriber(stream: stream, delegate: self)
        
        session.subscribe(subscriber!, error: &error)
    }
    
    func session(_ session: OTSession, streamDestroyed stream: OTStream) {
        print("A stream was destroyed in the session.")
    }
}

extension AppDelegate: OTPublisherDelegate {
    func publisher(_ publisher: OTPublisherKit, streamCreated stream: OTStream) {
    }
    
    func publisher(_ publisher: OTPublisherKit, streamDestroyed stream: OTStream) {
    }
    
    func publisher(_ publisher: OTPublisherKit, didFailWithError error: OTError) {
        print("Publisher failed: \(error.localizedDescription)")
    }
}

extension AppDelegate: OTSubscriberDelegate {
    func subscriberDidConnect(toStream subscriberKit: OTSubscriberKit) {
        print("Subscriber connected")
        
        if let subView = self.subscriber?.view {
            subView.frame = CGRect(x: 0, y: 0, width: 200, height: 300)
            
            if OpentokVideoFactory.view == nil {
                OpentokVideoFactory.viewToAddSub = subView
            } else {
                OpentokVideoFactory.view?.addSubscriberView(subView)
            }
        }
    }
    
    func subscriber(_ subscriber: OTSubscriberKit, didFailWithError error: OTError) {
        print("Subscriber failed: \(error.localizedDescription)")
    }
    
}

class OpentokVideoFactory: NSObject, FlutterPlatformViewFactory {
    static var view: OpentokVideoPlatformView?
    
    static var viewToAddSub: UIView?
    static var viewToAddPub: UIView?
    
    static func getViewInstance(
        frame: CGRect,
        viewId: Int64,
        args: Any?,
        messenger: FlutterBinaryMessenger?
    ) -> OpentokVideoPlatformView{
        if(view == nil) {
            view = OpentokVideoPlatformView()
            if viewToAddSub != nil {
                view?.addSubscriberView(viewToAddSub!)
            }
            if viewToAddPub != nil {
                view?.addPublisherView(viewToAddPub!)
            }
        }
        
        return view!
    }
    
    private var messenger: FlutterBinaryMessenger
    
    init(messenger: FlutterBinaryMessenger) {
        self.messenger = messenger
        super.init()
    }
    
    func create(
        withFrame frame: CGRect,
        viewIdentifier viewId: Int64,
        arguments args: Any?
    ) -> FlutterPlatformView {
        return OpentokVideoFactory.getViewInstance(
            frame: frame,
            viewId: viewId,
            args: args,
            messenger: messenger)
    }
}

class OpentokVideoPlatformView: NSObject, FlutterPlatformView {
    private let videoContainer: OpenTokVideoContainer
    
    override init() {
        videoContainer = OpenTokVideoContainer()
        super.init()
    }
    
    public func addSubscriberView(_ view: UIView) {
        videoContainer.addSubscriberView(view)
    }
    
    public func addPublisherView(_ view: UIView) {
        videoContainer.addPublisherView(view)
    }
    
    func view() -> UIView {
        return videoContainer
    }
}

final class OpenTokVideoContainer: UIView {
    private let subscriberContainer = UIView()
    private let publisherContainer = UIView()
    
    init() {
        super.init(frame: .zero)
        addSubview(subscriberContainer)
        addSubview(publisherContainer)
    }
    
    
    public func addSubscriberView(_ view: UIView) {
        subscriberContainer.addSubview(view)
    }
    
    public func addPublisherView(_ view: UIView) {
        publisherContainer.addSubview(view)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        let width = frame.width
        let height = frame.height
        
        let videoWidth = width / 2
        subscriberContainer.frame = CGRect(x: 0, y: 0, width: videoWidth, height: height)
        publisherContainer.frame = CGRect(x: videoWidth, y: 0, width: videoWidth, height: height)
    }
}
