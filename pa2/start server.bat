set path=C:\Program Files\Java\jre1.8.0_112\bin;
start rmiregistry
cd C:\Git\SWE-622\pa2\dist
java -Djava.rmi.server.codebase=file:/C:\Git\SWE-622\pa2\build\classes\pa2/  -Djava.rmi.server.hostname=127.0.0.1 -Djava.security.policy=java.policy -cp pa2.jar;. pa2.Server 
pause