import 'package:device_preview/device_preview.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';
import 'package:intl/intl.dart';

void main() {
  runApp(DevicePreview(
    enabled: !kReleaseMode,
    builder: (context) => MyApp(),
  ));
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      useInheritedMediaQuery: true,
      locale: DevicePreview.locale(context),
      builder: DevicePreview.appBuilder,
      home: WeatherHome(),
    );
  }
}

class WeatherHome extends StatefulWidget {
  @override
  _WeatherHomeState createState() => _WeatherHomeState();
}

class _WeatherHomeState extends State<WeatherHome> {
  Map<String, dynamic>? weatherData;
  Map<String, dynamic>? recommendationData;

  @override
  void initState() {
    super.initState();
    fetchWeatherData();
  }

  Future<void> fetchWeatherData() async {
    try {
      final response = await http
          .get(Uri.parse('http://localhost:8080/weather?city=Vancouver'));
      final data = json.decode(response.body);
      setState(() {
        weatherData = data;
      });
      fetchRecommendationData(data['city']);
    } catch (error) {
      print('Error fetching weather data: $error');
    }
  }

  Future<void> fetchRecommendationData(String city) async {
    try {
      final response = await http
          .get(Uri.parse('http://localhost:8080/recommendation?city=$city'));
      final data = json.decode(response.body);
      setState(() {
        recommendationData = data;
      });
    } catch (error) {
      print('Error fetching recommendation data: $error');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[200],
      body: Center(
        child: weatherData == null
            ? CircularProgressIndicator()
            : SingleChildScrollView(
                child: Column(
                  children: [
                    WeatherCard(weatherData: weatherData!),
                    if (recommendationData != null)
                      RecommendationSection(
                        recommendationData: recommendationData!,
                        recommendationType: weatherData!['recommendation_type'],
                      ),
                  ],
                ),
              ),
      ),
    );
  }
}

class WeatherCard extends StatelessWidget {
  final Map<String, dynamic> weatherData;

  const WeatherCard({required this.weatherData});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.all(16.0),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            Text(weatherData['city'],
                style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold)),
            Text(
                '${weatherData['temperature']}°C ${weatherData['weather_condition']}',
                style: TextStyle(fontSize: 32)),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                Expanded(
                  child: WeatherDetail(
                      label: 'Feels like',
                      value:
                          '${double.parse(weatherData['feels_like']).toStringAsFixed(0)}°C'),
                ),
                Expanded(
                  child: WeatherDetail(
                      label: 'Wind speed',
                      value:
                          '${double.parse(weatherData['wind_speed']).toStringAsFixed(0)} m/s'),
                ),
                Expanded(
                  child: WeatherDetail(
                      label: 'Humidity',
                      value:
                          '${double.parse(weatherData['humidity']).toStringAsFixed(0)}%'),
                ),
                Expanded(
                  child: WeatherDetail(
                      label: 'Sunrise',
                      value: DateFormat('HH:mm:ss').format(
                          DateTime.fromMillisecondsSinceEpoch(
                              int.parse(weatherData['sunrise']) * 1000))),
                ),
                Expanded(
                  child: WeatherDetail(
                      label: 'Sunset',
                      value: DateFormat('HH:mm:ss').format(
                          DateTime.fromMillisecondsSinceEpoch(
                              int.parse(weatherData['sunset']) * 1000))),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class WeatherDetail extends StatelessWidget {
  final String label;
  final String value;

  const WeatherDetail({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(label, style: TextStyle(fontSize: 16)),
        Text(value,
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
      ],
    );
  }
}

class RecommendationSection extends StatelessWidget {
  final Map<String, dynamic> recommendationData;
  final String recommendationType;

  const RecommendationSection({
    required this.recommendationData,
    required this.recommendationType,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.all(16.0),
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text("Today's Recommendation",
                style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
            SizedBox(height: 10),
            Text(
              recommendationData['advice'] ?? 'No recommendations available.',
              style: TextStyle(fontSize: 16),
            ),
            SizedBox(height: 10),
            if (recommendationData['fields'] != null &&
                recommendationData['fields'].isNotEmpty)
              ...recommendationData['fields'].map<Widget>((location) {
                return RecommendationCard(location: location);
              }).toList()
            else if (recommendationType == 'not_recommended')
              Text(
                'No recommended hiking spots today.',
                style: TextStyle(fontSize: 16),
              ),
          ],
        ),
      ),
    );
  }
}

class RecommendationCard extends StatelessWidget {
  final Map<String, dynamic> location;

  const RecommendationCard({required this.location});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: EdgeInsets.symmetric(vertical: 10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Image.network(location['imageUrl']),
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(location['name'],
                    style:
                        TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                Text(location['location']),
                Text('Rating: ${location['rating']}'),
                Text('Difficulty: ${location['difficulty']}'),
                Text('Distance: ${location['distance']} km'),
                Text('Estimated Time: ${location['estimatedTime']}'),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
