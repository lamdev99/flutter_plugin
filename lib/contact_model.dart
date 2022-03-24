import 'package:json_annotation/json_annotation.dart';

part 'contact_model.g.dart';
@JsonSerializable()
class Contact{
  String name;
  String number;

  Contact({required this.name, required this.number});

  factory Contact.fromJson(Map<String, dynamic> json) => _$ContactFromJson(json);

  /// `toJson` is the convention for a class to declare support for serialization
  /// to JSON. The implementation simply calls the private, generated
  /// helper method `_$UserToJson`.
  Map<String, dynamic> toJson() => _$ContactToJson(this);

  @override
  String toString() {
    return 'Contact{name: $name, number: $number}';
  }
}