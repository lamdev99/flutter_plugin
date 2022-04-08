import 'dart:async';
import 'dart:convert';
import 'dart:developer';
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

  static Future<String?> get getImageFromGallery async{
    final imageUri = await _channel.invokeMethod("getImageFromGallery");
    return imageUri;
  }

  static Future<List<String>> get getMultiImageFromGallery async{
    List<String> result = [];
    final List<dynamic>? listContact = await _channel.invokeMethod<List<dynamic>>("getMultiImageFromGallery");
    final jsonResponse = json.decode(listContact.toString());
    for(int index = 0; index < (listContact ?? []).length; index++){
      result.add(jsonResponse[index]);
    }
    return result;
  }

  static Future<String?> get getImageFromCamera async{
    final imageUri = await _channel.invokeMethod("getImageFromCamera");
    return imageUri;
  }
}
