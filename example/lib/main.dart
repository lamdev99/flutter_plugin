import 'dart:developer';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_plugin/contact_model.dart';
import 'package:flutter_plugin/flutter_plugin.dart';

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

  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
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
      log("main 77 ${platEx.toString()}");
      listContact = [];
    } catch (ex) {
      log("main75 Failed to get contact ${ex.toString()}");
      listContact = [];
    }
    if (!mounted) return;
    setState(() {
      _listContact.addAll(listContact);
    });
  }
}
