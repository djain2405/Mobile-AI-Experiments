//
//  ContentView.swift
//  SmartLabelMagic_iOS
//
//  Created by Divya Jain on 11/3/25.
//

import SwiftUI
import PhotosUI
import CoreML
import UIKit


struct ContentView: View {
    @State private var selectedItem: PhotosPickerItem?
    @State private var image: UIImage?
    @State private var result: ResultData = .idle
    @State private var isLoading = false
    
    struct ResultData: Equatable {
        var text: String = "Pick a photo to classify"
        var confidence: Double = 0
        static let idle = ResultData()
    }
    
    var resultColor: Color {
        switch result.confidence {
        case 0.8...1.0: return .green
        case 0.5..<0.8: return .yellow
        default: return .gray
        }
    }
    
    var body: some View {
        VStack(spacing: 20) {
            Text("✨ Smart Label v2")
                .font(.title2.bold())
            
            ZStack {
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color(.secondarySystemBackground))
                    .frame(height: 280)
                
                if let img = image {
                    Image(uiImage: img)
                        .resizable()
                        .scaledToFit()
                        .frame(height: 260)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .padding(8)
                        .transition(.opacity)
                } else {
                    Text("No image selected")
                        .foregroundStyle(.secondary)
                }
            }
            
            // result card
            ZStack {
                RoundedRectangle(cornerRadius: 14)
                    .fill(resultColor.opacity(0.15))
                    .overlay(
                        RoundedRectangle(cornerRadius: 14)
                            .stroke(resultColor, lineWidth: 1)
                    )
                    .frame(height: 90)
                    .animation(.spring(), value: result.confidence)
                
                if isLoading {
                    ProgressView().scaleEffect(1.2)
                } else {
                    Text(result.text)
                        .font(.headline)
                        .multilineTextAlignment(.center)
                        .transition(.scale.combined(with: .opacity))
                        .id(result.text)
                        .padding()
                }
            }
            .padding(.horizontal)
            
            PhotosPicker(selection: $selectedItem, matching: .images) {
                Text("Choose Photo")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color.blue)
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(.horizontal)
        }
        .padding()
        .onChange(of: selectedItem) { _, item in
            Task { await process(item) }
        }
    }
    
    func process(_ item: PhotosPickerItem?) async {
        guard let item else { return }
        isLoading = true
        result = .init(text: "Analyzing…", confidence: 0)
        if let data = try? await item.loadTransferable(type: Data.self),
           let uiImage = UIImage(data: data) {
            image = uiImage
            let (label, conf) = await classify(uiImage)
            let prefix = prefixFor(conf)
            let haptic = UINotificationFeedbackGenerator()
            haptic.notificationOccurred(conf >= 0.8 ? .success : .warning)
            withAnimation(.spring(response: 0.4, dampingFraction: 0.8)) {
                isLoading = false
                result = .init(text: "\(prefix) \(label) • \(Int(conf * 100))%", confidence: conf)
            }
        }
    }
    
    func classify(_ image: UIImage) async -> (String, Double) {
        guard let pb = image.pixelBuffer(width: 224, height: 224) else { return ("Unknown", 0) }
        do {
            let config = MLModelConfiguration(); config.computeUnits = .all
            let model = try MobileNetV2(configuration: config)
            let output = try model.prediction(image: pb)
            let label = output.classLabel
            let conf = output.classLabelProbs[label] ?? 0
            return (label, conf)
        } catch { return ("Error", 0) }
    }
    
    func prefixFor(_ c: Double) -> String {
        switch c {
        case 0.8...1.0: return "Definitely"
        case 0.5..<0.8: return "Probably"
        default: return "Maybe"
        }
    }
}
