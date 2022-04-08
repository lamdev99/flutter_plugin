import 'dart:developer';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_plugin/contact_model.dart';
import 'package:flutter_plugin/flutter_plugin_tk.dart';
void main() {
  runApp(const MyApp());
}



class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final List<Contact> _listContact = [];
  final List<String> _listPath = [];
  late String _imageUri = "";

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      useInheritedMediaQuery: true,
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: [
            TextButton(
              onPressed: () => _getAllContact(),
              child: const Text("Get All Contact"),
            ),
            TextButton(
              onPressed: () => _getImageFromGallery(),
              child: const Text("Get An Image"),
            ),
            TextButton(
              onPressed: () => _getMultiImageFromGallery(),
              child: const Text("Get Multi Image"),
            ),
            TextButton(
              onPressed: () => _takeImageFromCamera(),
              child: const Text("Take image from camera"),
            ),
            SizedBox(
              width: 100,
              height: 100,
              child: _imageUri == ""
                  ? const SizedBox()
                  : Image.file(
                      File(_imageUri),
                      fit: BoxFit.cover,
                    ),
            ),
            _listPath.isNotEmpty
                ? Expanded(
                    child: ListView.builder(
                      itemBuilder: (context, index) {
                        return SizedBox(
                          width: 100,
                          height: 100,
                          child: Image.file(
                            File(_listPath[index]),
                            fit: BoxFit.cover,
                          ),
                        );
                      },
                      itemCount: _listPath.length,
                    ),
                  )
                : const SizedBox(),
            Expanded(
              child: ListView.builder(
                itemBuilder: (context, index) {
                  return Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      Text(
                        _listContact[index].name,
                        style: Theme.of(context)
                            .textTheme
                            .headline6!
                            .copyWith(color: Colors.black),
                      ),
                      Text(
                        _listContact[index].number,
                        style: Theme.of(context)
                            .textTheme
                            .headline6!
                            .copyWith(color: Colors.red),
                      )
                    ],
                  );
                },
                itemCount: _listContact.length,
              ),
            )
          ],
        ),
      ),
    );
  }

  void _getAllContact() async {
    List<Contact> listContact;
    try {
      listContact = await FlutterPlugin.getAllContact;
    } on PlatformException catch (platEx) {
      log("_getAllContact/PlatformException  $platEx");
      listContact = [];
    } catch (ex) {
      log("_getAllContact/PlatformException  $ex");
      listContact = [];
    }
    if (!mounted) return;
    setState(() {
      _listContact.addAll(listContact);
    });
  }

  void _getImageFromGallery() async {
    String? imageUri;
    try {
      imageUri = await FlutterPlugin.getImageFromGallery;
    } on PlatformException catch (platEx) {
      log("_getImageFromGallery/PlatformException  $platEx");
    } catch (ex) {
      log("_getImageFromGallery/Exception  $ex");
    }
    setState(() {
      _imageUri = imageUri!;
    });
  }

  void _getMultiImageFromGallery() async {
    List<String> listPath = [];
    try {
      listPath.addAll(await FlutterPlugin.getMultiImageFromGallery);
    } on PlatformException catch (platEx) {
      log("_getMultiImageFromGallery/PlatformException  $platEx");
      listPath = [];
    } catch (ex) {
      log("_getMultiImageFromGallery/Exception  $ex");
      listPath = [];
    }
    setState(() {
      _listPath.addAll(listPath);
    });
  }

  void _takeImageFromCamera() async {
    String? imageUri;
    try {
      imageUri = await FlutterPlugin.getImageFromCamera;
    } on PlatformException catch (platEx) {
      log("_takeImageFromCamera/PlatformException  $platEx");
    } catch (ex) {
      log("_takeImageFromCamera/Exception $ex");
    }
    setState(() {
      _imageUri = imageUri!;
    });
  }
}
