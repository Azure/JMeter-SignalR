# SignalR JMeter Plugin

This plugin allows you to test your SignalR app server, including those using Azure SignalR service.

## Table of Contents

- [Quick startup](#quick-startup)
    - [Local bench](#local-bench)
    - [Distributed bench on Azure Load Testing](#distributed-bench-on-azure-load-testing)
- [Customize](#customize)
    - [Option 1: Modify in JMeter GUI](#option-1-modify-in-jmeter-gui)
    - [Option 2: Build the plugin jar](#option-2-build-the-plugin-jar)
- [Under the hood](#under-the-hood)
    - [JMeter, JMeter plugin and Azure Load Testing](#jmeter-jmeter-plugin-and-azure-load-testing)
    - [SignalR JMeter plugin](#signalr-jmeter-plugin-1)
    - [The repo directory](#the-repo-directory)

## Quick startup

### Local bench

#### Dependencies

1. [JDK 17](https://adoptium.net/temurin/releases/?version=17&package=jdk)
2. [JMeter](https://jmeter.apache.org/download_jmeter.cgi)

> Java 8 supported
>
>Both the SignalR plugin and JMeter are compatible with JDK 8. But JDK 17 is recommended.

#### Steps

1. Download [Jmeter SignalR plugin (JmeterSignalR.jar)](https://github.com/Azure/JMeter-SignalR/releases/tag/v1.0).
   Place the jar in JMeter's lib/ext folder.
2. Clone this repo and start the SignalR server in the examples/SignalRServer folder.
3. Launch JMeter and load the SignalR.jmx script in the examples folder.
4. Click start to begin .
   <kbd> ![SignalR Plugin Local Test](https://github.com/Azure/JMeter-SignalR/assets/16233725/dac69ed3-d1be-4912-87a0-fa22f4425641)</kbd>

### Distributed bench on Azure Load Testing

#### Dependencies

1. [Azure Load Testing](https://learn.microsoft.com/en-us/azure/load-testing/how-to-create-and-run-load-test-with-jmeter-script?tabs=portal)

#### Steps

1. In Azure Load Testing, create a test and select "Upload a JMeter Script".

   <img src="https://github.com/Azure/JMeter-SignalR/assets/16233725/76054a84-95c7-49ae-a9b6-89d8585bc6c4" alt="Azure Load Test Creation" width="65%">

2. Upload the JMeter script and SignalR plugin.

   <img src="https://github.com/Azure/JMeter-SignalR/assets/16233725/3fea8c81-7f17-4d79-90c5-cfeb6ccc42f6" alt="Upload Script and Plugin" width="65%">

3. Override parameters as
   needed: `webAppUrl, connectionCountTotal, groupSize, sendDelayInMilliSeconds, payloadSizeInBytes`

   <img src="https://github.com/Azure/JMeter-SignalR/assets/16233725/f7d66b6e-da05-4430-8350-d9d832f930df" alt="Parameter Override" width="65%">

4. Start the test and view the metrics.

   <img src="https://github.com/Azure/JMeter-SignalR/assets/16233725/238449b4-8443-4177-afc8-bc76291666cb" alt="Start Test" width="65%">

## Customize

### Option 1: Modify in JMeter GUI

<img src="https://github.com/Azure/JMeter-SignalR/assets/16233725/000c3ea3-216f-48c3-9ee5-e0b3c4a82f4d" alt="JMeter GUI Customization" width="90%">

### Option 2: Build the plugin jar

1. Clone the repo and modify the code as needed.
2. Build the jar using:
    - Windows: `.\gradlew.bat jar`
    - Linux/MacOS: `./gradle jar`
3. Find the built `JmeterSignalR.jar` in `build/libs`.
4. Enable the disabled *SendToGroup Java + GUI*.

<img src="https://github.com/Azure/JMeter-SignalR/assets/16233725/29bceb68-8c25-400f-ba40-466256c69f38f" alt="Enable SendToGroup" width="90%">

## Under the hood

### JMeter, JMeter plugin, and Azure Load Testing

- **JMeter**: An open-source benchmark tool, available in GUI and command-line modes. The GUI mode is useful for local
  debugging and modifying JMeter scripts but is less efficient than the command-line mode, which is more suitable for
  intense benchmark.
- **JMeter plugin**: Extends JMeter's capabilities by loading jars in the lib/ext folder. These jars usually include
  samplers, controllers, GUI components, helper functions, and other dependencies.
- **Azure Load Testing**: A service supporting JMeter engine, designed for easy execution of benchmarks across multiple
  JMeter instances. It integrates with Azure Monitor, providing a dashboard for nice metrics of JMeter results.

### SignalR JMeter plugin

- **SignalR client integration**: This plugin overcomes JMeter's lack of native support for the SignalR client. It
  simplifies the testing against SignalR or Azure SignalR services by adding SignalR dependencies into the plugin jar,
  avoiding the need for workarounds like using a websocket plugin.
- **Asynchronous metrics collection**: Adapts to the bi-directional nature of the SignalR protocol, which is different
  from the traditional HTTP request-response model. It buffers `SampleResult` in a memory queue before batching them to
  JMeter listeners.
- **Connection aggregation**: To optimize performance, the plugin aggregates multiple connections into a single JMeter
  thread using a `ConnectionBundle` class. This approach significantly reduces CPU overhead from thread context
  switching, especially valuable when connection counts reach several hundred. In Azure Load Testing, it allows the
  testing of up to **4000** connections per instance with just **1** virtual user.
- **Customization through JSR223 sampler**: Offers flexibility in customizing the SignalR client using the JSR223
  sampler, where groovy is the preferred scripting language. This feature enables users to alter client configurations
  and callbacks in the JMeter GUI.

### The repo directory

- The `examples` folder: Contains a compatible SignalR server and a JMeter script for immediate use and testing.
- Under `src/main/java/azure/signalr/groovy`: Houses groovy scripts identical to those in the JMeter script.
- Under `src/main/java/azure/signalr/java`: Java version for the JSR223 sampler. Those who prefer Java over groovy can
  modify these Java files and rebuild the plugin, then assemble the flow (
  e.g., `connect -> JoinGroup -> Send Message -> collect metrics`) using GUI.
- Under `src/main/java/azure/signalr`, `ConnectionBundle` class wraps a bundle of classes and `SignalRUtil` class offers
  some common help methods.


