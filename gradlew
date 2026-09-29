#!/bin/sh
# Gradle wrapper script
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
APP_HOME=`pwd -P`
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
JAVA_OPTS=""
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'
exec "$JAVACMD" $JAVA_OPTS $DEFAULT_JVM_OPTS \
  -classpath "$CLASSPATH" \
  org.gradle.wrapper.GradleWrapperMain "$@"
