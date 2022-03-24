import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:flutter_plugin/contact_model.dart';

class FlutterPlugin {
  static const MethodChannel _channel = MethodChannel('flutter_plugin');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<List<Contact>> get getAllContact async{
    List<Contact> result = [];
    final List<dynamic>? listContact = await _channel.invokeMethod<List<dynamic>>("getAllContact");
    final jsonResponse = json.decode(listContact.toString());
    for(int index = 0; index < (listContact ?? []).length; index++){
      result.add(Contact.fromJson(jsonResponse[index]));
    }
    return result;
  }
}
