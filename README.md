Desktop Trello Bugger
==================

## Instructions for development ##

### Clone ###
C:\Users\Alan\\**t**>`git clone https://github.com/alanboy/trello.git`

C:\Users\Alan\t\\**trello**>`git submodule init`

C:\Users\Alan\t\\**trello** >`git submodule update --init --recursive`

### Build trello4j ###
Add maven and requirements to your path:

	SET PATH=%PATH%;c:\Users\Alan\maven\bin\
	SET JAVA_HOME=c:\Program Files\Java\jdk1.7.0_51\

C:\Users\Alan\t\trello\\**trello4j**>`mvn install -DskipTests`

You should end up with trello4j in a JAR:
`C:\Users\Alan\t\trello\trello4j\target\trello4j-1.0-SNAPSHOT.jar`

### Build trello client ###
Add gradle to your path:

	SET PATH=%PATH%;C:\Users\Alan\gradle-2.2.1\bin\

C:\Users\Alan\t\\**trello**>`gradle build`

C:\Users\Alan\t\\**trello**>`gradle run`

Follow instructions for usage.

## Instructions for usage ##

Download latest trello client: [https://github.com/alanboy/trello/releases/download/v0.1.0.0/trello-0.1.0.0.zip](https://github.com/alanboy/trello/releases/download/v0.1.0.0/trello-0.1.0.0.zip)

Decompress to *C:\\trello\\*

Create a config.json file in your user home directory:

**c:\users\alan\trello.json**

    {
        "usertoken" : "60a1bb704dab4b885ae48e8493705cc0b876288600000000",
        "lists"  : [ "51379a57cb9468d000000000" ]  
    }

To get a user token go to: [https://trello.com/1/authorize?key=c67c1cdec3b70b84a052b4d085c15eb1&expiration=30days&name=trelloc&response_type=token&scope=read,write,account](https://trello.com/1/authorize?key=c67c1cdec3b70b84a052b4d085c15eb1&expiration=30days&name=trelloc&response_type=token&scope=read,write,account) click *Allow* to get a token.

Run it in `-b` mode to show all lists you have access to: `java -cp C:\trello\ -jar C:\trello\trello-0.0.1-SNAPSHOT.jar -b`

You will get something like this: 

	Board: Projects - 543f78b9933537aeaca5de35
	    List: Trello client - 5457d632be8a7c0cb6f217e8
	    List: OmegaUp - 543f78b9933537aeaca5de37
	    List: Ideas - 544814850fe58ee565d1a7b2
	    List: wtrace - 5441879841a3f4b1469bb4ef
	    List: Teddy - 543f78b9933537aeaca5de38
	    List: Java Cert App - 54db8cd04d42996613cd518b

Use those list ids to put in your config.json file.

Finally (!), run the client: `javaw -cp C:\trello\ -jar C:\trello\trello-0.0.1-SNAPSHOT.jar` or just do `gradle run`


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
						|
	/---------\   /---------\   /----------\
	|         |   |         |   |          |
	|TrelloCmd|   |UIServer |   |ListPanel |
	|         |   |         |   |          |
	|         |<->|         |<->|          |
	|         |   |         |   |          |
	|         |   |         |   |          |
	|         |   |         |   |          |
	\---------/   \---------/   \----------/ 
	      ^            ^
	      |            |
	/-------------------------\
	|                         |
	|TrelloClient             |
	| (com with trello.com)   |
	|                         |
	\-------------------------/


