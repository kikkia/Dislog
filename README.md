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
  <version>1.0.0</version>
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
	        implementation 'com.github.kikkia:Dislog:1.0.0'
	}
```

To use Dislog you need to build a DislogClient builder like so
```java
    DislogClient client = new DislogClient.Builder()
                .setDebugWebhookUrl("Debug webhook url")
                .setInfoWebhookUrl("Info webhook url")
                .setWarnWebhookUrl("Warning webhook url")
                .setErrorWebhookUrl("Error webhook url")
                .build();
```
The only things you need to set on the client are the webhook urls for the levels you want to log out to discord.

To send a log you need to construct a Log object. A log contains 3 things, a message, a level and an optional Exception.  

### Levels
There are 4 LogLevels:
- Error - Represents a fatal error
- Warning - Represents something that may not have been of error severity but needs attention
- Info - Informational logs
- Debug - Logs for the purpose of debugging.   
Logs are sent to a channel based on their level. You can give each their own channel or send them all to one channel, thats up to you. 

### Settings
There are various settings you can setup when building your Dislog client, they are:

- Name - The username that the webhook displays in discord.
- Identifier - An identifier that is shown before every log. (Good for use with multiple hosts/vms)
- Avatar Url - The url for the Dislog webhook.
- Print Stack Trace - A boolean that enables/disables printing the stack trace with the log when there is an Exception present. 
- TimeZone - set the timezone to standardize the timestamps on each log.

### MDC
Each log is printed with the contents of the MDC when the log is sent. The MDC is a powerful tool to manage values you want to send with logs. [Quick guide on how to use the MDC](https://www.baeldung.com/mdc-in-log4j-2-logback)
