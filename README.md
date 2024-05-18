# Desktop Trello Bugger

## Windows

- Install Maven [version 3.9.6](https://downloads.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip)
- Install Gradle [version 6.8](https://gradle.org/next-steps/?version=6.8&format=bin)

Add Maven and Gradle to your path.

```
SET PATH=%PATH%;c:\Users\Alan\maven\bin\;c:\users\alan\gradle-2.2.1\bin\
```

Clone and initialize the repo:

    C:\source>git clone https://github.com/alanboy/trello.git
    C:\source\trello>git submodule init
    C:\source\trello>git submodule update --init --recursive

Build [trello4j](https://github.com/joelso/trello4j):

    SET JAVA_HOME=c:\Program Files\Java\jdk1.7.0_51\ # Don't know that this is needed anymore
    C:\source\trello\trello4j>mvn install -DskipTests

You should end up with **trello4j** in a JAR: `C:\source\trello\trello4j\target\trello4j-1.0-SNAPSHOT.jar`

Now you can build and run:

    C:\source\trello>gradle build
    C:\source\trello>gradle run

## Linux ##

trelloc uses javafx and a package of javafx is not included in openjdk which is what most distributions have. Download an Oracle JDK from http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html and expand locally  then create a file a `gradle.properties` with will containg something like this:

    org.gradle.java.home=/home/alan/Downloads/jdk1.8.0_45/

then use `gradle build` and `gradle run` as above.

## Advanced options/command line ##

Run it in `-b` mode to show all lists you have access to: `java -cp C:\trello\ -jar C:\trello\trello-0.0.1-SNAPSHOT.jar -b`

You will get something like this:

    Board: Projects - 543f78b9933537aeaca5de35
        List: Trello client - 5457d632be8a7c0cb6f217e8
        List: OmegaUp - 543f78b9933537aeaca5de37
        List: Ideas - 544814850fe58ee565d1a7b2
        List: wtrace - 5441879841a3f4b1469bb4ef
        List: Teddy - 543f78b9933537aeaca5de38
        List: Java Cert App - 54db8cd04d42996613cd518b


### Debuggging trelloc ###

    jdb -classpath build\libs\trello-0.0.2.jar TrelloCmd

put a bp in a funtion

    stop in TrelloClient.doWork
    run
    use src\main\java
    catch all *

## Trello Architecture ##

                  /---------\
                  |         |
                  |Simple   |
                  | Browser |
                  |         |
                  |         |
                  |         |
                  |         |
                  \---------/
                        |
    /---------\   /---------\   /--------------\   /----------\
    |         |   |         |   |              |   |          |
    |TrelloCmd|   |UIServer |   |ContainerPanel|   |ListPanel |                     /------------\
    | *       |   |  has-a  |   |   is-a       |   |  is-a    |                     |            |
    |         |<->| JDialog |<->|  JPanel      |<->|   JList  |<-- rendered via --->| CardButton |
    |         |   |         |   |              |   |          |                     |    is-a    |
    |         |   |         |   |              |   |          |                     |    JButton |
    |         |   |         |   |              |   |          |                     |            |
    |         |   |         |   |              |   |          |                     \------------/
    \---------/   \---------/   \--------------/   \----------/
          |            |
    /-------------------------\
    |                         |
    |TrelloClient ***         |
    |                         |
    \-------------------------/
            |
    /-------------------------\   /--------------\
    |                         |   |              |
    |TrelloCache**            |<->|trello4j      |
    |                         |   |              |
    \-------------------------/   \--------------/

    * Application entry point
    ** Not implemented
    *** Wakes up every n seconds

    /-------------------------------------\
    | JDialog                             |
    |                                     |
    |         /------------------------\  |
    |         |   ContainerPanel        | | ContainerPanel extends JPanel
    |         |                         | |
    |         |     /-----------------\ | |
    |         |     |                 | | |
    |         |     |ListPanel        | | | ListPanel extends JList
    |         |     |     (JList)     | | |
    |         |     |                 | | |
    |         |     | /------------\  | | |
    |         |     | |            |  | | |
    |         |     | | Item       |  | | |
    |         |     | |            |  | | |
    |         |     | |            |  | | |
    |         |     | \------------/  | | |
    |         |     |                 | | |
    |         |     \-----------------/ | |
    |         \-------------------------/ |
    |                                     |
    \-------------------------------------/

