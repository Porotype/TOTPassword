Two-factor authentication example with Vaadin
=======
**tl:dr This example shows how to make a Vaadin application work with Google Authenticator.**

An example using two-factor authentication (also known as two-step verification) in a Vaadin application. This allows the application to add an additional layer of security, by requiring a one-time "TOTP" password to be entered.

To play around with the example project, check out the sources and import the project into your favorite IDE. To launch it locally using jetty plugin issue:

```
mvn package jetty:run
```


Related links
===
TOTP RFC: http://tools.ietf.org/html/rfc6238

TOTP java library: https://github.com/wstrange/GoogleAuth
(there are other implementation on GitHub, and a messy reference implementation in the RFC)

QRCode add-on: http://vaadin.com/addon/qrcode

Google Authenticator
  - Android: https://play.google.com/store/apps/details?id=com.google.android.apps.authenticator2
  - iOS: https://itunes.apple.com/en/app/google-authenticator/id388497605
