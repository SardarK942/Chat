import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String name;

    public ChatClient(Socket socket, String name) {
        try {
            this.socket =socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.name = name;
        }
        catch (EOFException ex){
            closeAll(socket, bufferedReader, bufferedWriter);
        }
        catch (IOException ex){
            closeAll(socket, bufferedReader, bufferedWriter);
        }
    }
    public void sendMessage(){
        try {
            bufferedWriter.write(name);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()) {
                if(scanner.nextLine() == null) { scanner.close(); throw new EOFException();}
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(name + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }
        catch (EOFException ex) {
            closeAll(socket, bufferedReader, bufferedWriter);
            System.out.println("Inside the EOF!!!");
        }
        catch (IOException ex) {
            closeAll(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listenForMeesage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while(socket.isConnected()){
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        if(msgFromGroupChat == null) throw new EOFException();
                        System.out.println(msgFromGroupChat);
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
        }).start();
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
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("What is your name?");
        String name = scanner.nextLine();
        System.out.println("Sending name to server. . . ");
        int port;


        if(args.length < 1){
        System.out.println("Need PortNumber Dawg");
        return;
    }
        else
            try {
                port = Integer.valueOf(args[0]);
                Socket socket = new Socket("localhost", port);
                ChatClient chatClient = new ChatClient(socket, name);
                chatClient.listenForMeesage();
                chatClient.sendMessage();



            } catch (IOException ex){
                ex.printStackTrace();
            }
    }

}