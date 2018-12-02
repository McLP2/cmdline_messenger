# Commandline Messenger
**A simple end to end encrypted command line messenger.**

## DISCALIMER
*This is a project for fun and learning. I am not responsible for any damage that may be caused by the use of this software. Also I am not liable for any vulnerabilities or similar.*

## Usage
### Server
To setup a server, you must [download](https://github.com/McLP2/cmdline_messenger/releases) the latest server.jar and keygen.jar. Put the files into an empty directory, then run keygen.jar. This will create two files. Make sure to keep the prvkey-file secret and in the same folder as server.jar, but distribute the svrkey-file to your clients.

To run the server, you need to run `java -jar server.jar <port>` in a command line. Switch `<port>` with the port you want the server to run on. Open this port in your firewalls and tell your users, which port you chose and what your IP-address/hostname is.
### Client
#### Connect
To connect to a server, you must have following things: 
* the current [client.jar](https://github.com/McLP2/cmdline_messenger/releases) file
* the server's svrkey-file
* the port and IP-address/hostname of the server.

Put the client.jar and svrkey files into the same empty directory. Then open a console and run `java -jar client.jar <hostname> <port>`. Switch `<port>` with the port of the server and `<hostname>` with its IP-address/hostname.
#### Commands
The server tells you most of the things you have to enter. There are also some commands you can use. Just enter one into the console to use it.
* `!change` tells the server, you want to connect to a new chat partner. It will tell you what to do next.
* `!exit` shuts down the client application.
