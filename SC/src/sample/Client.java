package sample;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class Client {
    private static Socket socket;
    public static String ip = "localhost";
    public static int port = 1000;
    private static BufferedReader in; // поток чтения из сокета
    private static PrintWriter out; // поток чтения в сокет
    private String nickname;
    public WriteMsg writeMsg;
    public ReadMsg readMsg;

    public Client(String ipAdr, String nick)
    {
        this.ip=ipAdr;
        this.nickname= nick;
        try {
            this.socket = new Socket(ip, port);
        } catch (IOException e) {
            System.err.println("Socket failed");
        }
        try {
            // потоки чтения из сокета / записи в сокет, и чтения с консоли
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            connectToServer();
        } catch (IOException e) {
            Client.this.downService();
        }
        // В противном случае сокет будет закрыт
        // в методе run() нити.
    }

    private void connectToServer()
    {
        out.println(nickname);
        System.out.println(nickname);
    }
    public ArrayList<String> setAllUsers() throws IOException {
        out.println(nickname);
        String str = in.readLine();
        if(str.equals(""))
        {
            ArrayList<String> empty=new ArrayList<String>();
            return empty;
        }
        //int sizeList = Integer.parseInt(str);
        ArrayList<String> onlineUsers=new ArrayList<String>(Arrays.asList(str.split("\\s")));
        //..onlineUsers  = str.split("\\s"); // Разбиение строки на слова с помощью разграничителя (пробел)
           // onlineUsers.add(in.readLine());
        return onlineUsers;
    }

    private static void downService()
    {
        try {
            if (!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
            }
        } catch (IOException ignored) {}
    }
    public void close()
    {
        writeMsg.sendMssg("close0007","server");

    }
    public class ReadMsg extends Thread
    {
        @Override
        public void run() {
            String str;
            try
            {
                while (true) {
                    str = in.readLine();
                    System.out.println(str);
                    if (str.equals("newUsers")) {
                        str = in.readLine();
                        Controller.addNewUser(str);
                        continue;
                    }
                    else if(str.equals("delete"))
                    {
                        //out.println("ok");
                        str = in.readLine();
                        Controller.deleteNewUser(str);
                        continue;
                    }
                    Controller.newMessage(str);
                    System.out.println(str);

                }
            } catch (IOException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            }

        }
    }
    public void setThreads()
    {
        System.out.println("setThreads");
        writeMsg = new WriteMsg();
        readMsg = new ReadMsg();
        writeMsg.start();
        readMsg.start();
    }

    public class WriteMsg extends Thread
    {
        public String userWord ;
        public String receiver ;
        public void sendMssg(String word, String rec)
        {
              userWord = word;
              receiver = rec;
              run();
        }
        @Override
        public void run()
        {
            if(userWord.equals("close0007"))
            {
                out.println("close0007");
                Client.downService();
            }
            if(!receiver.isEmpty()&& !userWord.isEmpty())
            out.println("msg0003");
            Date time = new Date(); // текущая дата
            SimpleDateFormat dt1 = new SimpleDateFormat("HH:mm:ss"); // берем только время до секунд
            String dtime = dt1.format(time); // время
            String str="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> <message type=\"send_message\" time=\"" + dtime +"\" sender=\""+ nickname +"\" receiver=\""+ receiver +"\" content=\""+userWord+"\"></message>";
            //System.out.print(str+"\n");
            if(!receiver.isEmpty()&& !userWord.isEmpty())
            out.println(str);

        }
    }
}
