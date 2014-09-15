gradle-plugin
=============

The plugin is currently in beta.

Building plugin and running tests
=================================

First, you will have to install gradle (see [1]).

Unfortunately, there is a dirty hack that needs to be done before you can test it.

The reason is that the current version of gradle (that is 2.1) uses slf4j v1.7.5 for logging and that version has a bug (see [2]). Unfortunately JiBX's CodeGen will not work because of this bug. That bug was fixed in v1.7.6.

I've already informed gradle team about it, and they've already accepted my patch, so starting with gradle v2.2 everything will be fine (see [3]). For now, you will have to download slf4j v1.7.6+ (see [4], I've used v1.7.7) and replace the following jars in $GRADLE_HOME/lib directory:

jcl-over-slf4j-1.7.5.jar

jul-to-slf4j-1.7.5.jar

log4j-over-slf4j-1.7.5.jar

slf4j-api-1.7.5.jar

Please keep in mind that you will have to keep the name intact because gradle is explicitly listing all its jars. To do this just rename *-1.7.7.jar to *-1.7.5.jar and replace the files.

That done, you can start testing it:

First, you need to build the plugin itself and install it into your local maven repo:

cd jibx-gradle-plugin

gradle install

Now you either can run some tests:

cd test-suite

gradle test

or use it yourself in a new gradle project. The documentation is not yet ready, but you can look at the tests from test-suite to get some examples.

[1] http://www.gradle.org/

[2] http://bugzilla.slf4j.org/show_bug.cgi?id=279

[3] https://issues.gradle.org/browse/GRADLE-3167

[4] http://slf4j.org/