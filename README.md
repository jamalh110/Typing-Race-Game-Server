# Typing-Race-Game-Server

Demo Video: https://youtu.be/MvyhU7ZtN7w

This is the Game Server. It is built using a lightweight non blocking I/O framework called Netty. 

It consists of two input listeners: One for the client and one for the server.

The server channel will eventually use TLS.

The client channel is built using HTTP and TCP. I used TCP because all of the data being sent needs to arrive, and I used HTTP because on cell networks, HTTP is the only feasible protocol that can be trusted not to be blocked.

The server consists of a matchmaking thread and 1-10 game threads. 
When the server receives a request from the login server to make a match for a player, it puts that request in a BlockingQueue that the matchmaking thread reads from.
When two players are in the queue, the matchmaking thread creates a Game object, assigns the players to that Game, and sends the Game to the game thread with the least activity. 
The client then connects to the match and sends and receives data from the server.

I used blocking queues throughout the program to ensure that everything is thread safe. I also used in-memory hashmaps as databases so writing and retrieving data is fast.

