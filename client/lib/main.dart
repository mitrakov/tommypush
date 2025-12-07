// ignore_for_file: avoid_print, use_key_in_widget_constructors

import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/services.dart';

// to set up Firebase: see http://mitrakoff.com/en/flutter/firebase/get-started
// note: in `ios/Runner/GoogleService-Info.plist` there is a Firebase API-Key exposed which is NOT a security risk (https://stackoverflow.com/questions/38092301, https://groups.google.com/g/firebase-talk/c/4A23wPAbRjw)
void main() async {
  Future<String?> initMessaging() async {
    await Firebase.initializeApp();
    final messaging = FirebaseMessaging.instance;
    NotificationSettings settings = await messaging.requestPermission(
      alert: true,
      announcement: false,
      badge: true,
      carPlay: true,
      criticalAlert: false,
      provisional: false,
      sound: true,
    );
    final apnsToken = await messaging.getAPNSToken();
    final fcmToken = await messaging.getToken();
    print("User granted permission: ${settings.authorizationStatus}; APNS Token is: $apnsToken; FCM token is $fcmToken");
    return fcmToken;
  }

  WidgetsFlutterBinding.ensureInitialized(); // make this call if you have initialization code before "runApp()"
  final token = await initMessaging();
  runApp(MaterialApp(title: "Tommypush", home: MyApp(token ?? "")));
}

class MyApp extends StatelessWidget {
  final String token;
  const MyApp(this.token);

  @override
  Widget build(BuildContext context) {
    return Scaffold (
      appBar: AppBar(title: const Text("Tommy Push Notifications App")),
      body: Center(child: Text("Your FCM token is $token")),
      floatingActionButton: FloatingActionButton(
        child: const Icon(Icons.copy),
        onPressed: () async {
          await Clipboard.setData(ClipboardData(text: token));
          ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text("Copied!"), duration: Duration(seconds: 2)));
        }
      ),
    );
  }
}

/*
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/material.dart';

// This handler runs when the app is in the background or terminated.
// It is crucial to initialize Firebase inside this handler.
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  await Firebase.initializeApp();
  print("Handling a background message: ${message.messageId}");
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp();

  // Set the background message handler
  FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);

  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  RemoteMessage? _initialMessage;

  @override
  void initState() {
    super.initState();
    _checkInitialMessage();
  }

  // Function to check for an initial message
  Future<void> _checkInitialMessage() async {
    RemoteMessage? initialMessage = await FirebaseMessaging.instance.getInitialMessage();

    if (initialMessage != null) {
      setState(() {
        _initialMessage = initialMessage;
      });
      _handleMessage(initialMessage);
    }

    // Also handle messages when the app is in the background and opened via notification
    FirebaseMessaging.onMessageOpenedApp.listen(_handleMessage);
  }

  void _handleMessage(RemoteMessage message) {
    // Implement your logic to handle the message payload
    // For example, navigate to a specific screen based on data in the message
    if (message.data['type'] == 'chat') {
      Navigator.pushNamed(context, '/chat', arguments: message.data);
    } else if (message.data['type'] == 'product') {
      Navigator.pushNamed(context, '/product_detail', arguments: message.data['productId']);
    }
    // ... other handling
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Firebase Messaging Demo',
      home: Scaffold(
        appBar: AppBar(
          title: Text('Firebase Messaging'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text('Initial Message:'),
              if (_initialMessage != null)
                Column(
                  children: [
                    Text('Title: ${_initialMessage!.notification?.title ?? 'N/A'}'),
                    Text('Body: ${_initialMessage!.notification?.body ?? 'N/A'}'),
                    Text('Data: ${_initialMessage!.data}'),
                  ],
                )
              else
                Text('No initial message.'),
            ],
          ),
        ),
      ),
      routes: {
        '/chat': (context) => ChatScreen(),
        '/product_detail': (context) => ProductDetailScreen(),
      },
    );
  }
}

// Example screens for navigation
class ChatScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final Map<String, dynamic>? args = ModalRoute.of(context)?.settings.arguments as Map<String, dynamic>?;
    return Scaffold(
      appBar: AppBar(title: Text('Chat Screen')),
      body: Center(child: Text('Chat with: ${args?['senderId'] ?? 'Unknown'}')),
    );
  }
}

class ProductDetailScreen extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final String? productId = ModalRoute.of(context)?.settings.arguments as String?;
    return Scaffold(
      appBar: AppBar(title: Text('Product Detail')),
      body: Center(child: Text('Product ID: ${productId ?? 'Unknown'}')),
    );
  }
}
*/
