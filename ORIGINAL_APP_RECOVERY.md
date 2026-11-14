# Original Lumina Focus recovery and upgrade record — 2026-11-14

## Verified source artifacts
- User uploaded `LuminaFocus.apk` and `LuminaFocus(1).apk`; both SHA-256: `687c3b95805c214d14bcf46b74f8d7a4ec2927797cf51fa4c504b660052d7ea0`.
- APK archive contains `classes.dex`, `AndroidManifest.xml`, assets including privacy and Tor licensing files, native `libtor.so` and `libhev-socks5-tunnel.so`, and `META-INF/FOCUS.RSA`.
- Classes reference `com/lumina/focus/` and Android `VpnService`; original functionality is broader than the minimal prototype currently in this repo.
- Original APK signer subject: `CN=Lumina Focus`; signing certificate SHA-256 fingerprint `BD:91:39:14:59:BC:0A:01:74:19:CB:06:5E:74:19:7F:62:D6:90:9B:4D:55:F8:C3:59:7B:23:D3:0A:2E:82:25`.
- **No original private signing key/keystore was recovered.** An APK contains the public certificate, not the signing private key.

## Critical constraints
- Do not replace or uninstall the existing user's app until a migration/backup plan is verified.
- A seamless Android update needs the same application ID, compatible signing certificate (or valid key rotation), and sufficient versionCode. A newly generated signing key cannot simply overwrite the original APK.
- This repository currently contains a **standalone prototype**, not the original source tree. It lacks the existing app's VPN/communication functionality.
- Avoid claiming YouTube in-stream ads can always be removed with a DNS VPN. Implement browser-level filtering and graceful fallback; no guarantee of universal ad removal.
- Do not commit private signing keys or credentials to this public repository.

## Next engineering actions
1. Recover original Android source project and original signing keystore, if available, or explicitly choose a new signing identity and documented data-migration path.
2. Reconstruct/merge original functionality before releasing an update; validate manifest package ID, version code, permissions, and signing.
3. Build and test on an Android device. Verify playback, focus timer, VPN setup, battery use, and app data retention.
4. Distribute only a tested signed APK and document installation and Quick Share.

## User preference
Keep all work in this GitHub repository, handle technical steps on the user's behalf where possible, make installation phone-friendly, and preserve project decisions for future continuation.
