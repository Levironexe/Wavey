# Wavey - Task Management App

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue)
![Firebase](https://img.shields.io/badge/Firebase-31.5.0-orange)
![Android](https://img.shields.io/badge/Android-API%2034-brightgreen)
![MVVM](https://img.shields.io/badge/Architecture-MVVM-purple)
[![Live Demo](https://img.shields.io/badge/Live-Demo-brightgreen)](https://wavey-app-demo.example.com)

Wavey is a modern Android task management application designed to help users efficiently track and organize their daily activities. Built with Kotlin and following MVVM architecture, the app delivers a clean, intuitive interface for managing tasks across multiple devices.

## 📱 App Overview

Wavey provides a structured task-tracking system with a comprehensive dashboard that:
- Displays a summarized view of task statuses
- Organizes tasks into "To-Do" and "Completed" categories
- Supports task categorization (Work, School, Errands)
- Features seamless cross-device synchronization

## ✨ Features

- **User Authentication**
  - Google Sign-In integration
  - Email/password registration and login
  - User profile management

- **Task Management**
  - Create, edit, and delete tasks
  - Toggle task completion status
  - Categorize tasks by type
  - Real-time status updates

- **Intuitive UI**
  - Clean Material Design implementation
  - Dashboard with completion statistics
  - Bottom navigation for easy access
  - Custom UI components

- **Cloud Integration**
  - Firebase authentication
  - Cloud Firestore for data storage
  - Real-time synchronization across devices

## 🏗️ Architecture

Wavey is built using the MVVM (Model-View-ViewModel) architecture pattern:

- **Models**: Task and Category data classes
- **Views**: Activities and Adapters for UI representation
- **ViewModels**: TasksViewModel with StateFlow and LiveData

Key components include:
- SectionedTaskAdapter for expandable task sections
- CategoryAdapter for managing categories
- TaskRepository for handling Firebase interactions
- DialogHelper for managing user interactions

## 🚀 Getting Started

### [Read my document](https://drive.google.com/file/d/19ZcOpRca1mE3lLiq6CEczq09sCCjK183/view?usp=drive_link)

### Prerequisites

- Android Studio Arctic Fox (2021.3.1) or newer
- Kotlin 1.9.0+
- Android SDK API level 24+
- Google Play services

### Installation

1. Clone the repository
```bash
git clone https://github.com/yourusername/wavey.git
```

2. Open the project in Android Studio

3. Connect to Firebase
   - Create a Firebase project
   - Add your app to the Firebase project
   - Download and add the `google-services.json` file to your app directory
   - Enable Authentication and Firestore in your Firebase console

4. Build and run the application

## 📊 Implementation Details

Wavey utilizes several modern Android development technologies:
- **StateFlow and LiveData** for reactive UI updates
- **RecyclerView** with custom adapters for dynamic lists
- **Firebase Authentication** for secure user management
- **Cloud Firestore** for real-time data storage
- **Material Design Components** for consistent UI elements
- **Repository Pattern** for data access abstraction
- **Custom UI Components** including ChipGroups and CalendarView

## 🔄 Workflow

1. User authenticates via Google or Email
2. Dashboard presents task status summary
3. Tasks are organized into To-Do and Completed sections
4. Users can create, edit, categorize, and complete tasks
5. All changes sync automatically across devices

## 🛠️ Future Enhancements

- Task reminders and notifications
- Advanced filtering and sorting options
- Calendar integration
- Collaboration features
- Dark mode support
- Offline functionality improvements

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

Built with 💙 by [Your Name](https://github.com/yourusername)
