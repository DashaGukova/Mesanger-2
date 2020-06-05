package server;

import org.xml.sax.SAXException;
import saxPars.SAXPars;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * проект реализует консольный многопользовательский чат.
 * вход в программу запуска сервера - в классе Server.
 * @author izotopraspadov, the tech
 * @version 2.0
 */

class ServerSomthing extends Thread {

    private Socket socket; // сокет, через который сервер общается с клиентом,
    // кроме него - клиент и сервер никак не связаны
    private BufferedReader in; // поток чтения из сокета
    private PrintWriter out; // поток завписи в сокет
    private  ArrayList<ArrayList<String>> historyOfDialogues = new ArrayList<>();
   // private ArrayList<String> onlineusers= new ArrayList<>();
    /**
     * для общения с клиентом необходим сокет (адресные данные)
     * @param socket
     * @throws IOException
     */

    public ServerSomthing(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        start(); // вызываем run()
    }
    @Override
    public void run() {
        String word;
        try {
            word = in.readLine();
            this.setName(word);
            System.out.println("new nick:"+word);
            getAllUsers();
            for (ServerSomthing vr : Server.serverList) {
                if(vr.getName().equals(this.getName()))
                    continue;
                else {
                    vr.out.println("newUsers");
                    vr.out.println(word);
                }
            }
            try {
                while (true) {
                    word = in.readLine();
                    if (word.equals("Kod:STOP")) {
                        this.downService();
                        break;
                    } else if (word.equals("msg0003")) {
                        word=in.readLine();
                        SAXParserFactory factory = SAXParserFactory.newInstance();
                        SAXParser parser = factory.newSAXParser();
                        SAXPars saxp = new SAXPars();
                        InputStream is = new ByteArrayInputStream(word.getBytes());
                        parser.parse(is, saxp);
                        System.out.println("Echoing: " + word);
                        for (ServerSomthing vr : Server.serverList) {
                            if(saxp.getReceiver().equals(vr.getName()) || this.getName().equals(vr.getName())) {
                                vr.out.println(word);
                            }
                        }
//                        for (int i=0;i<this.onlineusers.size(); i++) {
//                            if(this.onlineusers.get(i).equals(saxp.getReceiver())) {
//                                this.historyOfDialogues.get(i).add(word);
//                            }
//                        }
                    }
                    else if(word.equals("close0007"))
                    {
                        this.downService();
                    }
                }
            } catch (NullPointerException ignored) {} catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }


        } catch (IOException e) {
            this.downService();
        }
    }
    private void getAllUsers() throws IOException {
        String name = in.readLine();
        System.out.println(Server.serverList.size());
        //out.println(Server.serverList.size()-1);
        String listUsers= new String();
        for (int i=0; i<Server.serverList.size();i++){
            if(Server.serverList.get(i).getName().equals(this.getName())) {
                continue;
            }
                String str=Server.serverList.get(i).getName();
                listUsers+=Server.serverList.get(i).getName();
                listUsers+=" ";
                System.out.println("Online:"+str);

        }
            out.println(listUsers);
    }
    /**
     * отсылка одного сообщения клиенту по указанному потоку
     * @param msg
     */
    private void send(String msg) {
        out.write(msg + "\n");
    }
    /**
     * закрытие сервера
     * прерывание себя как нити и удаление из списка нитей
     */
    private void downService() {
        try {
            if(!socket.isClosed()) {
                for (ServerSomthing vr : Server.serverList) {
                    for (ServerSomthing vr2 : Server.serverList) {
                        if(vr2.getName().equals(this.getName()))
                            continue;
                            vr2.out.println("delete");
                            System.out.println("send del");
                            vr2.out.println(this.getName());
                            System.out.println("delete " + this.getName());
                            //Server.story.addStoryEl(this.getName(), this.historyOfDialogues);
                    }
                    if(vr.equals(this)) {
                            socket.close();
                            in.close();
                            out.close();
                            vr.interrupt();
                            Server.serverList.remove(this);
                    }
                }
            }
        } catch (IOException ignored) {}
    }
}

/**
 * класс хранящий в ссылочном приватном
 * списке информацию о последних 10 (или меньше) сообщениях
 */

//class Story {
//    public ArrayList<String>allOfflineUsers= new ArrayList<> ();
//    public ArrayList<ArrayList<ArrayList<String>>> story = new ArrayList<>();
//
//    public void addStoryEl(String name, ArrayList<ArrayList<String>> list) {
//       allOfflineUsers.add(name);
//       story.add(list);
//    }
//    public void deleteStoryEl(String name) {
//        for(int i=0;i<allOfflineUsers.size();i++)
//        {
//            if(allOfflineUsers.get(i).equals(name))
//            {
//                allOfflineUsers.remove(i);
//                story.remove(i);
//            }
//        }
//    }
//}

public class Server {

    public static final int PORT = 1000;
    public static LinkedList<ServerSomthing> serverList = new LinkedList<>(); // список всех нитей - экземпляров сервера, слушающих каждый своего клиента
    //public static Story story;

    /**
     * @param args
     * @throws IOException
     */

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        //story = new Story();
        System.out.println("Server Started");
        try {
            while (true) {
                // Блокируется до возникновения нового соединения:
                Socket socket = server.accept();
                try {
                    serverList.add(new ServerSomthing(socket)); // добавить новое соединенние в список

                } catch (IOException e) {
                    // Если завершится неудачей, закрывается сокет,
                    // в противном случае, нить закроет его:
                    socket.close();
                }
            }
        } finally {
            server.close();
        }
    }
}