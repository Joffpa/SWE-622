Joffrey Pannee
SWE 622 
Programming Assignment 1


Instructions:

pa1.jar is located in the dist folder. under the dist folder are a Server and Client folder that
contain identical copies of the pa1.jar file as well as .bat files that give example to its usage.
You are free to move the pa1.jar file into any directory you wish for both the client and server, the Client/server folders are only included for convenience.

The location of the .jar file acts as the root path for both the client and server.

The upload feature creates directories if they do not exist.

All path parameters should be specified relative to the .jar file for both client and server.

The shutdown command will wait until all other active client threads are finished before shutting down.

The client will create UploadLog.txt and DownloadLog.txt as needed in
the same folder as the client's pa1.jar to track incomplete uploads/downloads.

Commands (windows):

Server:
java -jar pa1.jar server start <port num> 


Client:
SET PA1_SERVER=localhost:<port num>  //Always run this before any client command
java -jar pa1.jar client upload <path_on_client> </path/filename/on/server>
java -jar pa1.jar client download </path/existing_filename/on/server> <path_on_client>
java -jar pa1.jar client dir </path/existing_directory/on/server>
java -jar pa1.jar client mkdir </path/new_directory/on/server>
java -jar pa1.jar client rmdir </path/existing_directory/on/server>
java -jar pa1.jar client rm </path/existing_filename/on/server>
java -jar pa1.jar client shutdown
