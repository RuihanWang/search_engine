CS6200
Final Project
README

Language: Java

Dependencies:
Jonathan Hedley's Java library for parsing HTML called jsoup, which can be found at https://jsoup.org/
Lucene Version 4.7

The jsoup and Lucene JAR dependencies can be found in the /lib directory.

Building and Running the Code:

In order to build and run this project, the easiest approach would be to import
this whole directory into an Eclipse project, add the jsoup and Lucene JARs as part of the build path,
then run the top level Main.java class.

Here are some more detailed instructions that I followed on the CCIS machines using Eclipse
if you are unfamiliar with the process. (If you are familiar with this process, then you may
know an easier way to set this up in Eclipse.)

-Open Eclipse.
-Create a new Java project: File -> New -> Project...
-Select "Java Project" in the New Project dialog then click "Next >".
-Enter a Project name, then click "Finish".
-Click File -> Import...
-Select "File System" from the Import dialog then click "Next >".
-Next to the "From directory" input box, click "Browse" then
 find and select this directory then click "OK".
-Check the checkbox next to this directory's name in the left-hand box.
-Next to the "Into folder" input box, click "Browse" then
 select the newly created project then click "OK".
-Click "Finish".
-In the Package Explorer pane on the left, right click the new project
 and click Build Path -> Configure Build Path.
-Click on the "Libraries" tab.
-Click on the "Add External JARs..." button on the right.
-Search for the lib/jsoup-1.9.2.jar in this directory, select it, and click "OK".
-Click "OK".
-Repeat this step for each of the Lucene JARs as well.
-In the menu next to the green run button on the top, click on "Run Configurations...".
-On the left, select "Java Application" and create a New launch configuration.
-Click "Search" next to the Main class input box.
-Select "Main" and click "OK".
-Click "Run"

The program should now start running.

If you prefer to use the command line, then the following commands should work if you
start in this directory in the terminal:

rm -r bin
mkdir bin
javac -d bin -cp "lib/*" $(find ./src -name "*.java")
java -cp "bin:lib/*" Main

