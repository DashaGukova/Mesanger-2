package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.xml.sax.SAXException;
import saxPars.SAXPars;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;


public class Controller {
    @FXML
    private TextField ip;
    @FXML
    private TextField nickname;
    @FXML
    private Button enter;
    private Client client;
    public Stage stage;
    private String ipAdr;
    private static String nick;
    private static ObservableList<String> langs;
    public MultipleSelectionModel<String> langsSelectionModel;
    private ListView<String> users;
    private static String talkPartner;
    public static TextArea dialogueText;
    private static ArrayList<ArrayList<String>> historyOfDialogue = new ArrayList<>();
    private Label nicknameLabel;
    private static TextArea newMessage;
    private Button send;
    private Button downServer;

    public static void addNewUser(String str) {
        langs.add(str);
        ArrayList<String> neW = new ArrayList<>();
        historyOfDialogue.add(neW);
    }
    public static void deleteNewUser(String str) {
        for(int i=0;i<langs.size();i++) {
            if(langs.get(i).equals(str)) {
                langs.remove(i);
                historyOfDialogue.remove(i);
            }
        }
    }
    public static void newMessage(String str) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = null;
        parser = factory.newSAXParser();
        SAXPars saxp = new SAXPars();
        InputStream is = new ByteArrayInputStream(str.getBytes());
        parser.parse(is, saxp);
        String sender = saxp.getSender();
        String r = saxp.getReceiver();
        System.out.println(saxp.getText());
        if (sender.equals(sender) && r.equals(talkPartner)) {
            dialogueText.appendText(saxp.getTime() + " " + saxp.getSender() + " : " + saxp.getText()+"\n");
        } else if (sender.equals(talkPartner) && r.equals(nick)) {
            dialogueText.appendText(saxp.getTime() + " " + saxp.getSender() + " : " + saxp.getText()+"\n");
        }
        for(int i=0; i<langs.size();i++)
        {
            if(saxp.getReceiver().equals(langs.get(i))||saxp.getSender().equals(langs.get(i))){
                historyOfDialogue.get(i).add(saxp.getTime() + " " + saxp.getSender() + " : " + saxp.getText()+"\n");
                newMessage.clear();
                break;
            }
        }
    }

    public void setStage(Stage nstage)
    {
        this.stage=nstage;
    }

    public void enter(ActionEvent actionEvent) throws IOException {
        this.ipAdr=ip.getText();
        this.nick= nickname.getText();
        client = new Client(ipAdr, nick);
        createElement();
        //setOnlineUsers();
    }
    private void createElement() throws IOException {
        dialogueText = new TextArea();
        langs = FXCollections.observableArrayList(client.setAllUsers());
        users = new ListView<String>(langs);
        langsSelectionModel = users.getSelectionModel();
        for(int i=0;i<langs.size();i++) {
            ArrayList <String> neW = new ArrayList<>();
            historyOfDialogue.add(neW);
        }
        nicknameLabel = new Label("      Вы вошли под пользователем  "+nick+"\n\n");
        newMessage = new TextArea();
        send = new Button("Send");
        send.setOnAction(event-> {
            send();
            newMessage.clear();
        });
        downServer = new Button ("exit");
        downServer.setOnAction(event-> {
            client.close();
            stage.close();
        });;
        setOnlineUsers();
        MainWindow();
    }

    private void send()
    {
        String str = newMessage.getText();
        client.writeMsg.sendMssg(str, talkPartner);
    }
    private void MainWindow()
    {
        SplitPane splitPane = new SplitPane();
        splitPane.setPadding(new Insets(20));
        javafx.scene.control.ScrollPane scrollPaneDialog = new javafx.scene.control.ScrollPane();
        scrollPaneDialog.setContent(dialogueText);
        scrollPaneDialog.setPrefSize(420,500);
        scrollPaneDialog.setVbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPaneDialog.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.AS_NEEDED);
        dialogueText.setPrefSize(420,500);
        dialogueText.setWrapText(true);
        newMessage.setWrapText(true);
        newMessage.setPrefSize(360, 57);
        send.setPrefSize(60, 57);
        users.setPrefSize(300, 500);
        dialogueText.setWrapText(true);
        dialogueText.setEditable(false);
        newMessage.setWrapText(true);
        downServer.setPrefSize(300, 20);
        //Label empty = new Label("\n\n");
        FlowPane left = new FlowPane(nicknameLabel, users, downServer);
        FlowPane right = new FlowPane(scrollPaneDialog, newMessage, send);
        splitPane.getItems().addAll(right, left);
        Scene scene = new Scene(splitPane,800,600);
        stage.setScene(scene);
        stage.setTitle("MSSG");
        stage.setResizable(false);
        stage.show();
        client.setThreads();
    }
    private void setOnlineUsers() throws IOException {
        langsSelectionModel.selectedItemProperty().addListener(new ChangeListener<String>(){
            public void changed(ObservableValue<? extends String> changed, String oldValue, String newValue){
                System.out.println(newValue);
                talkPartner=newValue;
                dialogueText.clear();
                printDialog();
            }
        });
    }
    private void printDialog()
    {
        for(int i=0;i<langs.size();i++) {
            if(langs.get(i).equals(talkPartner)){
                for(int j=0;j<historyOfDialogue.get(i).size();j++) {
                    dialogueText.appendText(historyOfDialogue.get(i).get(j));
                }
            }
        }
    }
}
