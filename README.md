[![](https://jitpack.io/v/kikkia/Dislog.svg)](https://jitpack.io/#kikkia/Dislog)

# Dislog
Java/Kotlin library for sending logs to discord.

## Adding Dislog to your project
### Maven 
Add the jitpack repository to your `pom.xml` file if you do not already have it
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
```

Then you can add the Dislog dependancy to your `pom.xml` as well  
```xml
<dependency>
  <groupId>com.github.kikkia</groupId>
  <artifactId>Dislog</artifactId>
  <version>2.0.4</version>
</dependency>
```

### Gradle
Add the jitpack repostiory to your `build.gradle` file
```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Then you can add the repository
```gradle
dependencies {
	        implementation 'com.github.kikkia:Dislog:2.0.4'
	}
```

## Usage

To use Dislog you can to build a DislogClient builder like so
```java
    DislogClient client = new DislogClient.Builder()
                .addWebhook(LogLevel.DEBUG, "Debug webhook url")
                .build();
```
The only things you need to set on the client are the webhook urls for the levels you want to log out to discord. You can add as many  webhook urls per log level as you want. Supported log levels are `DEBUG, INFO, WARN, ERROR, FATAL, TRACE`

To send a log you need to construct a Log object. A log contains 3 things, a message, a level and an optional Exception.

#### NOTE:
Currently each dislogClient that is created will spawn 1 thread per webhook. Make sure to only make 1 dislog client that is shared between all of your loggers. Spawning more, especially dynamically, can lead to thread leaks. In the future I will impliment 1 thread per webhook, independant of the number of dislog clients.

## Settings
There are various settings you can setup when building your Dislog client, they are:

- Name - The username that the webhook displays in discord.
- Identifier - An identifier that is shown before every log. (Good for use with multiple hosts/vms)
- Avatar Url - The url for the Dislog webhook.
- Print Stack Trace - A boolean that enables/disables printing the stack trace with the log when there is an Exception present. 
- TimeZone - set the timezone to standardize the timestamps on each log.
- Max Retries - set how many times to retry sending a log before throwing it out. (Default = 5)
- Poll rate - Each webhook has a thread that polls a log queue, this is the period of which it will poll at. (Default = 100ms)

## MDC
Each log is printed with the contents of the MDC when the log is sent. The MDC is a powerful tool to manage values you want to send with logs. [Quick guide on how to use the MDC](https://www.baeldung.com/mdc-in-log4j-2-logback)
