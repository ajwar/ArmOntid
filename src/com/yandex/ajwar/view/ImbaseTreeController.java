package com.yandex.ajwar.view;

import com.yandex.ajwar.MainApp;
import com.yandex.ajwar.util.ImbaseUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.*;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by 53189 on 10.02.2017.
 */
public class ImbaseTreeController implements Initializable {
    private Stage imbaseDialogStage;
    private MainApp mainApp;
    private static final Logger log=Logger.getLogger(ImbaseTreeController.class);
    private volatile static double xOffset = 0;
    private volatile static double yOffset = 0;
    private volatile static boolean mouseClick=false;
    private volatile static boolean presKey=false;
    @FXML
    private TreeView<String> treeViewImbase;
    @FXML
    private AnchorPane anchorPaneImbase;
    private TreeItem<String> treeItemImbase;
    @Override
    public void initialize (URL location, ResourceBundle resources) {
        initImbaseTreeView();
        try {
            containTreeViewImbase();
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        treeViewImbase.setRoot(treeItemImbase);
        treeItemImbase.setExpanded(true);
    }

    /**Заполнение TreeView из базы Imbase из таблицы tc_ceh*/
    private void containTreeViewImbase() throws XPathExpressionException {
        try {
            treeItemImbase = new TreeItem<>("Цеха и участки");
            ImbaseUtil im = ImbaseUtil.returnAndCreateThreadImbase();
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            String xml = im.openQuery2(im, "select f_name,f_owner,f_level from tc_ceh order by f_name");
            XPathExpression expr = xpath.compile("//data[f_owner=0]");
            //XPathExpression expr=xpath.compile("//data[1]");
            NodeList nodes = (NodeList) expr.evaluate(new InputSource(new StringReader(xml)), XPathConstants.NODESET);
            //String[] array=new String[nodes.getLength()];
            Map<String, Integer> map = new LinkedHashMap<>();
            Integer level;
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                String name = n.getChildNodes().item(0).getTextContent();
                level = Integer.parseInt(n.getChildNodes().item(2).getTextContent());
                map.put(name, level);
                treeItemImbase.getChildren().add(new TreeItem<>(name));
            }
            for (int i = 0; i < map.size(); i++) {
                level = map.get(treeItemImbase.getChildren().get(i).getValue());
                System.out.println(level);
                expr = xpath.compile("//data[f_owner=" + level + "]");
                nodes = (NodeList) expr.evaluate(new InputSource(new StringReader(xml)), XPathConstants.NODESET);
                for (int j = 0; j <nodes.getLength() ; j++) {
                    Node n=nodes.item(j);
                    treeItemImbase.getChildren().get(i).getChildren().add(new TreeItem<>(n.getChildNodes().item(0).getTextContent()));
                }

            }
        } finally {
            ImbaseUtil.closeThreadImbase();
        }
    }
    @FXML
    private void clickCancelImbaseTreeView(){
        getImbaseDialogStage().close();
    }
    private void clickAgreeImbaseTreeView(){

    }
    private void initImbaseTreeView(){
        anchorPaneImbase.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        anchorPaneImbase.setOnMouseDragged(event -> {
            getImbaseDialogStage().setX(event.getScreenX() - xOffset);
            getImbaseDialogStage().setY(event.getScreenY() - yOffset);
        });
        treeViewImbase.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount()==1 && event.isControlDown()){
                ЭТО ЗАПИСАТЬ В CSS,a потом адд и ремуве
                /**.myTree .tree-cell {
                    -fx-background-color: #0a0a0a ;
                    -fx-text-fill: #ffffff ;
                }*/
                treeViewImbase.getSelectionModel().getSelectedItem().
                System.out.println("Нажат контроль и левая кнопка мыши");
            }
        });
    }
            /*for (int j = 0; j <nod.getLength() ; j++) {
                Node nCh = nod.item(i);
                treeItemImbase.getChildren().get(i).getChildren().add(new TreeItem<>(nCh.getChildNodes().item(0).getTextContent()));
            }*/



            //NodeList nod=xpath.evaluate("//data[f_owner="+level+"]",XPathConstants.NODESET);
            /*for (int j = 0; j <nodeList.getLength() ; j++) {
                Node node=nodeList.item(j);

            }*/
        //}


    public Stage getImbaseDialogStage() {
        return imbaseDialogStage;
    }

    public void setImbaseDialogStage(Stage imbaseDialogStage) {
        this.imbaseDialogStage = imbaseDialogStage;
    }

    public MainApp getMainApp() {
        return mainApp;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}
