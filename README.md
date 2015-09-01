Desktop Trello Bugger
==================

## Instructions for development ##

### Windows ###

	C:\Users\Alan\t>git clone https://github.com/alanboy/trello.git
	C:\Users\Alan\t\trello>git submodule init
	C:\Users\Alan\t\trello>git submodule update --init --recursive

Build trello4j and add maven and requirements to your path:

	SET PATH=%PATH%;c:\Users\Alan\maven\bin\
	SET JAVA_HOME=c:\Program Files\Java\jdk1.7.0_51\
	C:\Users\Alan\t\trello\trello4j>mvn install -DskipTests

You should end up with **trello4j** in a JAR: `C:\Users\Alan\t\trello\trello4j\target\trello4j-1.0-SNAPSHOT.jar`

Add gradle to your path:

	SET PATH=%PATH%;C:\Users\Alan\gradle-2.2.1\bin\

Now you can build and run:

	C:\Users\Alan\t\trello>gradle build
	C:\Users\Alan\t\trello>gradle run

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
						^
						|
	/---------\   /---------\   /----------\   /----------\
	|         |   |         |   |          |   |          |
	|TrelloCmd|   |UIServer |   |ListPanel |   |CardButton|
	| *       |   |  has-a  |   |  is-a    |   |          |
	|         |<->| JDialog |<->|   JPanel |<->|          |
	|         |   |         |   |          |   |          |
	|         |   |         |   |          |   |          |
	|         |   |         |   |          |   |          |
	\---------/   \---------/   \----------/   \----------/
	      ^            ^
	      |            |
	/-------------------------\
	|                         |
	|TrelloClient             | // trello client is the only thread waking up every second
	|                         |
	\-------------------------/
			^
			|
	/-------------------------\   /--------------\
	|                         |   |              |
	|TrelloCache**            |<->|trello4j      |
	|                         |   |              |
	\-------------------------/   \--------------/
	
	
	* Application entry point
	** Not implemented



	/----------------------------------\
	| JDialog                          |
	|                                  |
	| /---------\  /-----------------\ |
	| |         |  |                 | |
	| |ListPanel|  |ListPanel        | | ListPanel extends JPanel
	| |         |  |                 | |
	| |         |  | /------------\  | |
	| |         |  | |            |  | |
	| |         |  | | CardButton |  | | CardButton extends JButton
	| |         |  | |            |  | |
	| |         |  | |            |  | |
	| |         |  | \------------/  | |
	| |         |  |                 | |
	| \---------/  \-----------------/ |
	|                                  |
	\----------------------------------/
	
