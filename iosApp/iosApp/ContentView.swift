import SwiftUI

struct ContentView: View {
	var body: some View {
		VStack(spacing: 12) {
			Text("Learn Mokshan")
				.font(.title)
			Text("The iOS application is not implemented yet.")
				.foregroundStyle(.secondary)
				.multilineTextAlignment(.center)
		}
		.padding()
	}
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
