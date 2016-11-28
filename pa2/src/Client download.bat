
java -Djava.rmi.server.useCodebaseOnly=false -Djava.rmi.server.codebase=file:/C:\Git\SWE-622\pa2\dist\pa2.jar  -Djava.rmi.server.hostname=127.0.0.1 -Djava.security.policy=java.policy -cp pa2.jar;. pa2.Client download "New Folder/New Text Document.txt" "New File.txt"
pause 