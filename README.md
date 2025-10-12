# On-Device Image Classifier

A cross-platform mobile app demonstrating **on-device AI** for iOS and Android. This app performs real-time image classification completely locally on your device — no cloud, no network calls, 100% private.

## What It Does

Choose any photo from your library, and the app instantly predicts what's in it with a confidence score. Everything runs locally using native ML frameworks (Core ML for iOS, TensorFlow Lite for Android).

## Features

- **Fast** ⚡ - Results appear instantly (~50ms)
- **Private** 🔒 - Everything happens locally, photos never leave your device
- **Offline** 🔋 - Works anywhere, no internet required
- **Accurate** - Uses MobileNetV2 trained on ImageNet (1000 object classes)
- **Cross-Platform** 📱 - Native implementations for both iOS and Android

## Tech Stack

### iOS
- **SwiftUI** - Modern declarative UI framework
- **Core ML** - Apple's machine learning framework
- **MobileNetV2** - Lightweight image classification model (~17MB)
- **iOS 14.0+** - Minimum deployment target

### Android
- **Jetpack Compose** - Modern declarative UI toolkit
- **TensorFlow Lite** - Google's on-device ML framework
- **MobileNetV2** - Quantized model (~4MB)
- **Android 7.0+ (API 24+)** - Minimum SDK version

## How It Works

```
User selects photo → MobileNetV2 model → Prediction with confidence score
                     (runs on device)
```

The architecture is surprisingly simple:
1. **MobileNetV2 model** runs locally via Core ML (iOS) or TensorFlow Lite (Android)
2. **Native UI** handles the interface and image picker
3. **Image → Model → Predictions** (all on device, ~50ms)

## Project Structure

```
OnDeviceClassifierDemo/
├── OnDeviceClassifierDemo_iOS/          # iOS app
│   ├── OnDeviceClassifierDemo/
│   │   ├── ContentView.swift            # Main UI
│   │   ├── ImagePicker.swift            # Photo picker
│   │   └── Assets.xcassets
│   ├── MobileNetV2.mlmodel              # Core ML model (17MB)
│   ├── OnDeviceClassifierDemo.xcodeproj
│   └── README.md
│
├── OnDeviceClassifierDemo_android/      # Android app
│   ├── app/
│   │   └── src/
│   │       └── main/
│   │           ├── java/
│   │           ├── res/
│   │           └── assets/
│   │               └── mobilenet_v2.tflite  # TF Lite model (4MB)
│   ├── build.gradle
│   └── README.md
│
├── .gitignore                            # Combined iOS + Android
└── README.md                             # This file
```

## Getting Started

### iOS Setup

#### Prerequisites
- Xcode 13.0+
- iOS 14.0+ device or simulator
- macOS 11.0+ (Big Sur or later)

#### Installation
```bash
cd OnDeviceClassifierDemo_iOS
open OnDeviceClassifierDemo.xcodeproj
```

Build and run in Xcode (`Cmd + R`)

[See iOS README](./OnDeviceClassifierDemo_iOS/README.md) for detailed setup.

### Android Setup

#### Prerequisites
- Android Studio Arctic Fox or later
- Android 7.0+ (API 24+) device or emulator
- JDK 11+

#### Installation
```bash
cd OnDeviceClassifierDemo_android
./gradlew build
```

Open project in Android Studio and run.

[See Android README](./OnDeviceClassifierDemo_android/README.md) for detailed setup.

## On-Device vs Cloud AI

| Cloud AI | On-Device AI |
|----------|--------------|
| Network required | ✅ Works offline |
| ~500ms latency | ✅ ~50ms latency |
| Privacy concerns | ✅ 100% private |
| ✅ Infinitely scalable | Battery constrained |
| ✅ Complex models | Limited model size |

## Use Cases

Perfect for:
- Real-time image recognition
- Privacy-sensitive applications
- Offline functionality
- Low-latency requirements

## Demo

### iOS
<img src="demos/ios_demo.gif" width="300" alt="iOS Demo">

### Android
<img src="demos/android_demo.gif" width="300" alt="Android Demo">

*Tap "Choose Photo" → select any image → instant label appears with confidence score*

## Performance Comparison

| Platform | Model Size | Inference Time | Memory Usage |
|----------|-----------|----------------|--------------|
| iOS (Core ML) | 17MB | ~50ms | ~30MB |
| Android (TF Lite) | 4MB (quantized) | ~70ms | ~25MB |

## Model Information

- **Model**: MobileNetV2
- **Input**: 224x224 RGB image
- **Output**: 1000 ImageNet class labels with probabilities
- **Training Dataset**: ImageNet
- **Accuracy**: Top-1: ~71%, Top-5: ~90%

## Future Enhancements

- [ ] Support for custom ML models
- [ ] Camera integration for real-time classification
- [ ] History of predictions
- [ ] Export results
- [ ] Object detection support
- [ ] Multi-object classification
- [ ] Batch processing

## Blog Post

Read the full tutorial series on my blog:
- **Part 1**: [Running AI Locally on iPhone — No Cloud Needed](YOUR_BLOG_URL/ios-coreml)
- **Part 2**: [On-Device AI with Android + TensorFlow Lite](YOUR_BLOG_URL/android-tflite) (Coming soon)

## License

MIT License - feel free to use this project for learning and building your own on-device AI apps!

## Author

**Divya Jain**
- Blog: [Mobile With Me](https://deevaa-portfolio.vercel.app/blog)
- LinkedIn: [Your LinkedIn](YOUR_LINKEDIN_URL)
- GitHub: [@djain2405](https://github.com/djain2405)

## Acknowledgments

- MobileNetV2 model from [Apple's Core ML Model Gallery](https://developer.apple.com/machine-learning/models/) (iOS)
- TensorFlow Lite models from [TensorFlow Hub](https://tfhub.dev/) (Android)
- Trained on [ImageNet](http://www.image-net.org/) dataset
- Built with native frameworks: [SwiftUI](https://developer.apple.com/xcode/swiftui/), [Core ML](https://developer.apple.com/documentation/coreml), [Jetpack Compose](https://developer.android.com/jetpack/compose), [TensorFlow Lite](https://www.tensorflow.org/lite)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Resources

### iOS
- [Core ML Documentation](https://developer.apple.com/documentation/coreml)
- [Creating a Core ML Model](https://developer.apple.com/documentation/coreml/creating_a_core_ml_model)
- [SwiftUI Tutorials](https://developer.apple.com/tutorials/swiftui)

### Android
- [TensorFlow Lite Documentation](https://www.tensorflow.org/lite/guide)
- [ML Kit Documentation](https://developers.google.com/ml-kit)
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)

---

If you found this helpful, consider starring the repo! ⭐
