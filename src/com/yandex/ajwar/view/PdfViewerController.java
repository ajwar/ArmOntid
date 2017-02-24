package com.yandex.ajwar.view;

import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.yandex.ajwar.MainApp;
import com.yandex.ajwar.model.*;
import com.yandex.ajwar.util.AlertUtilNew;
import com.yandex.ajwar.util.DateUtil;
import com.yandex.ajwar.util.S4AppUtil;
import com.yandex.ajwar.util.TextFormatterUtil;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

public class PdfViewerController implements Initializable {

    private MainApp mainApp;
    private S4AppUtil S4App = S4AppUtil.getInstance();
    private static final Logger log=Logger.getLogger(PdfViewerController.class);
    private Preferences preferencesScanKdAndTd = MainApp.getPreferencesScanKdAndTd();
    private Preferences preferencesTableViewDocTypes = MainApp.getPreferencesTableViewDocTypes();
    private Preferences preferencesTableViewInputMask = MainApp.getPreferencesTableViewInputMask();
    private Preferences preferencesTableViewFormats = MainApp.getPreferencesTableViewFormats();
    private FileChooser fileChooser;
    private TextFormatterUtil textFormatterUtil;
    private ObjectProperty<PDFFile> currentFile;
    private ObjectProperty<ImageView> currentImage;
    private DoubleProperty zoom;
    private PageDimensions currentPageDimensions;
    private static ExecutorService executorServiceLoad;
    private Path deleteMusorPath = null;
    private int pagesOfSheets = 0;
    private static final double ZOOM_DELTA = 1.1d;
    private volatile static double DEGREE = 0.0d;
    private static final String DEFAULT_TEXT_COMBO_BOX_MASK = "Произвольно";
    private static final String STR_ZAKAZ_DOP = "**-**** Дополнение №*";
    private static final String STR_FORMAT_TD ="В25.*****.*****";
    private static final String TOOLTIP_STR_FORMAT_TD ="Допускается ввод только по маске \"В25.*****.*****\"";
    private static final String SP=MainApp.getSP();
    private static final String USER_HOME=MainApp.getUserHome();
    private static final String SELECT_SQL_IN_OBJECT="((#Тип объекта# = 3) OR (#Тип объекта# = 4)) AND (#Обозначение# <> '')";
    private static List<Long> listDocIdScan=new ArrayList<>();
    //private static final String HOME_PATH_SCAN="";
    //private final String FULL_PATH_FILE ="\\AppData\\Local\\Temp\\_IMS\\";
    private static final byte SECTION_ID = 1; //Раздел документация(столбец SECTION_ID) в таблице SSECTION
    //private static final byte SECTION_ID_DETAIL=4;
    private final String[] arrayBuroOgt={
            "БШ",
            "БМО",
            "БСиТО",
            "БСУ",
            "БПП",
            "ГрТиС"};

    @FXML
    private Pagination pagination;
    @FXML
    private Label labelCurrentZoom;
    @FXML
    private Label labelDopNumberPdfView;
    @FXML
    private Label countFileLabel;
    @FXML
    private Label labelNumberOfSheets;
    @FXML
    private ScrollPane scroller;
    @FXML
    private Tooltip tooltipDesignationPdfView;
    @FXML
    private TableView<MapSketch> tableViewMapSketchTd;
    @FXML
    private TableColumn<MapSketch,TextField> tableColTextFieldOtdRegNumMapSketch;
    @FXML
    private TableColumn<MapSketch,TextField> tableColTextFieldDesignIIMapSketch;
    @FXML
    private TableColumn<MapSketch,TextField> tableColTextFieldNumChangeMapSketch;
    @FXML
    private TableColumn<MapSketch,TextField> tableColTextFieldMapSketch;
    @FXML
    private TableColumn<MapSketch,Button> tableColButtonMapSketch;
    @FXML
    private TableColumn<ObjectTp,Number> tableColNumberMapSketch;
    @FXML
    private TableView<ObjectTp> tableViewObjectTp;
    @FXML
    private TableColumn<ObjectTp,String> tableColDesignObjectTp;
    @FXML
    private TableColumn<ObjectTp,String> tableColNameObjectTp;
    @FXML
    private TableColumn<ObjectTp,Number> tableColNumberObjectTp;
    @FXML
    private TableColumn<ObjectTp,Integer> tableColArtIdObjectTp;
    @FXML
    private TableView<NumberSheet> tableViewNumberSheet;
    @FXML
    private TableColumn<NumberSheet,String> tableColDesignNumSheet;
    @FXML
    private TableColumn<ObjectTp,Number> tableColNumberSheet;
    @FXML
    private TableView<StringData> listFileTable;
    @FXML
    private TableColumn<StringData, String> filePathColumn;
    @FXML
    private RadioButton radioButtonNotActual;
    @FXML
    private RadioButton radioButtonActualNow;
    @FXML
    private RadioButton radioButtonTermChange;
    @FXML
    private DatePicker datePickerOtdNew;
    @FXML
    private DatePicker datePickerOtdVersion;
    @FXML
    private DatePicker datePickerTermChange;
    @FXML
    private ComboBox<String> comboBoxChangeReasonPdfViewController;
    @FXML
    private ComboBox<String> comboBoxDocTypesPdfViewController;
    @FXML
    private ComboBox<String> comboBoxMaskPdfViewController;
    @FXML
    private ComboBox<String> comboBoxFormatPdfViewController;
    @FXML
    private ComboBox<String> comboBoxBuroOgtTd;
    @FXML
    private TextField textFieldDesignation;
    @FXML
    private TextField textFieldNumberChange;
    @FXML
    private TextField textFieldDopNumberPdfView;
    @FXML
    private TextField textFieldDesignPathMapTd;
    @FXML
    private TextField textFieldOtdRegNumPathMapTd;
    @FXML
    private TextField textFieldNumChangePathMapTd;
    @FXML
    private TextField textFieldDesignationIIPathMapTd;
    @FXML
    private TextField textFieldDesignSetMapTd;
    @FXML
    private TextField textFieldOtdRegNumSetMapTd;
    @FXML
    private TextField textFieldNumChangeSetMapTd;
    @FXML
    private TextField textFieldDesignationIISetMapTd;
    @FXML
    private TextField textFieldDesignSheetMaterialTd;
    @FXML
    private TextField textFieldOtdRegNumSheetMaterialTd;
    @FXML
    private TextField textFieldNumChangeSheetMaterialTd;
    @FXML
    private TextField textFieldDesignationIISheetMaterialTd;
    @FXML
    private TextField textFieldDesignOperationMapTd;
    @FXML
    private TextField textFieldOtdRegNumOperationMapTd;
    @FXML
    private TextField textFieldNumChangeOperationMapTd;
    @FXML
    private TextField textFieldDesignationIIOperationMapTd;
    @FXML
    private TextField textFieldRouteCeh;
    @FXML
    private TextArea textAreaName;
    @FXML
    private TextArea textAreaNote;
    @FXML
    private CheckBox checkBoxArbitrarily;
    @FXML
    private CheckBox checkBoxRegOtd;
    @FXML
    private CheckBox checkBoxRegOtdVersion;
    @FXML
    private CheckBox checkBoxNoteVersion;
    @FXML
    private CheckBox checkBoxBuroOgtTd;
    @FXML
    private Button buttonExportInSearch;
    @FXML
    private Button buttonCheckDesignation;
    @FXML
    private Button buttonListVersion;
    @FXML
    private Button buttonAddVersionInSearch;
    @FXML
    private Button buttonDeductNamePdfView;
    @FXML
    private Button buttonRouteMapScanTd;
    @FXML
    private Button buttonSetMapScanTd;
    @FXML
    private Button buttonSheetMaterialScanTd;
    @FXML
    private Button buttonOperationMapScanTd;
    @FXML
    private Button buttonExportInSearchTp;
    @FXML
    private ContextMenu contextMenuPaginationPdfView;
    @FXML
    private ImageView imageViewPdfView;
    @FXML
    private TabPane tabPaneScanKdPdfView;
    @FXML
    private TabPane tabPaneScanKdNewAndVersionPdfView;
    @FXML
    private Tab tabScanKdPdfView;
    @FXML
    private Tab tabScanTdPdfView;
    @FXML
    private Tab tabScanKdNewPdfView;
    @FXML
    private Tab tabScanKdVersionPdfView;

