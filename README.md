# Airtel UPI Listener

Build the APK using GitHub Actions; Android Studio is not required.

1. Create a GitHub repository and upload this project.
2. Push to main/master or run Actions -> Build Android APK -> Run workflow.
3. Download the `AirtelUPIListener-debug` artifact.
4. Install the APK on the merchant phone.
5. Open it, configure the bot endpoint and webhook secret.
6. Enable Notification Access.

Default bot endpoint:
https://agni.ender.co.in:45850/api/upi/payment

The bot endpoint must be reachable from the phone. Use HTTPS for production.
