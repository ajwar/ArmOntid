package com.yandex.ajwar.view;

import com.yandex.ajwar.MainApp;
import com.yandex.ajwar.util.ImbaseUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.xpath.*;
import java.io.StringReader;
import java.net.URL;
import java.util.*;

/**
 * Created by 53189 on 10.02.2017.
 */
public class ImbaseTreeController implements Initializable {
    private Stage imbaseDialogStage;
    private MainApp mainApp;
    private static final Logger log=Logger.getLogger(ImbaseTreeController.class);
    private volatile static double xOffset = 0;
    private volatile static double yOffset = 0;
    @FXML
    private TreeView<String> treeViewImbase;
    @FXML
    private AnchorPane anchorPaneImbase;
    @FXML
    private TextField textFieldImbaseCeh;
    private volatile static TreeItem<String> treeItemImbase;

    @Override
    public void initialize (URL location, ResourceBundle resources) {
        if (treeItemImbase==null) containTreeViewImbase();
        initImbaseTreeView();
        treeViewImbase.setRoot(treeItemImbase);
        treeItemImbase.setExpanded(true);
    }

    /**Заполнение TreeView из базы Imbase из таблицы tc_ceh*/
    public void containTreeViewImbase(){
        try {
            treeItemImbase = new TreeItem<>("Цеха и участки");
            ImbaseUtil im = ImbaseUtil.returnAndCreateThreadImbase();
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            String xml = im.openQuery2(im, "select f_name,f_owner,f_level from tc_ceh order by f_name");
            //XPathExpression expr=xpath.compile("//data[1]");
            XPathExpression expr = null;
            NodeList nodes = null;
            try {
                expr = xpath.compile("//data[f_owner=0]");
                nodes = (NodeList) expr.evaluate(new InputSource(new StringReader(xml)), XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
                log.error("Ошибка при заполнении данных из Имбэйс",e);
            }
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
                try {
                    expr = xpath.compile("//data[f_owner=" + level + "]");
                    nodes = (NodeList) expr.evaluate(new InputSource(new StringReader(xml)), XPathConstants.NODESET);
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                    log.error("Ошибка при заполнении данных из Имбэйс",e);
                }
                for (int j = 0; j <nodes.getLength() ; j++) {
                    Node n=nodes.item(j);
                    treeItemImbase.getChildren().get(i).getChildren().add(new TreeItem<>(n.getChildNodes().item(0).getTextContent()));
                }

            }
        } finally {
            ImbaseUtil.closeThreadImbase();
        }
    }
    /**Нажатие на кнопку отмена*/
    @FXML
    private void clickCancelImbaseTreeView(){
        getImbaseDialogStage().close();
    }
    /**Нажатие на кнопку подтвердить*/
    @FXML
    private void clickAgreeImbaseTreeView(){
        getMainApp().getPdfViewerController().getTextFieldRouteCeh().setText(textFieldImbaseCeh.getText());
        getImbaseDialogStage().close();

    }
    /**Очиста поля по нажатию кнопки*/
    @FXML
    private void clickButtonImbaseClear(){
        textFieldImbaseCeh.clear();
    }

    public String returnSubString(TreeItem<String> treeItem){
        String str,strParent,strChildren;
        if (treeItem.getParent()==treeItemImbase){
            str=treeItem.getValue().trim().substring(0,treeItem.getValue().indexOf(" "));
        }else {
            strParent=treeItem.getParent().getValue().trim();
            strChildren=treeItem.getValue().trim();
            str=strParent.substring(0,strParent.indexOf(" "))+"/"+strChildren.substring(0,strChildren.indexOf(" "));
        }
        return str;
    }
    private void initImbaseTreeView(){
        textFieldImbaseCeh.clear();
        anchorPaneImbase.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        anchorPaneImbase.setOnMouseDragged(event -> {
            getImbaseDialogStage().setX(event.getScreenX() - xOffset);
            getImbaseDialogStage().setY(event.getScreenY() - yOffset);
        });
        treeViewImbase.setOnMouseClicked(event -> {
            TreeItem<String> tempItem = treeViewImbase.getSelectionModel().getSelectedItem();
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1 && event.isControlDown() && tempItem != treeItemImbase && tempItem != null) {
                if (textFieldImbaseCeh.getText().isEmpty()) {
                    textFieldImbaseCeh.setText(returnSubString(tempItem));
                } else {
                    textFieldImbaseCeh.setText(textFieldImbaseCeh.getText() + "-" + returnSubString(tempItem));
                }
            }
        });
    }


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

    public TreeItem<String> getTreeItemImbase() {
        return treeItemImbase;
    }

    public void setTreeItemImbase(TreeItem<String> treeItemImbase) {
        this.treeItemImbase = treeItemImbase;
    }
}