    /**Дисэйбл главной формы*/
    private void disableMainWindow(boolean flag) {
        Platform.runLater(()->getMainApp().getPrimaryStage().getScene().getRoot().setDisable(flag));
    }
    /**
     * Блокировка кнопок по событиям
     */
    private void disableButton(boolean flag) {
        buttonExportInSearch.setDisable(flag);
        buttonAddVersionInSearch.setDisable(flag);
    }
    /**
     * Возвращает обозначение документа
     */
    private String returnDesignation(S4AppUtil S4AppThread,TextField field,String docName,Long idDocType) {
        if (idDocType==null) {//Для сканированной КД
            String str;
            S4AppThread.openQuery(S4AppThread,"select suffix,dt_code from doctypes where doc_name=\""+docName+"\"");
            int suffix=Integer.parseInt(S4AppThread.queryFieldByName(S4AppThread,"suffix"));
            String code=S4AppThread.queryFieldByName(S4AppThread,"dt_code");
            S4AppThread.closeQuery(S4AppThread);
            if (!textFieldDopNumberPdfView.getText().isEmpty()) str=" Дополнение №"+textFieldDopNumberPdfView.getText();
                else str="";
            if (suffix==0)
                    return field.getText()+str;
                else
                    return field.getText()+str +" "+ code;
        } else {//Для сканированной ТД
            S4AppThread.openQuery(S4AppThread,"select suffix,dt_code from doctypes where doc_type=\""+idDocType+"\"");
            int suffix=Integer.parseInt(S4AppThread.queryFieldByName(S4AppThread,"suffix"));
            String code=S4AppThread.queryFieldByName(S4AppThread,"dt_code");
            S4AppThread.closeQuery(S4AppThread);
            if (suffix==0) return field.getText();
            else return field.getText()+" "+code;
        }
    }

    /**
     * Присваиваю начальные значения всем DataPicker
     */
    private void initValueDataPicker() {
        datePickerOtdNew.setValue(DateUtil.NOW_LOCAL_DATE());
        datePickerTermChange.setValue(DateUtil.NOW_LOCAL_DATE());
        datePickerOtdVersion.setValue(DateUtil.NOW_LOCAL_DATE());
    }

    /**
     * Формирование полного пути
     */
    private String copyAndReturnFullPath(String designation, int version,String move) {
        String path;
        if (version == 0) {
            path = move + SP + designation + " Скан.pdf";
        } else {
            path = move + SP + designation + " [" + version + "] Скан.pdf";
        }
        return path;
    }

