## Fish The Break - Fishing Data Tracker App

## Overview

Fish The Break is an innovative mobile application designed to assist anglers in tracking essential
fishing data, ensuring a better fishing experience. By utilizing advanced data layers, weather
analytics, and real-time tracking, it helps fishermen make data-driven decisions for optimal fishing
strategies. The app integrates seamlessly with ArcGIS maps, offering users features like distance
measurement, weather radar, and route management.

Disclaimer: This repository contains a clone of the original project created for showcasing my
contributions. Due to intellectual property agreements, the actual code and project are not
available publicly. This repo is intended to demonstrate my work on the project.

## Features

### 1. Weather & Sea Data Layers

Chlorophyll Levels: Displays phytoplankton clusters that indicate high fish activity.

Weather Radar: Provides precipitation density and cloud coverage over both sea and land to help
users track weather patterns affecting fish behavior.

Wind Speed & Direction: Helps anglers assess chop and locate where to expect winds, optimizing their
fishing strategy.

ArcGIS Integration: Integrated with ArcGIS ISRI maps, providing dynamic, real-time weather and
geographic data.

### 2. Trolling Tracking & Route Management

Track Trolling Lines: The app allows users to accurately track their trolling lines, marking key
points while trolling.

Save Routes & Locations: Fishermen can save points of interest and routes, leveraging Room DB for
local storage.

Measure Distances: Quickly measure distances between fishing spots to optimize coverage.

### 3. Offline Support

Room Database: Used for offline storage of trolling points and routes. This ensures that users can
continue using the app even when there’s no internet connection.

Flows & Coroutines: Used for handling asynchronous operations like retrieving and saving data in a
smooth, non-blocking way.

### 4. Data Layers Customization

Users can choose their favorite layers (such as Chlorophyll, Wind Speed, etc.) from a set of
available options and toggle them as needed.

This feature allows fishermen to focus on the data that matters most to them during their fishing
expeditions.

### 5. Distance Measurement & Points Saving

The app allows fishermen to measure distances between fishing locations, providing accurate
information for trip planning and route optimization.

Saved points and routes are stored using Room DB, ensuring all data is available offline.

## Technologies Used

# Kotlin: Primary language used for Android development.

# Room Database: For offline storage of fishing points and routes.

# Flow & Coroutines: For handling background tasks and making the app responsive while fetching and saving data.

# ArcGIS (ISRI maps): Integrated with the app for real-time weather data and mapping features.

# MVVM Architecture: For clean separation of concerns, ensuring better scalability and maintainability of the app.

# Retrofit: For making network calls and fetching real-time data such as weather and sea conditions.

# Google Maps API: Used for mapping and geolocation features.

# Push Notifications

App Architecture
This app follows the MVVM architecture to ensure clean code and separation of concerns:

Model: Handles the data, including Room DB and network calls.

View: The UI layer that handles user interaction.

ViewModel: Manages the data logic and provides data to the View, ensuring the UI is responsive and
decoupled from business logic.

Repository: Handles data management, including fetching from the network or Room DB.

## Setup Instructions

## 1. Clone this repository:

- [ ] [git clone https://github.com/yourusername/fish-the-break.git]
- [ ] [cd fish-the-break]

## 2. Install dependencies:

Open the project in Android Studio and let it sync the dependencies automatically.

## 3. API Keys:

Google Maps API: Make sure to add your Google Maps API key in the local.properties file.

ArcGIS API: You'll need an API key for accessing ArcGIS data layers.

## 4. Run the app:

Build and run the app on an emulator or a physical device. The app should work offline for managing
and saving trolling points and routes.

### Conclusion

Fish The Break is a cost-effective fishing app that enhances the experience of anglers by providing
weather insights, location-based features, and real-time data. My contributions to this project
include integrating weather data layers, implementing the trolling tracking feature, and enabling
offline support using Room DB. This app empowers fishermen to make data-driven decisions, ultimately
optimizing their fishing strategies for improved results.

Feel free to explore the app and let me know if you have any questions or suggestions!

## Disclaimer:

As mentioned earlier, the code in this repository is a clone created for demonstrating my skills and
is not the actual code due to intellectual property agreements with the original project.




