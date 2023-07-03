# Bisq2 Web UI

## What this project is about

This is a subproject from Bisq2. It adds a web-ui to the project so Bisq2 can be used inside a browser. Target is that this project can be installed on Personal Node like Start9, Umbrel,... or any selfhosted server. The user shall be able to access the Node through a browser.

The web-ui is an option besides the desktop UI but not a replacement. Its intended to have a scaled down version of the desktop UI with fewer bells and whistles. 

## How to run it
Bisq2 Web UI is a standard JEE web project. To see the full interaction, you will need to start up more than one node to form a small Bisq network. Actually you need:
- 2 seed nodes, which tell the client where to find other clients.
- one or more desktop clients
- and this web app as second client.

Actually the desktop client may access the publicly available price nodes as well, but they are not necessary to run the web ui.

## Prerequisites
- Java 11 or higher
- Tomcat 9 or higher
- Gradle 7.5
- Git (obviously)
- (Optionally): Intellij Ultimate

### Running/Debugging In Intellij Ultimate With Tomcat in Development Mode
- Download and unpack the newest [Tomcat 9](https://tomcat.apache.org/download-90.cgi).
- Open this project in Intellij Ultimate.
- In the subdirectory <$PROJECT-DIR>/.run there are several Run-configurations for your convenience. In Intellij Ultimate, in the 'Select Run/Debug Configuration' start a seed node and wait until it compiles and has started. then start the other seed node and the desktop client and web ui project configuration.
- point your browser to http://localhost:8080
### Manually setting up a web container and building a war file
The Bisq2 sourcecode is downloaded into the subdirectory <$PROJECT-DIR>/bisq. There is a description on how to set up Bisq2 with seednodes and desktop clients in <$PROJECT-DIR>/bisq/README.md
To build a war file you can run the gradle target 

```bash
./gradlew war
```
Then deploy the resulting war-file in any JEE compliant web container, like Tomcat, Jetty,...

Point your browser to http://localhost:8080

## How to contribute

Contribution are always welcome. If you read through this doc already, you are probably a Java dev and we are looking for your help. Best way is to meet us at our [Matrix room](https://matrix.to/#/#bisq.v2.dev:bitcoin.kyoto) . 

If you find any error in this document or have problems following it, please let us know so we can improve it. And maybe fixing this doc could be your first contributing?!