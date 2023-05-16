// ignore_for_file: avoid_print, use_key_in_widget_constructors

import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/services.dart';
import 'package:fluttertoast/fluttertoast.dart';

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
  runApp(MyApp(token ?? ""));
}

class MyApp extends StatelessWidget {
  final String token;
  const MyApp(this.token);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        title: 'Push Notifications App',
        debugShowCheckedModeBanner: false,
        home: Scaffold (
          appBar: AppBar(title: const Text("Tommy Push Notifications App")),
          body: Center(child: Text("Your FCM token is $token")),
          floatingActionButton: FloatingActionButton(
            child: const Icon(Icons.copy),
            onPressed: () {
              Clipboard.setData(ClipboardData(text: token));
              Fluttertoast.showToast(msg: "Copied!");
            }
          ),
        )
    );
  }
}
