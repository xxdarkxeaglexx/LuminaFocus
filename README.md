# Lumina Focus Preview
Separate Android application ID com.lumina.focus.preview. Based on the recovered Call Guard and Focus Browser source. Selecting this app as Android’s call screening provider switches that role to this app. This preview does not include the 1.8 Tor and VPN features.

## Build and pair
Build the repository with the included GitHub Actions workflow, or run Gradle 8.13 assembleDebug with JDK 17 and Android SDK 36. Install the same APK on two Android phones with a current Android System WebView. Open Private chat on each phone and connect both to the same trusted Wi-Fi network. Guest Wi-Fi may isolate devices.

On phone A, tap Create invite and copy the code to phone B using an existing messaging app. B pastes it and taps Use pairing code. Copy B’s reply back to A and tap Use pairing code on A. Compare every group of the security code in person or by a trusted voice call. Confirm on each phone. End the session if codes differ. Pairing codes include temporary network information and should be shared privately.

WebRTC encrypts the data channel using DTLS. Every session uses fresh browser-generated credentials. Text and photo messages are held in memory, and each can be opened once. Unopened items expire after two minutes. Open items close after ten seconds. Leaving the screen discards received items. Original photos stay on the sender’s device. A recipient can still use another camera. No offline delivery, contact accounts, remote push notifications or cloud relay is implemented. Chat is a prototype and has not been independently security-reviewed.

The browser filters common ad domains and selected YouTube advertising requests. Streamed ads can still appear. Call screening uses the existing bundled report lists. Website traffic and website storage are separate from the in-memory chat.
