# TxDev Systems Android Application

## Overview
The **TxDev Systems App** is an Android application developed using Kotlin. It provides a modern, data-driven interface that integrates IoT-based device monitoring and management through charts, dashboards, and authentication systems. The app includes robust testing, database integration, and CI/CD support.

## Features
- **User Authentication** via API integration and token management  
- **Dashboard and Charts** for device metrics such as temperature, battery, and door status  
- **Room Database** for local persistence  
- **Retrofit Networking** for API communication  
- **Firebase Integration** for cloud-based services  
- **Unit and Instrumentation Tests** for UI and API validation  
- **Continuous Integration (CI)** using GitHub Actions  

## Technologies Used
- **Kotlin**
- **Android Jetpack Components**
- **Room Persistence Library**
- **Retrofit**
- **Firebase Cloud Messaging**
- **JUnit & Mockito**
- **Gradle (KTS)**
- **GitHub Actions**

## Installation and Setup
1. **Clone the Repository**
   ```bash
   git clone https://github.com/<your-repo-link>.git
   cd TxDev_App-main# TxDev_App

   # Project Setup

## 2. Open in Android Studio

* Open the project folder in Android Studio.
* Allow Gradle to sync dependencies automatically.

## 3. Configure Firebase

* Add your `google-services.json` file in the `app/` directory if not already included.

## 4. Run the App

* Connect an Android device or use an emulator.
* Click Run ▶ in Android Studio.

## Testing

The app includes both unit tests and instrumentation tests:

* Located in `app/src/androidTest/java/`
* Tests cover:
  * API integrations (`AuthApiTest`, `RetroFitClientTest`)
  * UI components and fragments (`DashboardFragmentTest`, `HomeFragmentTest`)
  * ViewModel logic and adapters

To run tests:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## References

Anasthasios, A. (2023) SpeedView Compose: Dynamic Speedometer and Gauge for Android. Version 1.0.0-alpha01. Available at: https://github.com/anastr/SpeedView (Accessed: 25 August 2025).

Anasthasios, A. (2023) SpeedView: Dynamic Speedometer and Gauge for Android. Version 1.6.1. Available at: https://github.com/anastr/SpeedView (Accessed: 25 August 2025).

Android Developers. (2025). Token | Android Developers. Available at: https://developer.android.com/reference/androidx/browser/trusted/Token.

Android Developers. (2025a). Test apps on Android. Available at: https://developer.android.com/training/testing.

Android Developers. (2025b). (Deprecated) Advanced Android in Kotlin 05.1: Testing Basics | Android Developers. Available at: https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-basics?index=..%2F..index#0 [Accessed 25 Aug. 2025].

Android Developers. (n.d.). Room Persistence Library. Available at: https://developer.android.com/jetpack/androidx/releases/room [Accessed 27 Jun. 2025].

Atlassian. (n.d.). Continuous integration vs. delivery vs. deployment. Retrieved September 22, 2025, from https://www.atlassian.com/continuous-delivery/principles/continuous-integration-vs-delivery-vs-deployment.

Bechtold, S. (2016). JUnit 5 User Guide. Available at: https://docs.junit.org/current/user-guide/.

Carrington, M. (2021). Mobile App Development Team: Structure and Roles. Velvetech. Available at: https://www.velvetech.com/blog/mobile-app-development-team-structure/ (Accessed: 3 June 2025).

College, V. (2023). How to reference with Cite Them Right. Bloomsbury. doi:10.5040/9781350886148.

Evans, E. (2003). Domain-Driven Design: Tackling Complexity in the Heart of Software. Boston: Addison-Wesley.

Figma Inc. (2024). Figma: Interface Design Tool. Available at: https://www.figma.com [Accessed 27 Jun. 2025].

GeeksforGeeks. (2025). DevOps Lifecycle. Retrieved September 22, 2025, from https://www.geeksforgeeks.org/devops/devops-lifecycle/.

Google Firebase. (n.d.). Firebase Cloud Messaging Documentation. Available at: https://firebase.google.com/docs/cloud-messaging [Accessed 27 Jun. 2025].

Gurnov, A. (2022). Project Charter: Guide with Examples, Template, and Video. Wrike. Available at: https://www.wrike.com/project-management-guide/faq/what-is-a-project-charter-in-project-management/ (Accessed: 3 June 2025).

henrymbuguakiarie. (2025). Call a web API in a sample Android mobile app - Microsoft identity platform. Available at: https://learn.microsoft.com/en-us/entra/identity-platform/quickstart-native-authentication-android-call-api [Accessed 25 Aug. 2025].

Hirunika Karunathilaka. (2021). Token-based authentication & REST API Implementation for Android | Kotlin apps. Medium. Available at: https://hirukarunathilaka.medium.com/token-based-authentication-rest-api-implementation-for-android-kotlin-apps-d2109b18eb36 [Accessed 25 Aug. 2025].

ICASA. (2023). Annual State of the ICT Sector Report in South Africa. Available at: https://www.icasa.org.za [Accessed 27 Jun. 2025].

Information Regulator (South Africa). (2021). Protection of Personal Information Act (POPIA). Available at: https://www.justice.gov.za/inforeg/ [Accessed 27 Jun. 2025].

Keup, M. (2021). How to Create a Project Roadmap (Example Included). ProjectManager. Available at: https://www.projectmanager.com/blog/tips-for-project-roadmap.

Landau, P. (2020). Sprint Planning 101: How to Plan Great Sprints. ProjectManager. Available at: https://www.projectmanager.com/blog/sprint-planning-101 (Accessed: 3 June 2025).

Microsoft Docs. (n.d.). ASP.NET Core Web API Documentation. Available at: https://learn.microsoft.com/en-us/aspnet/core/web-api [Accessed 27 Jun. 2025].

Norman, D.A. (2013). The Design of Everyday Things. Revised and Expanded ed. New York: Basic Books.

Panaya. (2024). What is User Acceptance Testing (UAT) - The Full Process Explained. Available at: https://www.panaya.com/blog/testing/what-is-uat-testing/ [Accessed 15 October 2025].

PostgreSQL Global Development Group. (n.d.). PostgreSQL Documentation. Available at: https://www.postgresql.org/docs/ [Accessed 27 Jun. 2025].

Smartsheet. (2022). Complete Guide to Agile Project Charters. Available at: https://www.smartsheet.com/content/agile-project-charter (Accessed: 3 June 2025).

Sproviero, F. (2018). Android Unit Testing with Mockito. Available at: https://www.kodeco.com/195-android-unit-testing-with-mockito [Accessed 25 Aug. 2025].

Twilio. (n.d.). Twilio Messaging Services. Available at: https://www.twilio.com/docs/sms [Accessed 27 Jun. 2025].

Varsity College, I. V. (2025). INSY7315 Module Manual / Module Outline. Pretoria: Varsity College Pretoria.

## Authors

* Cherika Bodde
* Nathan Hani
* Marené Van Der Merwe
* Charné Janse Van Rensburg
* Ryan Stratford
* Inge Dafel
