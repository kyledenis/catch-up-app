# catch-up-app
Minimum SDK: API 29 (Q; Android 10.0)

> Ensure you switch to `development` branch during build.
>
> App will not work correctly unless you input your relevant Google Maps API key into `secrets.properties` where specified.
>
> `google-services.json` must also be present within `./app` directory.

## Description
Catch-Up is a location-based Android application designed to help friends and family decide on meeting spots based on individual preferences and geographic convenience. The app facilitates the selection process through an interactive and engaging interface, allowing users to swipe through suggested Points of Interest (POIs), much like Tinder.

## Features
- User Authentication: Secure login and registration system.
- Dynamic POI Suggestions: Utilises user location and preferences to suggest potential meeting places.
- Interactive Map Integration: Users can pinpoint locations and visualise POIs on a map.
- Swipe to Vote: Users can swipe right or left to vote on their preferred meeting spots.
- Group Lobby System: Users can join or create a lobby to make collective decisions.
- Saved Lists: Users can save and manage lists of their favorite places.

## Tech Stack
- Frontend: Java for Android
- Backend: Firebase for authentication, database management, and hosting.
- APIs: Google Maps for mapping functionalities.
