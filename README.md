# Bluemix-IoT-android-wear
IoT starter app for Bluemix using Android Wear

# Try it yourself #

If you have Android Watch, please, go ahead and try it!
There is how:

# Step 0 - Prerequisites #

Android Studio - download [here](https://developer.android.com/sdk/index.html)
Android Wear watch (if you don't have one, there is [how to simulate Android Watch](https://developer.android.com/training/wearables/apps/creating.html#SetupEmulator) )
Android phone or tablet (could be simulated too - [here](https://developer.android.com/tools/devices/index.html) )

# Step 1 - Bluemix side #
[Register to Bluemix](http://ibm.biz/BluemixCEE)
Go to Bluemix and set up the environment (Please follow Step 1 [here](http://m2m.demos.ibm.com/iotstarter.html) )

# Step 2 - Android side #
Download this app from GitHub ( [https://github.com/IBMCloudiLab/Bluemix-IoT-android-wear](https://github.com/IBMCloudiLab/Bluemix-IoT-android-wear)) into your Android Studio
You can use Git, that is build in Android studio or any other Git client.

Create an APK install file in Android Studio
Build - Generate Signed APK
After this select:
-> Module: App
-> If you don't have a keystore with signing key for app - please create them with the wizard
-> Finish

Install it into your device and run the app (it takes a while to synchronize the Watch with the phone, to be sure wait +-10 seconds after the installation to run it)

# Step 3 - IoT Foundation #
Register your device (Please follow Step 2 [here](http://m2m.demos.ibm.com/iotstarter.html))
Go to your dashboard of IoT service and select your device

Enjoy!

Big thanks to pocmo for Sensor dashboard app from which I have used some parts of code [https://github.com/pocmo/SensorDashboard](https://github.com/pocmo/SensorDashboard) 