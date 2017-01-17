package com.yandex.ajwar.view;

import com.sun.javafx.stage.StageHelper;
import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.yandex.ajwar.MainApp;
import com.yandex.ajwar.model.StringData;
import com.yandex.ajwar.util.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

public class PdfViewerController implements Initializable {

    private MainApp mainApp;
    private S4AppUtil S4App = S4AppUtil.getInstance();
    private static final Logger log=Logger.getLogger(MainApp.class);
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
    private static final String defaultTextComboBoxMask = "Произвольно";
    private static final String strZakazDop = "**-**** Дополнение №*";
    //private final String FULL_PATH_FILE ="\\AppData\\Local\\Temp\\_IMS\\";
    private static final byte SECTION_ID = 1; //Раздел документация(столбец SECTION_ID) в таблице SSECTION

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
    private TextField textFieldDesignation;
    @FXML
    private TextField textFieldNumberChange;
    @FXML
    private TextField textFieldDopNumberPdfView;
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
        if (flag) executorServiceLoad.submit(()->getMainApp().getPrimaryStage().getScene().getRoot().setDisable(true));
        else executorServiceLoad.submit(()->getMainApp().getPrimaryStage().getScene().getRoot().setDisable(false));
    }

    /**
     * Возвращает обозначение документа
     */
    private String returnDesignation(TextField field) {
        String str;
        S4App.openQuery(S4App,"select suffix,dt_code from doctypes where doc_name=\""+comboBoxDocTypesPdfViewController.getSelectionModel().getSelectedItem()+"\"");
        int suffix=Integer.parseInt(S4App.queryFieldByName(S4App,"suffix"));
        String code=S4App.queryFieldByName(S4App,"dt_code");
        S4App.closeQuery(S4App);
        if (!textFieldDopNumberPdfView.getText().isEmpty()) str=" Дополнение №"+textFieldDopNumberPdfView.getText();
            else str="";
        if (suffix==0)
                return field.getText()+str;
            else
                return field.getText()+str +" "+ code;
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
     * Формирование полного пути и копирование в рабочуюю документацию
     */
    private String copyAndReturnFullPath(String designation, int version) {
        String path;
        if (version == 0 && System.getProperty("os.name").indexOf("Win") != -1) {
            path = preferencesScanKdAndTd.get("textFieldFolderMove", System.getProperty("user.home")) + "\\" + designation + " Скан.pdf";
        } else if( version !=0 && System.getProperty("os.name").indexOf("Win") != -1){
            path = preferencesScanKdAndTd.get("textFieldFolderMove", System.getProperty("user.home")) + "\\" + designation + " [" + version + "] Скан.pdf";
        }else if (version==0 && System.getProperty("os.name").indexOf("Win") == -1){
            path = preferencesScanKdAndTd.get("textFieldFolderMove", System.getProperty("user.home")) + "/" + designation + " Скан.pdf";
        }else {
            path = preferencesScanKdAndTd.get("textFieldFolderMove", System.getProperty("user.home")) + "/" + designation + " [" + version + "] Скан.pdf";
        }
        return path;
    }

    /**
     * Блокировка кнопок по событиям
     */
    private void disableButton(boolean flag) {
        buttonExportInSearch.setDisable(flag);
        buttonAddVersionInSearch.setDisable(flag);
    }

    /**Невидимость форм окна при изменении в опциях ЧекБоксов*/
    public void disableFormPdfView(){
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

    /**
     * Нажатие на кнопку "Экспорт в Search"
     */
    @FXML
    private void exportInSearch() {
        String msg = "";
        String select=comboBoxMaskPdfViewController.getSelectionModel().getSelectedItem();
        int versionId = 0, i = 0;
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
            //Проверяю код документа и формирую полное обозначение
            String designation = returnDesignation(textFieldDesignation);
            //Полный путь к файлу
            String path = copyAndReturnFullPath(designation, versionId);
            //нахожу Айди типа документа из базы Серча
            S4App.openQuery(S4App,"select doc_type from doctypes where doc_name=\"" + comboBoxDocTypesPdfViewController.getSelectionModel().getSelectedItem() + "\"");
            long docType = Long.parseLong(S4App.queryFieldByName(S4App,"doc_type"));
            S4App.closeQuery(S4App);
            //присваиваю номер архива в переменную
            long archive = preferencesScanKdAndTd.getLong("textFieldArchiveId", 323);
            String fullFileName;
            if (System.getProperty("os.name").indexOf("Win") != -1){
                fullFileName=preferencesScanKdAndTd.get("textFieldFolderScan", System.getProperty("user.home"))+"\\"+listFileTable.getSelectionModel().getSelectedItem().getNameFile();
            }else {
                fullFileName=preferencesScanKdAndTd.get("textFieldFolderScan", System.getProperty("user.home"))+"/"+listFileTable.getSelectionModel().getSelectedItem().getNameFile();
            }
            try {
                Files.copy(Paths.get(fullFileName), Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error("Ошибка при копировании файла.",e);
                e.printStackTrace();
            }
            long id = S4App.createFileDocumentWithDocType(S4App,path, docType, archive, designation, textAreaName.getText(), SECTION_ID);//создаю документ
            if (id > 0) {
                S4App.openDocVersion(S4App,id, 0);
                try {
                    S4App.setFieldValue(S4App,"Формат", comboBoxFormatPdfViewController.getSelectionModel().getSelectedItem());
                    S4App.setFieldValue(S4App,"Кол-во листов", labelNumberOfSheets.getText());
                } catch (Exception e) {
                    log.error("Ошибка при добавлении в файл в Search параметров формат и кол-во листов.Файл с айди="+id,e);
                    e.printStackTrace();
                }
                if (checkBoxRegOtd.isSelected()) {
                    S4App.setFieldValue_DocVersion(S4App,"OTD_STATUS", "1");//присваиваю статус ОТД зарегистрирован
                    S4App.setFieldValue_DocVersion(S4App,"OTD_REG", DateUtil.parseDateToString(datePickerOtdNew.getValue()));//дату регистрации беру из DatePicker
                }
                //S4App.saveChanges();
                S4App.checkIn(S4App);//возвращаю файл в архив
                S4App.closeDocument(S4App);//
                AlertUtilNew.message("Удачная регистрация.", "Айди документа=" + id, "Сканированный документ занесен в архив.", Alert.AlertType.INFORMATION);
                new File(fullFileName).delete(); //удаляю файл после удачной регистрации
                updateTableView();//обновляю файлы в списке
                disableButton(true);//Запрещаю нажатие на кнопку
            } else {
                AlertUtilNew.message("Неудачная регистрация.", "Обратитесь к любому администратору Search по телефонам 22-35,23-49 или 22-06", "Произошла непредвиденная ошибка.", Alert.AlertType.ERROR);
            }
            disableMainWindow(false);
        }
    }

    /**
     * Нажатие на кнопку добавить версию
     */
    @FXML
    private void addVersionInSearch() {
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
            byte actualizeNewVersions = 0;
            String designation = returnDesignation(textFieldDesignation);//Обозначение с кодом типа документа
            int baseDocId = S4App.getDocID_ByDesignation(S4App,designation);//Док Айди
            S4App.openDocument(S4App,baseDocId);
            //Проверяю взятие на редактирование документ и док айди
            if (baseDocId > 0 && S4App.getDocStatus(S4App) == 0) {
                S4App.closeDocument(S4App);
                String versionCode = textFieldNumberChange.getText();//Номер изменения
                String versionNote = textAreaNote.getText();//Комментарий к версии документа
                String stringVersionFileName = copyAndReturnFullPath(designation, S4App.getDocMaxVersionID(S4App,baseDocId) + 1);//Составляю имя файла версии
                S4App.openQuery(S4App,"Select reasoncode from rreasons where reasontext=\"" + comboBoxChangeReasonPdfViewController.getSelectionModel().getSelectedItem() + "\"");
                int reasonCode = Integer.parseInt(S4App.queryFieldByName(S4App,"reasoncode"));//код причины изменения из БД Серча
                S4App.closeQuery(S4App);
                S4App.createDocVersion(S4App,baseDocId, S4App.getDocMaxVersionID(S4App,baseDocId), versionCode, versionNote, stringVersionFileName, reasonCode, 0);
                String fullFileName;
                if (System.getProperty("os.name").indexOf("Win") != -1){
                    fullFileName=preferencesScanKdAndTd.get("textFieldFolderScan", System.getProperty("user.home"))+"\\"+listFileTable.getSelectionModel().getSelectedItem().getNameFile();
                }else {
                    fullFileName=preferencesScanKdAndTd.get("textFieldFolderScan", System.getProperty("user.home"))+"/"+listFileTable.getSelectionModel().getSelectedItem().getNameFile();
                }
                try {
                    Files.copy(Paths.get(fullFileName), Paths.get(stringVersionFileName), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    log.error("Произошла ошибка при копировании файла:"+fullFileName,e);
                    e.printStackTrace();
                }
                S4App.openDocVersion(S4App,baseDocId, S4App.getDocMaxVersionID(S4App,baseDocId));
                //в зависимости от радиобаттонов актуализирую версию и присваиваю срок изменения
                if (radioButtonTermChange.isSelected()) {
                    S4App.setFieldValue_DocVersion(S4App,"termofchg", DateUtil.parseDateToString(datePickerTermChange.getValue()));
                } else if (radioButtonActualNow.isSelected()) actualizeNewVersions = 1;

                if (checkBoxRegOtdVersion.isSelected()) {
                    S4App.setFieldValue_DocVersion(S4App,"OTD_STATUS", "1");//присваиваю статус ОТД зарегистрирован
                    S4App.setFieldValue_DocVersion(S4App,"OTD_REG", DateUtil.parseDateToString(datePickerOtdVersion.getValue()));//дату регистрации беру из DatePicker
                }
                //S4App.setFieldValue_DocVersion("Кол-во листов",labelNumberOfSheets.getText());
                S4App.saveWorkCopy(S4App,baseDocId);
                S4App.checkIn(S4App,actualizeNewVersions);
                S4App.closeDocument(S4App);
                AlertUtilNew.message("Удачная регистрация версии документа.", "Ошибок при занесении в архив нет.", "Добавлена новая версия документа.", Alert.AlertType.INFORMATION);
                disableButton(true);//Запрещаю нажатие на кнопку
                new File(fullFileName).delete(); //удаляю файл после удачной регистрации
                updateTableView();//обновляю файлы в списке
            } else if (baseDocId == 0)
                AlertUtilNew.message("Оповещение.", "Документа с таким обозначением нет в Search.", "Информационное сообщение.", Alert.AlertType.WARNING);
            else {
                String nameUser = S4App.nfoGetUserFullNameByUserID(S4App,S4App.getDocStatus(S4App));
                AlertUtilNew.message("Произошла ошибка.", "Документ взят на изменение пользователем " + nameUser + ".", "Не удалось добавить версию.", Alert.AlertType.ERROR);
            }
            disableMainWindow(false);
        }
    }

    /**
     * Выводит список версий при нажатии на кнопку "Список версий"
     */
    @FXML
    private void checkVersionListDoc() {
        String design=returnDesignation(textFieldDesignation);
        int id = S4App.getDocID_ByDesignation(S4App,design);
        if (id>0){
            S4App.openDocument(S4App,id);
            S4App.showVersionList(S4App);
            S4App.closeDocument(S4App);
        }else
            AlertUtilNew.message("Оповещение.", "Документа с таким обозначением нет в Search.", "Информационное сообщение.", Alert.AlertType.INFORMATION);
    }

    /**
     * Обновление таблицы с файлами при нажатии на кнопку "Обновить"
     */
    @FXML
    private void updateTableView() {
        getMainApp().getStringNameFileData().clear();
        findPdfFile(preferencesScanKdAndTd.get("textFieldFolderScan", System.getProperty("user.home")));
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
                    if (System.getProperty("os.name").indexOf("Win") != -1) {
                        if (!Paths.get(System.getProperty("user.home") + "\\AppData\\Local\\Temp\\ScanKdAndTd\\").toFile().exists())
                            Paths.get(System.getProperty("user.home") + "\\AppData\\Local\\Temp\\ScanKdAndTd\\").toFile().mkdirs();//создаю свою директорию,если ее нету для Windows
                        pathDestination = Paths.get(System.getProperty("user.home") + "\\AppData\\Local\\Temp\\ScanKdAndTd\\" + new Date().getTime() + ".pdf");
                        fullNameFile=preferencesScanKdAndTd.get("textFieldFolderScan", System.getProperty("user.home"))+"\\"+row.getNameFile();
                    } else {
                        if (!Paths.get(System.getProperty("user.home") + "/ScanKdAndTd/").toFile().exists())
                            Paths.get(System.getProperty("user.home") + "/ScanKdAndTd/").toFile().mkdirs();//создаю свою директорию,если ее нету для UNIX
                        pathDestination = Paths.get(System.getProperty("user.home") + "/ScanKdAndTd/" + new Date().getTime() + ".pdf");//для Unix систем
                        fullNameFile=preferencesScanKdAndTd.get("textFieldFolderScan", System.getProperty("user.home"))+"/"+row.getNameFile();
                    }
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
                        //pagination.getScene().getRoot().setDisable(true);
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
            String designOld=returnDesignation(textFieldDesignation);
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
        String design=returnDesignation(textFieldDesignation);
        int baseDocId = S4App.getDocID_ByDesignation(S4App,design);
        if (baseDocId>0)
            AlertUtilNew.message("Внимание!", "Такой документ уже есть в архиве.ID документа = " +baseDocId /*S4App.queryFieldByName(S4App,"doc_id")*/, "Сообщение для ознакомления!", Alert.AlertType.WARNING);
        else
            AlertUtilNew.message("Оповещение.", "Документа с таким обозначением нет в Search.", "Информационное сообщение.", Alert.AlertType.INFORMATION);
    }

    /**Проверка заполнения обозначения*/
    private boolean checkDesignationInSearchAndAddVersion(String str){
        boolean flag;
        if (strZakazDop.equals(str)){
            flag=(textFieldDesignation.getText()+" Дополнение №"+textFieldDopNumberPdfView.getText()).length()>=str.length();
        }else if (str.equals(defaultTextComboBoxMask)){
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
            if (!comboBoxMaskPdfViewController.getSelectionModel().getSelectedItem().equals(defaultTextComboBoxMask)) {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                final String str = newValue.replace(oldValue, "");
                if (str.equals(clipboard.getString())) {
                    textFieldDesignation.setText(oldValue);
                    clipboard.clear();
                }
            }
            /**Вешаю слушателя на TextFieldDesignation для разблокировки кнопок проверить и список версий*/
            if (!comboBoxMaskPdfViewController.getSelectionModel().getSelectedItem().equals(strZakazDop)){
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
            if (newValue!=null && !defaultTextComboBoxMask.equals(newValue)) {
                textFieldDesignation.setTextFormatter(TextFormatterUtil.gTextFormatter(newValue, textFieldDesignation));
                tooltipDesignationPdfView.setText("Ввод только по выбранной маске.");
            }else {
                textFieldDesignation.setTextFormatter(null);
                textFieldDesignation.clear();
                tooltipDesignationPdfView.setText("Допускается ввод любого обозначения.");
            }
            if (newValue!=null && newValue.equals(strZakazDop)){
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
            if (newValue) {
                comboBoxFormatPdfViewController.setEditable(newValue);
            } else {
                comboBoxFormatPdfViewController.setEditable(newValue);
                comboBoxFormatPdfViewController.getSelectionModel().select(0);
            }
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
            comboBox.getItems().add(defaultTextComboBoxMask);
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
        //createAndConfigureFileChooser();
        //Запускаю в других потоках начальную загрузку из реестра
        //executorServiceLoad.submit(this::containComboBoxMask);
        //executorServiceLoad.submit(this::containComboBoxDocTypes);
        //executorServiceLoad.submit(this::containComboBoxFormats);
        //executorServiceLoad.submit(this::addListenerTextFieldDesignation);
        //executorServiceLoad.submit(this::addListenerCheckBoxArbitrarily);
        //executorServiceLoad.submit(this::addListenerComboBoxMaskPdfView);

        currentFile = new SimpleObjectProperty<>();
        //updateWindowTitleWhenFileChanges();

        currentImage = new SimpleObjectProperty<>();
        scroller.contentProperty().bind(currentImage);

        zoom = new SimpleDoubleProperty(1);
        // To implement zooming, we just get a new image from the PDFFile each time.
        // This seems to perform well in some basic tests but may need to be improved
        // E.g. load a larger image and scale in the ImageView, loading a new image only
        // when required.
        zoom.addListener((observable, oldValue, newValue) -> updateImage(pagination.getCurrentPageIndex(), DEGREE));
        labelCurrentZoom.textProperty().bind(Bindings.format("%.0f %%", zoom.multiply(100)));
        //невидимость Табов на форме
        /**disableFormPdfView();*/

        /**Чтение из БД Серча причин изменения из таблицы rreasons*/
        Platform.runLater(() -> containComboBoxChangeReason());
        Platform.runLater(() -> containComboBoxMask());
        Platform.runLater(() -> containComboBoxDocTypes());
        Platform.runLater(() -> containComboBoxFormats());
        Platform.runLater(() -> addListenerTextFieldDesignation());
        Platform.runLater(() -> addListenerCheckBoxArbitrarily());
        Platform.runLater(() -> addListenerComboBoxMaskPdfView());
        Platform.runLater(() -> addListenerTextFieldDopNumberPdfView());
        Platform.runLater(() -> addListenerTableViewList());
        //Platform.runLater(() -> checkNameOfDesignation());
        bindPaginationToCurrentFile();
        createPaginationPageFactory();
        //test2.selectedProperty().set(true);
        disableFormPdfView();
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
        // TODO: the -20 is a kludge to account for the width of the scrollbars, if showing.
        double horizZoom = (scroller.getWidth() - 20) / currentPageDimensions.width;
        double verticalZoom = (scroller.getHeight() - 20) / currentPageDimensions.height;
        zoom.set(Math.min(horizZoom, verticalZoom));
    }
    /**Максимально по высоте*/
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
                // draw image to buffered image:
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
}
