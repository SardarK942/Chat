import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
    private static ServerSocket serverSocket;

    public ChatServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public static void startServer(){
        try{

            while (!serverSocket.isClosed()){

                Socket socket = serverSocket.accept();
                System.out.println("A new client connected!");
                ClientHandler clientHandler = new ClientHandler(socket);

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        }
        catch (EOFException ex){
            closeServerSocket();
        }
        catch (IOException ex) {
            closeServerSocket();
        }
    }

    public static void closeServerSocket(){
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        }
        catch (EOFException ex){
            ex.printStackTrace();
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port;
        if(args.length < 1){
//            System.out.println("Need PortNumber Dawg");
            return;
        }
        else
            try {
                port = Integer.parseInt(args[0]);
                ServerSocket serverSocket = new ServerSocket(port);
                ChatServer server = new ChatServer(serverSocket);
                ChatServer.startServer();
            }
            catch (EOFException ex){
                ex.printStackTrace();
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
    }

    public static class ClientHandler implements Runnable{

        public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
        private Socket socket;
        private BufferedReader bufferedReader;
        private BufferedWriter bufferedWriter;
        private String clientUserName;

        public ClientHandler(Socket socket) {
            try{
                this.socket = socket;
                this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.clientUserName = bufferedReader.readLine();
                clientHandlers.add(this);
                broadCastMessage("Server: " + clientUserName + " has entered the chat!");

            }
            catch (EOFException ex){
                closeAll(socket, bufferedReader, bufferedWriter);
            }
            catch (IOException ex){
                closeAll(socket, bufferedReader, bufferedWriter);
            }
        }

        @Override
        public void run() {
            String messageFromClient;

            while(socket.isConnected()) {
                try {
                    messageFromClient = bufferedReader.readLine();
                    if(messageFromClient == null) throw new EOFException();
                    broadCastMessage(messageFromClient);
                }
                catch (EOFException ex){
                    closeAll(socket, bufferedReader, bufferedWriter);
                    break;
                }
                catch (IOException ex){
                    closeAll(socket, bufferedReader, bufferedWriter);
                    break;
                }
            }
        }

        public void broadCastMessage(String messageToSend){
            for(ClientHandler clientHandler: clientHandlers){
              try {
                  if(!clientHandler.clientUserName.equals(clientUserName)){
                      clientHandler.bufferedWriter.write(messageToSend);
                      clientHandler.bufferedWriter.newLine();
                      clientHandler.bufferedWriter.flush();
                  }
              }
              catch (EOFException ex){
                  closeAll(socket, bufferedReader, bufferedWriter);
              }
              catch (IOException ex){
                  closeAll(socket, bufferedReader, bufferedWriter);
              }
            }
        }


        public void closeAll(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
            try{
                if (bufferedWriter != null){
                    bufferedReader.close();
                }
                if(bufferedWriter != null){
                    bufferedWriter.close();
                }
                if(socket != null){
                    socket.close();
                }
            }
            catch (EOFException ex){
                ex.printStackTrace();
            }
            catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }
}