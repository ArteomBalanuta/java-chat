# java-chat
Simple client/server chat - using java executors, in/out streams. 

How to run server:

cd /server

mvn clean compile assembly:single

java -jar target/server-1.0-SNAPSHOT-jar-with-dependencies.jar

2020-06-11 - server working poc, may be used via netcat, telnet :)  
2020-06-12 - gui terminal window has been adde (poc)

2020-08-00 - fixes, refactor, 
             Diffie-Hellman key exchange support has been added.