    /**Невидимость форм окна при изменении в опциях ЧекБоксов*/
    public void hideFormPdfView(){
        if (!preferencesScanKdAndTd.getBoolean("checkBoxScanKdOptions",true)){
            tabPaneScanKdPdfView.getTabs().remove(tabScanKdPdfView);
        }else if(!tabPaneScanKdPdfView.getTabs().contains(tabScanKdPdfView)){
            tabPaneScanKdPdfView.getTabs().add(tabScanKdPdfView);
        }
        if (!preferencesScanKdAndTd.getBoolean("checkBoxScanTdOptions",true)){
            tabPaneScanKdPdfView.getTabs().remove(tabScanTdPdfView);
        }else if(!tabPaneScanKdPdfView.getTabs().contains(tabScanTdPdfView)){
            tabPaneScanKdPdfView.getTabs().add(tabScanTdPdfView);
        }
        if (!preferencesScanKdAndTd.getBoolean("checkBoxScanKdNewOptions",true)){
            tabPaneScanKdNewAndVersionPdfView.getTabs().remove(tabScanKdNewPdfView);
        }else if(!tabPaneScanKdNewAndVersionPdfView.getTabs().contains(tabScanKdNewPdfView)){
            tabPaneScanKdNewAndVersionPdfView.getTabs().addAll(tabScanKdNewPdfView);
        }
        if (!preferencesScanKdAndTd.getBoolean("checkBoxScanKdVersionOptions",true)){
            tabPaneScanKdNewAndVersionPdfView.getTabs().remove(tabScanKdVersionPdfView);
        }else if (!tabPaneScanKdNewAndVersionPdfView.getTabs().contains(tabScanKdVersionPdfView)){
            tabPaneScanKdNewAndVersionPdfView.getTabs().add(tabScanKdVersionPdfView);
        }

    }
    private void progressIndicatorAlways(){
        getMainApp().showProgressIndicator();
        getMainApp().getProgressIndicatorController().getProgressIndicator().setProgress(-1d);
        getMainApp().getProgressIndicatorController().getProgressIndicatorStage().show();
    }
    /**
     * Нажатие на кнопку "Экспорт в Search"
     */
    @FXML
    private void exportInSearch() {
        String msg = "";
        String select=comboBoxMaskPdfViewController.getSelectionModel().getSelectedItem();
        int  i = 0;
        if (!checkDesignationInSearchAndAddVersion(select)) msg += ++i + ")Заполните поле \"Обозначение\".\r\n";
        if (textAreaName.getText().isEmpty()) msg += ++i + ")Заполните поле \"Наименование\".\r\n";
        if (comboBoxFormatPdfViewController.getSelectionModel().getSelectedItem().isEmpty())
            msg += ++i + ")Поставте формат в выпадающем списке \"Формат\".\r\n";
        if (checkBoxRegOtd.isSelected() && !DateUtil.checkRightDate(datePickerOtdNew))
            msg += ++i + ")Поставте правильную дату в календаре \"Регистрации в ОТД\".\r\n";
        if (!msg.isEmpty())
            AlertUtilNew.message("Проверка полей.", msg, "Не заполнены следующие поля:", Alert.AlertType.WARNING);
        else {
            disableMainWindow(true);
            progressIndicatorAlways();
            executorServiceLoad.submit(this::exportInSearchAdapter);
        }
    }
    private void exportInSearchAdapter(){
        int id=-1,versionId = 0;
        try {
            S4AppUtil S4AppThread = S4AppUtil.returnAndCreateThreadS4App();
            //Проверяю код документа и формирую полное обозначение
            String designation = returnDesignation(S4AppThread,textFieldDesignation,comboBoxDocTypesPdfViewController.getSelectionModel().getSelectedItem(),null);
            //Полный путь к файлу
            String path = copyAndReturnFullPath(designation, versionId,preferencesScanKdAndTd.get("textFieldFolderMove", USER_HOME));
            //нахожу Айди типа документа из базы Серча
            S4AppThread.openQuery(S4AppThread,"select doc_type from doctypes where doc_name=\"" + comboBoxDocTypesPdfViewController.getSelectionModel().getSelectedItem() + "\"");
            long docType = Long.parseLong(S4AppThread.queryFieldByName(S4AppThread,"doc_type"));
            S4AppThread.closeQuery(S4AppThread);
            //присваиваю номер архива в переменную
            long archive = preferencesScanKdAndTd.getLong("textFieldArchiveIdKd", 323);
            String fullFileName=preferencesScanKdAndTd.get("textFieldFolderScanKd", USER_HOME) + SP +listFileTable.getSelectionModel().getSelectedItem().getNameFile();
            try {
                Files.copy(Paths.get(fullFileName), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error("Ошибка при копировании файла " +path+".",e);
                e.printStackTrace();
            }
            id= S4AppThread.createFileDocumentWithDocType(S4AppThread,path, docType, archive, designation, textAreaName.getText(), SECTION_ID);//создаю документ
            if (id > 0) {
                S4AppThread.openDocVersion(S4AppThread,id, 0);
                S4AppThread.setFieldValue(S4AppThread,"Формат", comboBoxFormatPdfViewController.getSelectionModel().getSelectedItem());
                S4AppThread.setFieldValue(S4AppThread,"Кол-во листов", labelNumberOfSheets.getText());
                if (checkBoxRegOtd.isSelected()) {
                    S4AppThread.setFieldValue_DocVersion(S4AppThread,"OTD_STATUS", "1");//присваиваю статус ОТД зарегистрирован
                    S4AppThread.setFieldValue_DocVersion(S4AppThread,"OTD_REG", DateUtil.parseDateToString(datePickerOtdNew.getValue()));//дату регистрации беру из DatePicker
                }
                //S4AppThread.saveChanges();
                S4AppThread.checkIn(S4AppThread);//возвращаю файл в архив
                S4AppThread.closeDocument(S4AppThread);//
                final int idAdapter=id;
                Platform.runLater(()->AlertUtilNew.message("Удачная регистрация.", "Айди документа=" + idAdapter,
                        "Сканированный документ занесен в архив.", Alert.AlertType.INFORMATION));
                new File(fullFileName).delete(); //удаляю файл после удачной регистрации
                Platform.runLater(this::updateTableView);//updateTableView();//обновляю файлы в списке
                disableButton(true);//Запрещаю нажатие на кнопку
            } else {
                Platform.runLater(()->AlertUtilNew.message("Неудачная регистрация.", "Обратитесь к любому администратору Search " +
                            "по телефонам 22-35,23-49 или 22-06", "Произошла непредвиденная ошибка.", Alert.AlertType.ERROR));
            }
        }catch (Exception e){
            log.error("Ошибка при добавлении файла в Search c айди="+id,e);
            e.printStackTrace();
        }finally{
            disableMainWindow(false);
            Platform.runLater(()->getMainApp().getProgressIndicatorController().getProgressIndicatorStage().close());
            S4AppUtil.closeThreadS4App();
        }
    }

    /**
     * Нажатие на кнопку добавить версию
     */
    @FXML
    private void addVersionInSearch(){
        String msg = "";
        String select=comboBoxMaskPdfViewController.getSelectionModel().getSelectedItem();
        int i = 0;
        if (!checkDesignationInSearchAndAddVersion(select)) msg += ++i + ")Заполните поле \"Обозначение\".\r\n";
        if (textFieldNumberChange.getText().isEmpty()) msg += ++i + ")Заполните поле \"№ изменения\".\r\n";
        if (radioButtonTermChange.isSelected() && !DateUtil.checkRightDate(datePickerTermChange))
            msg += ++i + ")Поставьте правильную дату в календаре \"Срок изменения\".\r\n";
        if (checkBoxNoteVersion.isSelected() && textAreaNote.getText().isEmpty())
            msg += ++i + ")Заполните поле \"Комментарий\".\r\n";
        if (checkBoxRegOtdVersion.isSelected() && !DateUtil.checkRightDate(datePickerOtdVersion))
            msg += ++i + ")Поставьте правильную дату в календаре \"Зарегистрировать в ОТД\".\r\n";
        if (!msg.isEmpty()){
            AlertUtilNew.message("Проверка полей.", msg, "Не заполнены следующие поля:", Alert.AlertType.WARNING);
        }else {
            disableMainWindow(true);
            progressIndicatorAlways();
            //запускаю в другом потоке добавление версии
            executorServiceLoad.submit(this::addVersionInSearchAdapter);
        }
    }
    private void addVersionInSearchAdapter(){
        int baseDocId=-1;
        try {
            S4AppUtil S4AppThread = S4AppUtil.returnAndCreateThreadS4App();
            byte actualizeNewVersions = 0;
            String designation = returnDesignation(S4AppThread,textFieldDesignation,comboBoxDocTypesPdfViewController.getSelectionModel().getSelectedItem(),null);//Обозначение с кодом типа документа
            baseDocId= S4AppThread.getDocID_ByDesignation(S4AppThread,designation);//Док Айди
            S4AppThread.openDocument(S4AppThread,baseDocId);
            //Проверяю взятие на редактирование документ и док айди
            if (baseDocId > 0 && S4AppThread.getDocStatus(S4AppThread) == 0) {
                S4AppThread.closeDocument(S4AppThread);
                String versionCode = textFieldNumberChange.getText();//Номер изменения
                String versionNote = textAreaNote.getText();//Комментарий к версии документа
                String stringVersionFileName = copyAndReturnFullPath(designation, S4AppThread.getDocMaxVersionID(S4AppThread,baseDocId) + 1,preferencesScanKdAndTd.get("textFieldFolderMove", USER_HOME));//Составляю имя файла версии
                S4AppThread.openQuery(S4AppThread,"Select reasoncode from rreasons where reasontext=\"" + comboBoxChangeReasonPdfViewController.getSelectionModel().getSelectedItem() + "\"");
                int reasonCode = Integer.parseInt(S4AppThread.queryFieldByName(S4AppThread,"reasoncode"));//код причины изменения из БД Серча
                S4AppThread.closeQuery(S4AppThread);
                S4AppThread.createDocVersion(S4AppThread,baseDocId, S4AppThread.getDocMaxVersionID(S4AppThread,baseDocId), versionCode, versionNote, stringVersionFileName, reasonCode, 0);
                String fullFileName=preferencesScanKdAndTd.get("textFieldFolderScanKd", USER_HOME)+SP+listFileTable.getSelectionModel().getSelectedItem().getNameFile();
                try {
                    Files.copy(Paths.get(fullFileName), Paths.get(stringVersionFileName), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    log.error("Произошла ошибка при копировании файла:"+fullFileName,e);
                    e.printStackTrace();
                }
                S4AppThread.openDocVersion(S4AppThread,baseDocId, S4AppThread.getDocMaxVersionID(S4AppThread,baseDocId));
                //в зависимости от радиобаттонов актуализирую версию и присваиваю срок изменения
                if (radioButtonTermChange.isSelected()) {
                    S4AppThread.setFieldValue_DocVersion(S4AppThread,"termofchg", DateUtil.parseDateToString(datePickerTermChange.getValue()));
                } else if (radioButtonActualNow.isSelected()) actualizeNewVersions = 1;

                if (checkBoxRegOtdVersion.isSelected()) {
                    S4AppThread.setFieldValue_DocVersion(S4AppThread,"OTD_STATUS", "1");//присваиваю статус ОТД зарегистрирован
                    S4AppThread.setFieldValue_DocVersion(S4AppThread,"OTD_REG", DateUtil.parseDateToString(datePickerOtdVersion.getValue()));//дату регистрации беру из DatePicker
                }
                //S4App.setFieldValue_DocVersion("Кол-во листов",labelNumberOfSheets.getText());
                S4AppThread.saveWorkCopy(S4AppThread,baseDocId);
                S4AppThread.checkIn(S4AppThread,actualizeNewVersions);
                S4AppThread.closeDocument(S4AppThread);
                new File(fullFileName).delete(); //удаляю файл после удачной регистрации
                Platform.runLater(()->{
                    AlertUtilNew.message("Удачная регистрация версии документа.", "Ошибок при занесении " +
                            "в архив нет.", "Добавлена новая версия документа.", Alert.AlertType.INFORMATION);
                    updateTableView();//обновляю файлы в списке
                });
                disableButton(true);//Запрещаю нажатие на кнопку
            } else if (baseDocId == 0)
                Platform.runLater(()->AlertUtilNew.message("Оповещение.", "Документа с таким обозначением" +
                        " нет в Search.", "Информационное сообщение.", Alert.AlertType.WARNING));
            else {
                final String nameUser = S4AppThread.nfoGetUserFullNameByUserID(S4AppThread,S4AppThread.getDocStatus(S4AppThread));
                Platform.runLater(()->AlertUtilNew.message("Произошла ошибка.", "Документ взят на изменение пользователем " + nameUser + ".", "Не удалось добавить версию.", Alert.AlertType.ERROR));
            }
        } catch (Exception e) {
            log.error("Произошла ошибка при добавлении версии файла в Search с айди="+baseDocId,e);
            e.printStackTrace();
        } finally {
            disableMainWindow(false);
            Platform.runLater(()->getMainApp().getProgressIndicatorController().getProgressIndicatorStage().close());
            S4AppUtil.closeThreadS4App();
        }
    }

    /**
     * Выводит список версий при нажатии на кнопку "Список версий"
     */
    @FXML
    private void checkVersionListDoc() {
        String design=returnDesignation(S4App,textFieldDesignation,comboBoxDocTypesPdfViewController.getSelectionModel().getSelectedItem(),null);
        int id = S4App.getDocID_ByDesignation(S4App,design);
        if (id>0){
            S4App.hideSearch(S4App);
            S4App.showSearch(S4App);
            S4App.openDocument(S4App,id);
            S4App.showVersionList(S4App);
            S4App.closeDocument(S4App);
            getMainApp().getPrimaryStage().setAlwaysOnTop(true);
            getMainApp().getPrimaryStage().setAlwaysOnTop(false);
        }else
            AlertUtilNew.message("Оповещение.", "Документа с таким обозначением нет в Search.", "Информационное сообщение.", Alert.AlertType.INFORMATION);
    }

    /**
     * Обновление таблицы с файлами при нажатии на кнопку "Обновить"
     */
    @FXML
    private void updateTableView() {
        getMainApp().getStringNameFileData().clear();
        findPdfFile(preferencesScanKdAndTd.get("textFieldFolderScanKd", USER_HOME));
    }

    /**
     * Отображение пдф файла при двойном нажатии на список таблицы
     */
    private void addListenerTableViewList(){
        listFileTable.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                if (mouseEvent.getClickCount()==2){
                    StringData row = listFileTable.getSelectionModel().getSelectedItem();
                    pagination.getScene().getRoot().setDisable(true);
                    disableButton(false);
                    String fullNameFile;
                    if (row == null) return;
                    Path pathDestination;
                    //Подчищаю за собой файлы темпа
                    if (deleteMusorPath != null && deleteMusorPath.getParent().toFile().isDirectory()) {
                        File[] list = deleteMusorPath.getParent().toFile().listFiles();
                        for (int i = 0; i < list.length; i++) {
                            list[i].delete();
                        }
                    }
                    if (!Paths.get(USER_HOME + SP + "AppData" + SP + "Local" + SP + "Temp" + SP + "ScanKdAndTd" + SP).toFile().exists())
                        Paths.get(USER_HOME + SP + "AppData" + SP + "Local" + SP + "Temp" + SP + "ScanKdAndTd" + SP).toFile().mkdirs();//создаю свою директорию,если ее нету
                    pathDestination = Paths.get(USER_HOME + SP + "AppData" + SP + "Local" + SP + "Temp" + SP + "ScanKdAndTd" + SP + new Date().getTime() + ".pdf");
                    fullNameFile = preferencesScanKdAndTd.get("textFieldFolderScanKd", USER_HOME) + SP + row.getNameFile();
                    try {
                        Files.copy(Paths.get(fullNameFile), pathDestination, StandardCopyOption.REPLACE_EXISTING);//Первый элемент:что копирую,2ой:куда копирую,3ий:перезаписать,если файл есть.
                    } catch (IOException e) {
                        log.error("Не смог скопировать этот файл:"+ fullNameFile,e);
                        e.printStackTrace();
                    }
                    deleteMusorPath = pathDestination;//даю ссылку на файл
                    final File file = pathDestination.toFile();
                    if (file != null) {
                        Window window = pagination.getScene().getWindow();  //получаю ссылку на окно,где находится Pagination
                        if (window instanceof Stage) {  //проверяю тип окна
                            ((Stage) window).setTitle(file.getName());   //назначаю Титульник по названию выделенному файлу
                        }
                        final Task<PDFFile> loadFileTask = new Task<PDFFile>() {
                            @Override
                            protected PDFFile call() throws Exception {
                                try (
                                        RandomAccessFile raf = new RandomAccessFile(file, "r");
                                        FileChannel channel = raf.getChannel()
                                ) {
                                    ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                                    return new PDFFile(buffer);
                                }
                            }
                        };
                        loadFileTask.setOnSucceeded(event -> {
                            pagination.getScene().getRoot().setDisable(false);
                            final PDFFile pdfFile = loadFileTask.getValue();
                            currentFile.set(pdfFile);
                            updateImage(pagination.getCurrentPageIndex(), DEGREE);//нужно чтобы нормально обновляло
                        });
                        loadFileTask.setOnFailed(event -> {
                            pagination.getScene().getRoot().setDisable(false);
                            log.error("Could not load file " + file.getName(),loadFileTask.getException());
                            AlertUtilNew.showErrorMessage("Could not load file " + file.getName(), loadFileTask.getException(), pagination);
                        });
                        //pagination.getScene().getRoot().setDisableAll(true);
                        pagesOfSheets = pagination.getCurrentPageIndex();
                        executorServiceLoad.submit(loadFileTask);
                    }

                }
            }
        });
    }

    /**При клике на кнопку Вывод наим.производится считывание из базы Серча наименования предыдущего заказа(выбрана маска "**-**** Дополнение №*")*/
    @FXML
    private void checkNameOfDesignation(){
        if (!textFieldDesignation.getText().isEmpty() && !textFieldDopNumberPdfView.getText().isEmpty()){
            int num=Integer.parseInt(textFieldDopNumberPdfView.getText().trim());
            String designOld=returnDesignation(S4App,textFieldDesignation,comboBoxDocTypesPdfViewController.getSelectionModel().getSelectedItem(),null);
            String design;
            if (num>1){
                design= designOld.replace("№"+num,"№"+(num-1));
            }else {
                design= designOld.replace(" Дополнение №"+num,"");
            }
            S4App.openQuery(S4App,"select name from doclist where designatio=\""+design+"\"");
            String name=S4App.queryFieldByName(S4App,"name");
            S4App.closeQuery(S4App);
            if (!name.isEmpty()) textAreaName.setText(name);
            else AlertUtilNew.message("Поиск наименования документа в Search.","Такого наименования нет в Search или оно пустое","Закончился поиск.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Проверка есть ли документ с таким обозначением в архивах Search при нажатии на кнопку "Проверить"
     */
    @FXML
    private void checkDesignation() {
        String design=returnDesignation(S4App,textFieldDesignation,comboBoxDocTypesPdfViewController.getSelectionModel().getSelectedItem(),null);
        int baseDocId = S4App.getDocID_ByDesignation(S4App,design);
        if (baseDocId>0)
            AlertUtilNew.message("Внимание!", "Такой документ уже есть в архиве.ID документа = " +baseDocId, "Сообщение для ознакомления!", Alert.AlertType.WARNING);
        else
            AlertUtilNew.message("Оповещение.", "Документа с таким обозначением нет в Search.", "Информационное сообщение.", Alert.AlertType.INFORMATION);
    }

    /**Проверка заполнения обозначения*/
    private boolean checkDesignationInSearchAndAddVersion(String str){
        boolean flag;
        if (STR_ZAKAZ_DOP.equals(str)){
            flag=(textFieldDesignation.getText()+" Дополнение №"+textFieldDopNumberPdfView.getText()).length()>=str.length();
        }else if (str.equals(DEFAULT_TEXT_COMBO_BOX_MASK)){
            flag=!textFieldDesignation.getText().isEmpty();
        }else flag=textFieldDesignation.getText().length()==str.length();
        return flag;
    }

    /**
     * Вешаю слушателя на TextFieldDesignation для проверки и очистки буффера обмена
     */
    public void addListenerTextFieldDesignation() {
        textFieldDesignation.textProperty().addListener((observable, oldValue, newValue) -> {
            //проверяю выбранную маску,если не Произвольно,то очистка буффера обмена
            if (!comboBoxMaskPdfViewController.getSelectionModel().getSelectedItem().equals(DEFAULT_TEXT_COMBO_BOX_MASK)) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final String str = newValue.replace(oldValue, "");
                if (str.equals(clipboard.getString())) {
                    textFieldDesignation.setText(oldValue);
                    clipboard.clear();
                }
            }
            /**Вешаю слушателя на TextFieldDesignation для разблокировки кнопок проверить и список версий*/
            if (!comboBoxMaskPdfViewController.getSelectionModel().getSelectedItem().equals(STR_ZAKAZ_DOP)){
                buttonCheckDesignation.setDisable(newValue.isEmpty());
                buttonListVersion.setDisable(newValue.isEmpty());
            }else if (!textFieldDopNumberPdfView.getText().isEmpty()){
                buttonCheckDesignation.setDisable(newValue.isEmpty());
                buttonListVersion.setDisable(newValue.isEmpty());
                buttonDeductNamePdfView.setDisable(newValue.isEmpty());
            }
        });
    }

    /**Вешаю слушателя на TextFieldDopNumber для разблокировки кнопки проверить*/
    public void addListenerTextFieldDopNumberPdfView(){
        textFieldDopNumberPdfView.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!textFieldDesignation.getText().isEmpty()) {
                buttonCheckDesignation.setDisable(newValue.isEmpty());
                buttonDeductNamePdfView.setDisable(newValue.isEmpty());
                buttonListVersion.setDisable(newValue.isEmpty());
            }
        });
    }

    /**
     * Вешаю слушателя на КомбоБокс выбора Маски
     */
    public void addListenerComboBoxMaskPdfView() {
        comboBoxMaskPdfViewController.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue!=null && !DEFAULT_TEXT_COMBO_BOX_MASK.equals(newValue)) {
                textFieldDesignation.setTextFormatter(TextFormatterUtil.gTextFormatter(newValue, textFieldDesignation));
                tooltipDesignationPdfView.setText("Ввод только по выбранной маске.");
            }else {
                textFieldDesignation.setTextFormatter(null);
                textFieldDesignation.clear();
                tooltipDesignationPdfView.setText("Допускается ввод любого обозначения.");
            }
            if (newValue!=null && newValue.equals(STR_ZAKAZ_DOP)){
                labelDopNumberPdfView.setText(" Доп.№");
                labelDopNumberPdfView.setMinWidth(40);
                labelDopNumberPdfView.setVisible(true);
                textFieldDopNumberPdfView.setVisible(true);
                textFieldDopNumberPdfView.setMinWidth(50);
                textFieldDesignation.setMinWidth(197-50-40);
                buttonListVersion.setDisable(true);
                buttonCheckDesignation.setDisable(true);
            }else {
                textFieldDesignation.clear();
                textFieldDopNumberPdfView.clear();
                labelDopNumberPdfView.setText("");
                labelDopNumberPdfView.setVisible(false);
                textFieldDopNumberPdfView.setVisible(false);
                textFieldDesignation.setMinWidth(197);
                buttonDeductNamePdfView.setDisable(true);
            }
        });
    }

    /**
     * Добавляю слушателя для ЧекБокса "Произвольно", при выборе меняется значение Изменения выдающего списка
     */
    private void addListenerCheckBoxArbitrarily() {
        checkBoxArbitrarily.selectedProperty().addListener((observable, oldValue, newValue) -> {
            comboBoxFormatPdfViewController.setEditable(newValue);
            if (!newValue) comboBoxFormatPdfViewController.getSelectionModel().select(0);
        });
        checkBoxBuroOgtTd.selectedProperty().addListener((observable, oldValue, newValue) -> {
            comboBoxBuroOgtTd.setEditable(newValue);
            if (!newValue) comboBoxBuroOgtTd.getSelectionModel().select(0);
        });
    }

    /**Заполнение любого комбоБокса из данных реестра*/
    public void containComboBox(ComboBox comboBox){
        final String idComboBox=comboBox.getId();
        if (!comboBox.getItems().isEmpty()){
            comboBox.getItems().removeAll(comboBox.getItems());
        }
        Preferences pref=null;
        if (idComboBox.contains("comboBoxMaskPdfViewController")) {
            comboBox.getItems().add(DEFAULT_TEXT_COMBO_BOX_MASK);
            pref=preferencesTableViewInputMask;
        }else if (idComboBox.contains("comboBoxDocTypesPdfViewController")){
            pref=preferencesTableViewDocTypes;
        }else if (idComboBox.contains("comboBoxFormatPdfViewController")){
            pref=preferencesTableViewFormats;
        }
        String keys[]= new String[0];
        try {
            keys = pref.keys();
        } catch (Exception e) {
            log.error("Ошибка при чтении реестра.",e);
            e.printStackTrace();
        }
        for (int i = 0; i < keys.length; i++) {
            comboBox.getItems().add(keys[i]);
        }
        comboBox.getSelectionModel().select(0);
    }

    /**Обертка на главный метод заполнения(Для параллельного программирования).Заполнение ComboBox Масок из реестра при начальной инициализации и при нажатии на кнопку сохранить формы Options*/
    public void containComboBoxMask() {
        containComboBox(comboBoxMaskPdfViewController);
    }

    /**Обертка на главный метод заполнения(Для параллельного программирования).Заполнение ComboBox типы документов,которые хранятся в настройках (чтение таблицы TableViewDocTypes из формы Options)*/
    public void containComboBoxFormats() {
        containComboBox(comboBoxFormatPdfViewController);
    }

    /**Обертка на главный метод заполнения(Для параллельного программирования).Заполнение ComboBox типы документов,которые хранятся в настройках (чтение таблицы TableViewDocTypes из формы Options)*/
    public void containComboBoxDocTypes() {
        containComboBox(comboBoxDocTypesPdfViewController);
    }

    /**Заполнение ComboBox причина изменения(чтение из базы Search)*/
    public void containComboBoxChangeReason() {
                ObservableList<String> list = FXCollections.observableArrayList();
                S4App.openQuery(S4App,"Select reasontext from rreasons where reasoncode>=0");
                for (S4App.queryGoFirst(S4App); S4App.queryEOF(S4App) == 0; S4App.queryGoNext(S4App)) {
                    list.add(S4App.queryFieldByName(S4App,"reasontext"));
                }
                S4App.closeQuery(S4App);
                comboBoxChangeReasonPdfViewController.setItems(list);
                comboBoxChangeReasonPdfViewController.getSelectionModel().select(1);
    }
    private void containComboBoxBuroOgtTd(){
        comboBoxBuroOgtTd.getItems().addAll(arrayBuroOgt);
        comboBoxBuroOgtTd.getSelectionModel().select(0);
    }

    // ************ Initialization *************
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /**Заполнение списка в таблице данными(вывод файлов)*/
        filePathColumn.setCellValueFactory(cellDate -> cellDate.getValue().nameFileProperty());
        /**Задаю начальную дату в DataPicker(сегодня)*/
        initValueDataPicker();
        //Инициализирую подсказку
        /**Скрываю лабели доп текста и текстфилд*/
        textFieldDopNumberPdfView.setTextFormatter(TextFormatterUtil.gTextFormatter("***",textFieldDopNumberPdfView));

        createAndConfigureExecutorsLoadService();

        currentFile = new SimpleObjectProperty<>();

        currentImage = new SimpleObjectProperty<>();
        scroller.contentProperty().bind(currentImage);

        zoom = new SimpleDoubleProperty(1);
        zoom.addListener((observable, oldValue, newValue) -> updateImage(pagination.getCurrentPageIndex(), DEGREE));
        labelCurrentZoom.textProperty().bind(Bindings.format("%.0f %%", zoom.multiply(100)));

        /**Чтение из БД Серча причин изменения из таблицы rreasons*/
        Platform.runLater(() -> containComboBoxChangeReason());
        Platform.runLater(() -> containComboBoxMask());
        Platform.runLater(() -> containComboBoxDocTypes());
        Platform.runLater(() -> containComboBoxFormats());
        Platform.runLater(() -> containComboBoxBuroOgtTd());
        executorServiceLoad.submit(()->{
            addListenerTextFieldDesignation();
            addListenerCheckBoxArbitrarily();
            addListenerComboBoxMaskPdfView();
            addListenerTextFieldDopNumberPdfView();
            addListenerTableViewList();
            initTableColumnAll();
            bindingButtonExportInSearchTp();
        });
        bindPaginationToCurrentFile();
        createPaginationPageFactory();
        hideFormPdfView();
    }

    private void createAndConfigureExecutorsLoadService() {
        executorServiceLoad = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
    }

    private void bindPaginationToCurrentFile() {
        currentFile.addListener((observable, oldFile, newFile) -> {
            if (newFile != null) {
                pagination.setCurrentPageIndex(0);
                labelNumberOfSheets.setText(String.valueOf(newFile.getNumPages()));//Назначаю количество листов labelNumberOfSheets
            }
        });
        pagination.pageCountProperty().bind(new IntegerBinding() {
            {
                super.bind(currentFile);
            }

            @Override
            protected int computeValue() {
                return currentFile.get() == null ? 0 : currentFile.get().getNumPages();
            }
        });
        pagination.disableProperty().bind(Bindings.isNull(currentFile));
    }

    private void createPaginationPageFactory() {
        pagination.setPageFactory(pageNumber -> {
            if (currentFile.get() == null) {
                return null;
            } else {
                if (pageNumber >= currentFile.get().getNumPages() || pageNumber < 0) {
                    return null;
                } else {
                    updateImage(pageNumber, DEGREE);
                    return scroller;
                }
            }
        });
    }

    // ************** Event Handlers ****************
    /**Увеличение зумма*/
    @FXML
    private void zoomIn() {
        zoom.set(zoom.get() * ZOOM_DELTA);
    }
    /**Уменьшение зумма*/
    @FXML
    private void zoomOut() {
        zoom.set(zoom.get() / ZOOM_DELTA);
    }
    /**Максимально по высоте*/
    @FXML
    private void zoomFit() {
        double horizZoom = (scroller.getWidth() - 20) / currentPageDimensions.width;
        double verticalZoom = (scroller.getHeight() - 20) / currentPageDimensions.height;
        zoom.set(Math.min(horizZoom, verticalZoom));
    }
    /**Максимально по ширине*/
    @FXML
    private void zoomWidth() {
        zoom.set((scroller.getWidth() - 20) / currentPageDimensions.width);
    }

    /**
     * Поворачиваю ПДФ файл влево
     */
    @FXML
    private void zoomTurnOnLeft() {
        DEGREE -= Math.PI / 2;
        updateImage(pagination.getCurrentPageIndex(), DEGREE);
    }

    /**
     * Поворачиваю ПДФ файл вправо
     */
    @FXML
    private void zoomTurnOnRight() {
        DEGREE += Math.PI / 2;
        updateImage(pagination.getCurrentPageIndex(), DEGREE);

    }

    // *************** Загрузка в другом потоке картинки на основе pdf файла ****************

    private void updateImage(final int pageNumber, double deg) {
        final Task<ImageView> updateImageTask = new Task<ImageView>() {
            @Override
            protected ImageView call() throws Exception {
                PDFPage page = currentFile.get().getPage(pageNumber + 1);
                Rectangle2D bbox = page.getBBox();
                //узнаю ширину и высоту страницы актуального ПДФ файла
                final double actualPageWidth = bbox.getWidth();
                final double actualPageHeight = bbox.getHeight();
                currentPageDimensions = new PageDimensions(actualPageWidth, actualPageHeight);
                // ширина и высота в зависимости от зума
                final int width = (int) (actualPageWidth * zoom.get());
                final int height = (int) (actualPageHeight * zoom.get());
                // создаю картинку из страницы ПДФ файла
                java.awt.Image awtImage = page.getImage(width, height, bbox, null, true, true);
                //перевожу изображение в буфферное изображение
                AffineTransform at = new AffineTransform();
                BufferedImage buffImage = null;
                if (DEGREE == Math.PI * 2 || DEGREE == -Math.PI * 2) DEGREE = 0d;//Обнуляю угол поворота при 2*ПИ или -2*ПИ
                at.rotate(deg);
                if (deg == Math.PI / 2 || deg == -Math.PI * 3 / 2) {
                    at.translate(0, -height);//Задаю смещение начала коордионат после поворота изображения, чтобы нормально отображалось при просмотре
                    buffImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);//создаю просмотрщик изображения с выосотой и шириной картинки
                } else if (deg == Math.PI || deg == -Math.PI) {
                    at.translate(-width, -height);
                    buffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                } else if (deg == Math.PI * 3 / 2 || deg == -Math.PI / 2) {
                    at.translate(-width, 0);
                    buffImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);
                } else if (deg == 0 || deg == -Math.PI * 2 || deg == Math.PI * 2) {
                    at.translate(0, 0);
                    buffImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                }
                //Меняю яркость изображения
                //RescaleOp rescaleOp=new RescaleOp(0.0f,40,null);
                //BufferedImage bufferedImageOp=rescaleOp.filter(buffImage,buffImage);
                //Graphics2D g2d = bufferedImageOp.createGraphics();
                Graphics2D g2d = buffImage.createGraphics();
                g2d.drawImage(awtImage, at, null);
                // конвертирую буфферное изображение в картинку Java FX:
                Image image = SwingFXUtils.toFXImage(buffImage, null);
                // wrap in image view and return:
                ImageView imageView = new ImageView(image);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);//   ДОБАВИЛ ДЛЯ ПРОВЕРКИ!!!!
                //Включаю кеш для картинки
                imageView.setCache(true);//   ДОБАВИЛ ДЛЯ ПРОВЕРКИ!!!!!
                return imageView;
            }
        };

        updateImageTask.setOnSucceeded(event -> {
            pagination.getScene().getRoot().setDisable(false);
            currentImage.set(updateImageTask.getValue());
        });

        updateImageTask.setOnFailed(event -> {
            pagination.getScene().getRoot().setDisable(false);
            log.error("Ошибка при отображении файла pdf.",updateImageTask.getException());
            updateImageTask.getException().printStackTrace();
        });

        pagination.getScene().getRoot().setDisable(true);
        executorServiceLoad.submit(updateImageTask);
    }

	/*
     * Struct-like class intended to represent the physical dimensions of a page in pixels
	 * (as opposed to the dimensions of the (possibly zoomed) view.
	 * Used to compute zoom factors for zoomToFit and zoomToWidth.
	 * 
	 */

    private class PageDimensions {
        private double width;
        private double height;

        PageDimensions(double width, double height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public String toString() {
            return String.format("[%.1f, %.1f]", width, height);
        }
    }

    /**Обновляет кол-во файлов в таблице*/
    private void findPdfFile(String str) {
        File[] list;
        File file = new File(str);
        int sum = 0;
        if (file.isDirectory()) {
            list = file.listFiles();
            for (int i = 0; i < list.length; i++) {
                if (list[i].isFile() && (list[i].getName().endsWith(".pdf") || list[i].getName().endsWith(".PDF"))) {
                    showStringData(list[i].getName());
                    sum++;
                }
            }
        }
        countFileLabel.setText(String.valueOf(sum));
    }

    private void showStringData(String string) {
        getMainApp().getStringNameFileData().add(new StringData(string));
    }

    public ComboBox<String> getComboBoxDocTypesPdfViewController() {
        return comboBoxDocTypesPdfViewController;
    }

    public MainApp getMainApp() {
        return mainApp;
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        listFileTable.setItems(mainApp.getStringNameFileData());
    }

    public PdfViewerController() {
    }
    public ExecutorService getExecutorServiceLoad() {
        return executorServiceLoad;
    }

    //
    //
    //  Методы для Техн. Документации
    //
    //
    /**Инициализация Таблиц и Столбцов во вкладке <<Сканирование ТД>>,а также всех тектфилдов.*/
    private void initTableColumnAll(){
        tableColDesignObjectTp.setCellValueFactory(new PropertyValueFactory<>("designation"));
        tableColNameObjectTp.setCellValueFactory(new PropertyValueFactory<>("name"));
        tableColArtIdObjectTp.setCellValueFactory(new PropertyValueFactory<>("artId"));
        tableColNumberObjectTp.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(tableViewObjectTp.getItems().indexOf(param.getValue())+1));
        tableColDesignNumSheet.setCellValueFactory(new PropertyValueFactory<>("textFieldDesignNumberSheet"));
        tableColNumberSheet.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(tableViewNumberSheet.getItems().indexOf(param.getValue())+1));
        tableColButtonMapSketch.setCellValueFactory(new PropertyValueFactory<>("buttonScan"));
        tableColTextFieldNumChangeMapSketch.setCellValueFactory(new PropertyValueFactory<>("textFieldNumChangeMapSketchTd"));
        tableColTextFieldDesignIIMapSketch.setCellValueFactory(new PropertyValueFactory<>("textFieldDesignIIMapSketchTd"));
        tableColTextFieldOtdRegNumMapSketch.setCellValueFactory(new PropertyValueFactory<>("textFieldOtdRegNumMapSketch"));
        tableColTextFieldMapSketch.setCellValueFactory(new PropertyValueFactory<>("textFieldDesignMapSketch"));
        tableColNumberMapSketch.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(tableViewMapSketchTd.getItems().indexOf(param.getValue())+1));

        textFieldDesignPathMapTd.setTextFormatter(TextFormatterUtil.gTextFormatter(STR_FORMAT_TD, textFieldDesignPathMapTd));
        textFieldDesignOperationMapTd.setTextFormatter(TextFormatterUtil.gTextFormatter(STR_FORMAT_TD, textFieldDesignOperationMapTd));
        textFieldDesignSheetMaterialTd.setTextFormatter(TextFormatterUtil.gTextFormatter(STR_FORMAT_TD, textFieldDesignSheetMaterialTd));
        textFieldDesignSetMapTd.setTextFormatter(TextFormatterUtil.gTextFormatter(STR_FORMAT_TD, textFieldDesignSheetMaterialTd));
        textFieldNumChangePathMapTd.setTextFormatter(TextFormatterUtil.gTextFormatter("***", textFieldNumChangePathMapTd));
        textFieldNumChangeOperationMapTd.setTextFormatter(TextFormatterUtil.gTextFormatter("***", textFieldNumChangeOperationMapTd));
        textFieldNumChangeSetMapTd.setTextFormatter(TextFormatterUtil.gTextFormatter("***", textFieldNumChangeSetMapTd));
        textFieldNumChangeSheetMaterialTd.setTextFormatter(TextFormatterUtil.gTextFormatter("***", textFieldNumChangeSheetMaterialTd));

    }
    /**Адаптер на exportInSearchTP()*/
    @FXML
    private void exportInSearchTpAdapter(){
        disableMainWindow(true);
        getMainApp().showProgressIndicator();
        getMainApp().getProgressIndicatorController().getProgressIndicatorStage().show();
        executorServiceLoad.submit(this::exportInSearchTP);
    }

    /**Выполняется при нажатии на кнопку <<Занести в Search>> на вкладке Сканирование ТД*/
    private void exportInSearchTP(){
        String text="";
        try {
            S4AppUtil S4AppThread = S4AppUtil.returnAndCreateThreadS4App();
            long archive=preferencesScanKdAndTd.getLong("textFieldArchiveIdTd",323);
            String designation=returnDesignation(S4AppThread, textFieldDesignPathMapTd,null,preferencesScanKdAndTd.getLong("textFieldTpIdTd",1000633));
            String fullName=copyAndReturnFullPath(designation,0,preferencesScanKdAndTd.get("textFieldFolderScanTd", USER_HOME));
            long docType=preferencesScanKdAndTd.getLong("textFieldTpIdTd",1000633);
            String docName="Техпроцесс сканированный";//Наименование документа ТП
            ProgressIndicator progInd=getMainApp().getProgressIndicatorController().getProgressIndicator();
            progInd.setProgress(0.2d);
            long id=S4AppThread.createFileDocumentWithDocType(S4AppThread,fullName,docType,archive,designation,docName,SECTION_ID);
            if (id>0) {
                String temp="";
                ObservableList list;
                int link=0;
                long artId=S4AppThread.getArtID_ByDesignation(S4AppThread,designation);
                S4AppThread.openDocVersion(S4AppThread,id,0);
                S4AppThread.setFieldValue_DocVersion(S4AppThread,"OTD_STATUS", "1");//присваиваю статус ОТД зарегистрирован
                S4AppThread.setFieldValue_DocVersion(S4AppThread,"OTD_REG", DateUtil.parseDateToString(DateUtil.NOW_LOCAL_DATE()));//дата регистрации
                progInd.setProgress(0.4d);
                S4AppThread.closeDocument(S4AppThread);
                S4AppThread.openDocument(S4AppThread,id);
                if (!textFieldRouteCeh.getText().isEmpty()) S4AppThread.setFieldValue(S4AppThread,"Маршрут изготовления",textFieldRouteCeh.getText());
                S4AppThread.setFieldValue(S4AppThread,"Наименование бюро разработчика",comboBoxBuroOgtTd.getSelectionModel().getSelectedItem());
                list=tableViewNumberSheet.getItems();
                for (int i = 0; i < list.size(); i++) {
                    temp+=((NumberSheet)list.get(i)).getTextFieldDesignNumberSheet().getText()+";";

                }
                progInd.setProgress(0.6d);
                if (!temp.isEmpty()) S4AppThread.setFieldValue(S4AppThread,"Номер ведомости ТП на покрытие",temp);
                S4AppThread.checkIn(S4AppThread);//возвращаю файл в архив
                S4AppThread.closeDocument(S4AppThread);
                list=tableViewObjectTp.getItems();
                for (int i = 0; i <list.size() ; i++) {
                    link=S4AppThread.linkDocToArticle(S4AppThread,((ObjectTp)list.get(i)).getArtId(),id,1,0);
                    if (link==0) text+="Произошла ошибка при включении документации с айди="+id+" на объект с арт. айди="+((ObjectTp)list.get(i)).getArtId()+"\r\n";
                    link=0;
                }
                progInd.setProgress(0.8d);
                for (int i = 0; i <listDocIdScan.size() ; i++) {
                    //link=S4AppThread.linkDocVersionToArticleVersion(S4AppThread,artId,0,listDocIdScan.get(i),0,1,0);
                    link=S4AppThread.linkDocToArticle(S4AppThread,artId,listDocIdScan.get(i),1,0);
                    if (link==0) text+="Произошла ошибка при включении документации с айди="+listDocIdScan.get(i)+" на объект с арт. айди="+artId+"\r\n";
                    link=0;
                }
                progInd.setProgress(1.0d);
            }
        }finally {
            final String textAlert=text;
            Platform.runLater(()->{
                getMainApp().getProgressIndicatorController().getProgressIndicatorStage().close();
                if (!"".equals(textAlert)) Platform.runLater(()->AlertUtilNew.message("Ошибки.","В процессе занесения в архив Техпроцесса" +
                        " произошли такие ошибки:"+textAlert,"Ошибки Search.", Alert.AlertType.ERROR));
                else AlertUtilNew.message("Успешно.","Техпроцесс успешно создан и все привязки документации " +
                        "к объектам были выполнены успешно.","Техпроцесс создан в Search.", Alert.AlertType.INFORMATION);
                AnchorPane tabContent=(AnchorPane)tabScanTdPdfView.getContent();
                enableAll(tabContent.getChildren());
                clearAll(tabContent.getChildren());
            });
            listDocIdScan.clear();
            disableMainWindow(false);
            S4AppUtil.closeThreadS4App();
        }
    }
    /**Выполняется при нажатии на кнопку <<Выбрать из Search>> на вкладке Сканирование ТД*/
    @FXML
    public void clickButtonSelectInSearchObject(){
        disableMainWindow(true);
        executorServiceLoad.submit(this::clickButtonSelectInSearchObjectThread);
        /**запускаю в другом потоке заполнение таблицы Imbase*/
        //if (getMainApp().getImbaseTreeController().getTreeItemImbase()==null){
            executorServiceLoad.submit(() -> getMainApp().showImbaseTree());
        //}
    }
    /**Запуск в потоке открытия окна выбора объектов из Search*/
    @FXML
    public void clickButtonSelectInSearchObjectThread(){
        S4AppUtil S4AppThread = S4AppUtil.returnAndCreateThreadS4App();
        try {
            int idUser = S4AppThread.getUserID(S4AppThread);
            S4AppThread.openQuery(S4AppThread, "select sample_id from smplist where samplename=\"ScanKdAndTd\" and user_id=\"" + idUser + "\"");
            String strTemp = S4AppThread.queryFieldByName(S4AppThread, "sample_id");
            S4AppThread.closeQuery(S4AppThread);
            if (strTemp.isEmpty()) {
                Platform.runLater(()->AlertUtilNew.message("Внимание!", "Для выборки из определенных типов объектов (детали и сборочные единицы) " +
                        "создайте персональную выборку в узле 'Объекты' дерева Search с именем ScanKdAndTd.", "Обратите внимание!", Alert.AlertType.WARNING));
            } else {
                S4AppThread.startSelectArticles(S4AppThread);
                S4AppThread.hideSearch(S4AppThread);
                S4AppThread.showSearch(S4AppThread);
                S4AppThread.selectArticlesSample(S4AppThread,-2,"ScanKdAndTd",SELECT_SQL_IN_OBJECT,"");
                //S4AppThread.selectArticlesBySectID(S4AppThread,SECTION_ID_DETAIL,-1);
                for (int i = 0; i < S4AppThread.selectedArticlesCount(S4AppThread); i++) {
                    S4AppThread.openArticle(S4AppThread, S4AppThread.getSelectedArticleID(S4AppThread, i));
                    String design=S4AppThread.getArticleDesignation(S4AppThread);
                    tableViewObjectTp.getItems().add(new ObjectTp(design, S4AppThread.getArticleName(S4AppThread),S4AppThread.getArtID_ByDesignation(S4AppThread,design)));
                    S4AppThread.closeArticle(S4AppThread);
                }
                S4AppThread.endSelectArticles(S4AppThread);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Произошла ошибка при выборе объектов из Search во вкладке <<Сканирование ТД>>", e);
        } finally {
            S4AppUtil.closeThreadS4App();
            disableMainWindow(false);

        }
    }

    /**Выполняется на кнопку "Добавить карту эскизов",добавляется строка с текстфилдом и кнопкой в таблицу Карт эскизов*/
    @FXML
    private void addMapSketch(){
        TextField textFieldDesignMapSketch=new TextField();
        TextField textFieldOtdRegNumMapSketch=new TextField();
        TextField textFieldDesignIIMapSketchTd=new TextField();
        TextField textFieldNumChangeMapSketchTd=new TextField();
        Button button=new Button("Сканировать");
        Tooltip tooltip=new Tooltip("Допускается ввод только по маске \"В25.*****.*****\"");
        textFieldDesignMapSketch.setTooltip(tooltip);
        tooltip=new Tooltip("Допускается ввод номера до 1000(включительно).");
        textFieldNumChangeMapSketchTd.setTooltip(tooltip);
        textFieldDesignMapSketch.setTextFormatter(TextFormatterUtil.gTextFormatter(STR_FORMAT_TD,textFieldDesignMapSketch));
        textFieldNumChangeMapSketchTd.setTextFormatter(TextFormatterUtil.gTextFormatter("***",textFieldNumChangeMapSketchTd));
        button.setOnMouseClicked(event -> {
        long docType=preferencesScanKdAndTd.getLong("textFieldMapSketchIdTd",1000632);//doc_type "Карта эскизов и схем сканированая" из таблицы doctypes
        executorServiceLoad.submit(() -> getPdfFromScanner(textFieldDesignMapSketch,textFieldOtdRegNumMapSketch,textFieldNumChangeMapSketchTd,textFieldDesignIIMapSketchTd,button,"Карта эскизов и схем сканированая",docType));
        });
        tableViewMapSketchTd.getItems().add(new MapSketch(textFieldDesignMapSketch,textFieldOtdRegNumMapSketch,textFieldNumChangeMapSketchTd,textFieldDesignIIMapSketchTd,button));
    }

    /**Метод сканирования*/
    private void getPdfFromScanner(TextField textFieldDesign,TextField textFieldOtdRegNum,TextField textFieldNumChange,TextField textFieldDesignII,Button button, String name, Long docType){
        int idTemp;
        S4AppUtil S4AppThread = S4AppUtil.returnAndCreateThreadS4App();
        if (textFieldDesign.getText().length() != STR_FORMAT_TD.length()) {
            Platform.runLater(() -> AlertUtilNew.message("Внимание!", "Заполните поле обозначение.", "Пустое поле обозначение.", Alert.AlertType.WARNING));
        } else if ((idTemp = S4AppThread.getDocID_ByDesignation(S4AppThread, textFieldDesign.getText())) > 0) {
            Platform.runLater(() -> AlertUtilNew.message("Внимание!", "Документ с таким обозначением уже есть в архиве Search.Айди документа=" + idTemp, "Сообщение для ознакомления", Alert.AlertType.WARNING));
        } else {
            disableMainWindow(true);
            try {
                Platform.runLater(()->progressIndicatorAlways());
                String design = textFieldDesign.getText();
                int DPI = preferencesScanKdAndTd.getInt("textFieldDpiScanTd", 450);
                String fullFileName = preferencesScanKdAndTd.get("textFieldFolderScanTd", USER_HOME) + SP + design + " Скан.pdf";
                String fullFileNameTif = preferencesScanKdAndTd.get("textFieldFolderScanTd", USER_HOME) + SP + design + " Скан.tif";
                long archive = preferencesScanKdAndTd.getLong("textFieldArchiveIdTd", 323);
                S4AppThread.getImageFromScanner(S4AppThread, fullFileNameTif, -1, 0, DPI, 1, false, false, false, false);
                //создаю новый файл с расширением PDF
                File file = new File(convertTiff2Pdf(fullFileNameTif));
                if (!file.exists()) file.createNewFile();
                //удаляю файл с расширением тиф
                new File(fullFileNameTif).delete();
                long id = S4AppThread.createFileDocumentWithDocType(S4AppThread, fullFileName, docType, archive, design, name, SECTION_ID);//создаю документ
                if (id>0) {
                    listDocIdScan.add(id);
                    //S4AppThread.openDocument(S4AppThread, id);
                    S4AppThread.openDocVersion(S4AppThread,id,0);
                    S4AppThread.setFieldValue_DocVersion(S4AppThread,"OTD_STATUS", "1");//присваиваю статус ОТД зарегистрирован
                    S4AppThread.setFieldValue_DocVersion(S4AppThread,"OTD_REG", DateUtil.parseDateToString(DateUtil.NOW_LOCAL_DATE()));//дата регистрации
                    if (!textFieldOtdRegNum.getText().isEmpty()) S4AppThread.setFieldValue_DocVersion(S4AppThread,"OTDREGNUM", textFieldOtdRegNum.getText());//регистрационный номер
                    if (!textFieldNumChange.getText().isEmpty()) S4AppThread.setFieldValue_DocVersion(S4AppThread,"VER_CODE", textFieldNumChange.getText());//номер изменения ИИ
                    if (!textFieldDesignII.getText().isEmpty()) S4AppThread.setFieldValue_DocVersion(S4AppThread,"VER_NOTE", textFieldDesignII.getText());//в комментарии записываю номер ИИ
                    S4AppThread.saveChanges(S4AppThread);
                    S4AppThread.checkIn(S4AppThread);
                    S4AppThread.closeDocument(S4AppThread);
                }
            }catch(IOException e){
                e.printStackTrace();
                log.error("Ошибка при удалении темп файла.",e);
            } finally {
                //Закрываю поток в любом случае
                S4AppUtil.closeThreadS4App();
                textFieldDesign.setDisable(true);
                textFieldDesignII.setDisable(true);
                textFieldNumChange.setDisable(true);
                textFieldOtdRegNum.setDisable(true);
                button.setDisable(true);
                Platform.runLater(()->getMainApp().getProgressIndicatorController().getProgressIndicatorStage().close());
                disableMainWindow(false);
            }
        }

    }
    /**Выполняется на кнопку "Добавить ведомость",добавляется строка с текстфилдом в таблицу Ведомостей*/
    @FXML
    private void addNumberSheet(){
        TextField textField=new TextField();
        Tooltip tooltip=new Tooltip(TOOLTIP_STR_FORMAT_TD);
        textField.setTooltip(tooltip);
        //textField.setTextFormatter(TextFormatterUtil.gTextFormatter(STR_FORMAT_TD,textField));
        tableViewNumberSheet.getItems().add(new NumberSheet(textField));
    }
    /**удаляется строка из таблицы Объектов*/
    @FXML
    private void deleteObjectTp(){
        OptionsController.deleteRowsInTable(tableViewObjectTp);
    }
    /**удаляется строка из таблицы Карт эскизов*/
    @FXML
    private void deleteMapSketchTd(){
        OptionsController.deleteRowsInTable(tableViewMapSketchTd);
    }
    /**удаляется строка из таблицы Номеров ведомостей*/
    @FXML
    private void deleteNumberSheet(){
        OptionsController.deleteRowsInTable(tableViewNumberSheet);
    }

    @FXML
    private void clickButtonScanRouteMapTd(){
        long docType=preferencesScanKdAndTd.getLong("textFieldPathMapIdTd",1000628);//doc_type "Маршрутной карты сканированной" из таблицы doctypes
        executorServiceLoad.submit(() -> getPdfFromScanner(textFieldDesignPathMapTd,textFieldOtdRegNumPathMapTd,textFieldNumChangePathMapTd,textFieldDesignationIIPathMapTd,buttonRouteMapScanTd,"Маршрутная карта сканированная",docType));

    }
    @FXML
    private void clickButtonScanSetMapTd(){
        long docType=preferencesScanKdAndTd.getLong("textFieldSetMapIdIdTd",1000629);//doc_type "Комплектовочная карта сканированной" из таблицы doctypes
        executorServiceLoad.submit(() -> getPdfFromScanner(textFieldDesignSetMapTd,textFieldOtdRegNumSetMapTd,textFieldNumChangeSetMapTd,textFieldDesignationIISetMapTd,buttonSetMapScanTd,"Комплектовочная карта сканированная",docType));

    }
    @FXML
    private void clickButtonScanSheetMaterialTd(){
        long docType=preferencesScanKdAndTd.getLong("textFieldSheetMaterialIdTd",1000630);//doc_type "Ведомость материалов сканированной" из таблицы doctypes
        executorServiceLoad.submit(() -> getPdfFromScanner(textFieldDesignSheetMaterialTd,textFieldOtdRegNumSheetMaterialTd,textFieldNumChangeSheetMaterialTd,textFieldDesignationIISheetMaterialTd,buttonSheetMaterialScanTd,"Ведомость материалов сканированная",docType));

    }
    @FXML
    private void clickButtonScanOperationMapTd(){
        long docType=preferencesScanKdAndTd.getLong("textFieldOperationMapIdTd",1000631);//doc_type "Операционная карта сканированной" из таблицы doctypes
        executorServiceLoad.submit(() -> getPdfFromScanner(textFieldDesignOperationMapTd,textFieldOtdRegNumOperationMapTd,textFieldNumChangeOperationMapTd,textFieldDesignationIIOperationMapTd,buttonOperationMapScanTd,"Операционная карта сканированная",docType));

    }

    /**Метод конвертации тиф в pdf*/
    private static String convertTiff2Pdf(String tiff) {
        // target path PDF
        String pdf = null;
        try {
            pdf = tiff.substring(0, tiff.lastIndexOf('.') + 1) + "pdf";
            // новый документ в формате А4
            Document document = new Document(PageSize.LETTER, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdf));
            int pages = 0;
            document.open();
            PdfContentByte cb = writer.getDirectContent();
            RandomAccessFileOrArray ra = null;
            int comps = 0;
            ra = new RandomAccessFileOrArray(tiff);
            comps = TiffImage.getNumberOfPages(ra);
            // Сама конвертация
            for (int c = 0; c < comps; ++c) {
                com.itextpdf.text.Image img = TiffImage.getTiffImage(ra, c + 1);
                if (img != null) {
                    img.scalePercent(7200f / img.getDpiX(), 7200f / img.getDpiY());
                    document.setPageSize(new com.itextpdf.text.Rectangle(img.getScaledWidth(),img.getScaledHeight()));
                    img.setAbsolutePosition(0, 0);
                    cb.addImage(img);
                    document.newPage();
                    ++pages;
                }
            }
            ra.close();
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Ошибка при ковертации файла tif в pdf",e);
            pdf = null;
        }
        return pdf;

    }
    /**Вызов формы выбора цехов*/
    @FXML
    private void handleImbaseTreeForm(){
        getMainApp().showImbaseTree();
        getMainApp().getImbaseTreeController().getImbaseDialogStage().showAndWait();
    }
    /**Вызыввается метод при нажатии на кнопку "Разблокировать все"*/
    @FXML
    private void enableButtonAndTextfield(){
        AnchorPane tabContent=(AnchorPane)tabScanTdPdfView.getContent();
        enableAll(tabContent.getChildren());
    }
    /**Разблокировка всех кнопок на форме ТД*/
    private void enableAll(ObservableList list){
        for (int i = 0; i <list.size() ; i++) {
            if (list.get(i).equals(buttonExportInSearchTp)) continue;
            switch (list.get(i).getClass().getSimpleName()){
                case "Button":
                    ((Button) list.get(i)).setDisable(false);
                    break;
                case "TextField":
                    ((TextField)list.get(i)).setDisable(false);
                    break;
                case "MapSketch":
                    ((MapSketch)list.get(i)).setDisableAll(false);
                    break;
                case "VBox":
                    enableAll(((VBox)list.get(i)).getChildren());
                    break;
                case "HBox":
                    enableAll(((HBox)list.get(i)).getChildren());
                    break;
                case "TableView":
                    enableAll(((TableView)list.get(i)).getItems());
                    break;
            }
        }
    }
    @FXML
    private void clearAllTextFieldTd(){
        AnchorPane tabContent=(AnchorPane)tabScanTdPdfView.getContent();
        clearAll(tabContent.getChildren());
    }

    /**Очистка всех текстфилдов на форме ТД*/
    private void clearAll(ObservableList list){
        for (int i = 0; i <list.size() ; i++) {
            switch (list.get(i).getClass().getSimpleName()){
                case "TextField":
                    TextField temp=(TextField)list.get(i);
                    if (temp!=null && temp.getTextFormatter()!=null && temp.getTextFormatter().getValue().equals("В25.")){
                        temp.setText("В25.");
                    }else temp.clear();
                    break;
                case "MapSketch":
                    ((MapSketch)list.get(i)).clearAllMapSketch();
                    break;
                case "NumberSheet":
                    ((NumberSheet)list.get(i)).clearAllNumberSheet();
                    break;
                case "VBox":
                    clearAll(((VBox)list.get(i)).getChildren());
                    break;
                case "HBox":
                    clearAll(((HBox)list.get(i)).getChildren());
                    break;
                case "TableView":
                    clearAll(((TableView)list.get(i)).getItems());
                    break;
            }
        }
    }
    /**Слушатель для блокировки и разблокировки кнопки 'Занести в Search'*/
    private void bindingButtonExportInSearchTp(){
        BooleanBinding booleanBinding= textFieldDesignPathMapTd.textProperty().length().isNotEqualTo(STR_FORMAT_TD.length())
                .or(Bindings.size(tableViewObjectTp.getItems()).isEqualTo(0));
        //BooleanBinding booleanBinding=(textFieldDesignPathMapTd.disableProperty().not())
        buttonExportInSearchTp.disableProperty().bind(booleanBinding);

    }

    public TextField getTextFieldRouteCeh() {
        return textFieldRouteCeh;
    }

    public void setTextFieldRouteCeh(TextField textFieldRouteCeh) {
        this.textFieldRouteCeh = textFieldRouteCeh;
    }
}
