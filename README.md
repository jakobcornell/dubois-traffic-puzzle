# Dubois Traffic Puzzle

A traffic puzzle game inspired by Rush Hour, for use by the Dubois Project

## Level format

The level format has been slightly adapted from the original format by Camil Staps, et al. In the new format, the colors are defined in application code, and the unnecessary car count line is removed. Like its predecessor, the format uses x- (horizontal, from left) and y- (vertical, from top) coordinates.

The first integer is the left x-coordinate of the goal car (whose y-coordinate is assumed to be 2). All remaining integers are in groups of four, each consisting of two coordinates: the leftmost/top coordinate (x1, y1), and the rightmost/bottom coordinate (x2, y2). A level might look like this:

	0
	2 1 2 2
	3 1 3 3
	4 2 4 3

This level has its goal car along the left edge of the board, and three other vehicles, positioned vertically, one of which is a truck. Arbitrary whitespace or a single semicolon may be used to separate each pair of integers. Newlines should be avoided when using the SdbNavigator, as updating any fields in a level record will strip newlines from the map. Under this policy, a good way of writing the above level might be `0;2 1 2 2;3 1 3 3;4 2 4 3`.

[An issue](https://github.com/Reggino/SdbNavigator/issues/19) addressing newline handling has been opened on SdbNavigator's GitHub repository, so keep an eye on that if you're interested in newline support.

## Debugging

The app logs stacktraces where I expect interesting errors to occur. If users get messages like "An unexpected error occurred", they should be able to access the corresponding stacktrace by dumping logs through `adb`:

	adb logcat -d -s RushHour

`RushHour` is the debugging tag, set in the `strings.xml` resource file.

If something goes wrong while publishing a mathlete's stats to the database, a coach scan activity launches to prompt the user to find a coach. At this point the application should have printed the stats for that play to the log. The coach can run the above command to get the stats and enter them in the database manually.

## Issues

* Despite my best efforts, the loader fragments in this app do not survive teardown and rebuilding by the system. For example, if the user leaves one of the NFC scanning activities (probably by pressing the home button) while the app is querying the database, the activity will fail to receive a result from the fragment on return, which causes the activity to idle with the loading spinner indefinitely. If this happens, the activity will need to be restarted.

	Also, if the user navigates home during gameplay and relaunches the app, the activity stack will still hold the interrupted session, below the newly launched activity. Navigating back will reveal the old session, potentially leading to an inconsistent state (e.g. if another player has scanned in since). These are the result of design flaws in the app, but as a workaround, try to prevent users from using the home button.

* `SdbInterface#fetchLastPlay(Mathlete)` sorts a mathlete's plays in descending order (lexicographically) by date/time. This works as expected as long as the ISO 8601 format is used (as it is in this app) and all dates recorded use the same offset from UTC (which this app currently does **not** guarantee).

## Resources

Documentation for Amazon SimpleDB was fairly difficult for me to find (and find again later), so for the benefit of future developers:

* [`AmazonSimpleDBClient` javadocs](http://docs.aws.amazon.com/AWSAndroidSDK/latest/javadoc/com/amazonaws/services/simpledb/AmazonSimpleDBClient.html): the only way I've discovered to learn how to make basic queries

* [guide for using SimpleDB's `Select`](http://docs.aws.amazon.com/AmazonSimpleDB/latest/DeveloperGuide/UsingSelect.html)
