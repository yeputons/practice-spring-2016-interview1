This is an interview problem which was asking to implement a network client-server two player game, with
one process written in Java/Kotlin and another process written in Python.

I've implemented server in Java (it handles all game logic) and client in Python (it only makes random turns).
To run and test:
# Compile server with `mvn package`
# Run server with `java -jar target/interview1-1.0-SNAPSHOT-jar-with-dependencies.jar -p 1234`
# Run client with `python python_client/client.py localhost 1234`

There is also some logging in both applications which can be enabled with `--verbose` and `--debug` keys.
