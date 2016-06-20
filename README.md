# Multicast-Chat-Example
Multicast Chat Example implemented in 2013.

## Project definition
The purpose of this project is adapting previously developed chat example to communicate over Multicast IP. This new version of the chat will have the same basic chat functionality than the UDP one, but the architecture will be based in a __peer-to-peer__ schema, where the server roll disappears. Therefore, all communications will be an interchange of messages between members of a multicast group.

## Design
### Functional Description
The features of the chat are:
* Messages will have a maximum length (buffer size: 2048kB).
* Nicknames can't content some characters, such as "/", in order to prevent errors.
* Messages will be received dynamically (there is a thread listening permanently without blocking the interface).
* Nicknames can't be repeated.

### Protocol
The protocol will use the following codes and message types:
* `0`: receive connection.
* `1`: receive disconnection.
* `2`: ask for connected usernames.
* `3`: receive connected usernames.
* `4`: receive chat request.
* `5`: receive chat request answer.
* `6`: receive chat closure.
* `7`: receive message.
* `8`: send connected usernames.

Message | Description
--------------------------- | ---------------------------
`0`nick | It's received when a new user connects. The processing of this message adds the name to the connected users list.<br>When a new user wants to connect, the client asks for the names of the connected users and checks if the name is in use before connecting.
`1`nick | It's received when a user disconnects. The processing of this message removes the name of the connected users list.
`2` | Sends an "8" asking for the connected user names.
`3`nick`/`nick`/`... | It's received when an user asks for the connected usernames. It's ignored if the user is connected.
`4`nick`/`otherNick | Ignored if the nick isn't user's nick. It's received when _otherNick_ user sends a chat request to _nick_. If the receptor of the request is already chatting or waiting for a chat request answer, the request is refused automatically.
`5`nick`/`otherNick`/`answer | Ignored if the nick isn't user's nick. It's received when _otherNick_ user answers to the chat request sent by _nick_. The answer can be _true_, _refused_ or _chatting_.
`6`nick | Ignored if the nick isn't user's nick. It's received when the person chatting with the user closes the chat. It closes the chat.
`7`nick/text | Ignored if the nick isn't user's nick. It's received when the person chatting with the user sends a message.
`8` | It's received when a new user wants to connect and asks for the names of the connected users. It sends a "3" message.

All clients will send the messages to the same IP (default: 225.0.0.0) and port (default: 6789) and read from them.

### Failure Model
Action which causes the failure | Failure | Possible solution
--------------------------- | --------------------------- | ---------------------------
Message longer than the buffer size | Incomplete message |Trunk the message
A user connects with a username in use | ID theft | Check if the nick is already in use and don't allow the user to use it if it is already in use
Send a message | Receive your own message | Check the nick of the received messages and discard your own messages
Close the window without disconnecting | User permanently connected | Automatically disconnect when the window closes or not allowing to close the window if connected
A user tries to open a chat with another user who is already chatting | The user gets blocked | The server refuses the request automatically
A user tries to open a chat and the other user doesn't answer to the request | The user who tries to open the chat gets blocked | Set a timeout
