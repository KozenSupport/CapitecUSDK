# Capitec Customer Demo 1.0.0

This package contains the customer-visible Android demo/application source and
the binary dependencies required to build it. It intentionally does not contain
the private `capitec-api` implementation source.

The prebuilt `demo-release.apk` is the signed demonstration build. The source
project's Gradle build uses the customer's own local signing configuration;
its debug APK is for compilation checks, not a replacement for the supplied
POS demonstration build.

The main screen's language button switches between English and Simplified
Chinese. The PIN demo follows the same language selection.

## Build

Configure `sdk.dir` in `local.properties`, then run:

```bash
./gradlew :app:assembleDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## PIN coordinate demo

On a test POS device, open the app and tap **PIN 坐标适配测试** on the main screen.
The page lays out the keys, passes their screen-pixel rectangles through
`startInputPin(Bundle, PinKeyCoordInfo[], PinpadInputCallback)`, and starts PIN
input automatically. It shows input length, then the SDK result and encrypted
PIN Block; it never displays the clear PIN.

The included test flow uses online PIN, MK/SK TDES, ISO 9564-0, test PAN
`6217680206725660`, and TPK index 12. A test TPK must already be provisioned
at index 12 on the device. No key material is bundled in the JAR, APK, or source
package. Use a test PIN only; this demo does not submit a transaction to a host.

The coordinate overload is a compatibility adapter over FinancialLib 1.5.0's
existing `Map<String, View>` API, not a new vendor-native FinancialLib method.

## Dependency boundary

- `app/src`: customer-visible integration, UI, test flow and resources.
- `app/libs/capitec-api.jar`: compiled Capitec contracts and implementation.
- Other files under `app/libs`: required distributable device/vendor runtime dependencies.
- `xcheng-framework.jar` is platform/internal-only and is deliberately excluded.
- The private `capitec-api/src` directory is not part of this package.

The supported integration entry remains:

```java
CapitecInterfaces interfaces = new CapitecInterfaces(context);
```

Do not copy implementation classes from the JAR into the app source. Changes to
the binary API should be delivered as a new version of `capitec-api.jar`.
