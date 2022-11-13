import 'package:flutter/material.dart';
import 'package:firebase_core/firebase_core.dart';
import 'package:firebase_messaging/firebase_messaging.dart';

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
    final token = await messaging.getAPNSToken();
    print("User granted permission: ${settings.authorizationStatus}; Token is: $token");
    return token;
  }

  WidgetsFlutterBinding.ensureInitialized(); // make this call if you have initialization code before "runApp()"
  final token = await initMessaging();
  runApp(MyApp(token ?? ""));
}

class MyApp extends StatelessWidget {
  final String token;
  MyApp(this.token);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        title: 'Push Notifications application',
        debugShowCheckedModeBanner: false,
        home: Scaffold (
            appBar: AppBar(title: Text("Push Notifications application")),
            body: Center(child: Text("Your token is $token"))
        )
    );
  }
}
