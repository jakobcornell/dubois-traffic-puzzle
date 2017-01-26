# Dubois Traffic Puzzle

A traffic puzzle game inspired by Rush Hour, for use by the Dubois Project

## Debugging

The app logs stacktraces where I expect interesting errors to occur. If users get messages like "An unexpected error occurred", they should be able to access the corresponding stacktrace by dumping logs through `adb`:

	adb logcat -d -s RushHour

`RushHour` is the debugging tag, set in the `strings.xml` resource file.

## Issues

* Despite my best efforts, the loader fragments in this app do not survive teardown and rebuilding by the system. For example, if the user leaves one of the NFC scanning activities or rotates the screen while the app is querying the database, the activity will fail to receive a result from the fragment on return, which causes the activity to idle with the loading spinner indefinitely. If this happens, the activity will need to be restarted.
