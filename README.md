# Dubois Traffic Puzzle

A traffic puzzle game inspired by Rush Hour, for use by the Dubois Project

## Level format

The level format of the original game by Camil Staps, et al. is still supported. Because SdbNavigator completely fails to support newlines in attribute values, the level parser is no longer line-based, so any whitespace can be used for any separator in level maps. (Newlines may still be used in maps, but any update to the corresponding record from SdbNavigator will replace them with spaces.) Semicolons are also allowed as separators in hopes that they can be used to make level maps more readable in SdbNavigator. The parsing is done in the `BoardLoader` class.

[An issue](https://github.com/Reggino/SdbNavigator/issues/19) addressing newline handling has been opened on SdbNavigator's GitHub repository, so keep an eye on that if you're interested in newline support.

## Debugging

The app logs stacktraces where I expect interesting errors to occur. If users get messages like "An unexpected error occurred", they should be able to access the corresponding stacktrace by dumping logs through `adb`:

	adb logcat -d -s RushHour

`RushHour` is the debugging tag, set in the `strings.xml` resource file.

## Issues

* Despite my best efforts, the loader fragments in this app do not survive teardown and rebuilding by the system. For example, if the user leaves one of the NFC scanning activities or rotates the screen while the app is querying the database, the activity will fail to receive a result from the fragment on return, which causes the activity to idle with the loading spinner indefinitely. If this happens, the activity will need to be restarted.
