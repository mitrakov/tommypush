// ignore_for_file: use_key_in_widget_constructors, curly_braces_in_flow_control_structures
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

// this handler runs when the app is in the background or terminated; must be on-top level with "@pragma" statement
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  await Firebase.initializeApp(); // it is crucial to initialize Firebase inside this handler
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();                                    // allow async calls in "main"
  await Firebase.initializeApp();                                               // init Firebase plugin
  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);   // setup background handler for Firebase

  runApp(MaterialApp(title: "Tommypush", home: MyApp()));
}

class MyApp extends StatefulWidget {
  @override
  MyAppState createState() => MyAppState();
}

class MyAppState extends State<MyApp> {
  String? _fcmToken;
  RemoteMessage? _message;

  @override
  void initState() {
    super.initState();
    _initFirebase();
  }

  void _initFirebase() async {
    final messaging = FirebaseMessaging.instance;
    RemoteMessage? initialMessage = await messaging.getInitialMessage();        // check initial message on App startup/awake
    NotificationSettings settings = await messaging.requestPermission(          // request Push Notification permissions from user
      alert: true, announcement: false, badge: true, carPlay: true, criticalAlert: false, provisional: false, sound: true,
    );
    final apnsToken = await messaging.getAPNSToken();                           // APNS token (for iOS only)
    final fcmToken = await messaging.getToken();                                // FCM token (for iOS/Android)
    print("User granted permission: ${settings.authorizationStatus}; APNS Token is: $apnsToken; FCM token is $fcmToken");

    setState(() { _fcmToken = fcmToken; });
    if (initialMessage != null)
      _handleMessage(initialMessage);

    FirebaseMessaging.onMessageOpenedApp.listen(_handleMessage); // handle messages when the App is opened from the background
  }

  void _handleMessage(RemoteMessage message) => setState(() { _message = message; });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text("Tommypush App")),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(_message != null ? "Message:" : "FCM Token:", style: const TextStyle(fontWeight: FontWeight.w700)),
            if (_message != null)
              Column(
                children: [
                  Text('Title: ${_message!.notification?.title ?? ""}'),
                  Text('Body: ${_message!.notification?.body ?? ""}'),
                  Text('Data: ${_message!.data}'),
                ],
              )
            else Text(_fcmToken ?? ""),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.copy),
        onPressed: () async {
          await Clipboard.setData(ClipboardData(text: _fcmToken ?? ""));
          const msg = "FCM token copied!";
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text(msg), duration: Duration(seconds: 2)));
        }
      ),
    );
  }
}
