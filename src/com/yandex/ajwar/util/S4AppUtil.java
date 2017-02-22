package com.yandex.ajwar.util;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

/**
 * Created by 53189 on 16.11.2016.
 */
public class S4AppUtil extends ActiveXComponent{
    private static volatile S4AppUtil instance;
    private String errorMessage;
    private int errorCode;
    private int quietMode;
    public S4AppUtil(){
        this(null);
    }

    public S4AppUtil(String programId) {
        super(programId);
    }

    public static S4AppUtil getInstance(){
        //ActiveXComponent test=new ActiveXComponent("Excel.Application");
        //ActiveXComponent e=new ActiveXComponent(e.getProperty("ActiveSheet").toDispatch());
        //ComThread.InitMTA(true);
        S4AppUtil localInstance=instance;
        if (localInstance==null){
            synchronized (S4AppUtil.class){
                localInstance=instance;
                if (localInstance==null){
                        instance=localInstance=new S4AppUtil("S4.TS4App");
                }
            }
        }
        return localInstance ;
    }

    //Переменные
    public int getQuietMode(ActiveXComponent S4App){
        return S4App.getPropertyAsInt("QuietMode");
    }
    public int getErrorCode(ActiveXComponent S4App) {
        return S4App.getPropertyAsInt("ErrorCode");
    }
    public String getErrorMessage(ActiveXComponent S4App) {
        return S4App.getPropertyAsString("ErrorMessage");
    }
    public void setQuietMode(ActiveXComponent S4App,int value){
        S4App.setProperty("QuietMode",value);
    }
    public void setErrorCode(ActiveXComponent S4App,int value) {
        S4App.setProperty("ErrorCode",value);
    }
    public void setErrorMessage(ActiveXComponent S4App,String message) {
        S4App.setProperty("ErrorMessage",message);
    }

    //дополнительные функции и процедуры
    public static S4AppUtil returnAndCreateThreadS4App(){
        ComThread.InitMTA(true);
        S4AppUtil S4App=new S4AppUtil("S4.TS4App");
        S4App.login(S4App);
        return S4App;
    }
    public static void closeThreadS4App(){
        ComThread.Release();
        ComThread.quitMainSTA();
    }
    //Функции и процедуры Search
    public int getUserID(ActiveXComponent S4App){
        return Dispatch.call(S4App,"GetUserID").getInt();
    }
    public void openArticle(ActiveXComponent S4App,long artId){
        Dispatch.call(S4App,"OpenArticle",artId);
    }
    public void closeArticle(ActiveXComponent S4App){
        Dispatch.call(S4App,"CloseArticle");
    }
    public String getArticleDesignation(ActiveXComponent S4App){
        return Dispatch.call(S4App,"GetArticleDesignation").getString();
    }
    public String getArticleName(ActiveXComponent S4App){
        return Dispatch.call(S4App,"GetArticleName").getString();
    }
    public int messageBox(ActiveXComponent S4App,String text,String title, int format){
        return Dispatch.call(S4App,"MessageBox",text,title,format).getInt();
    }
    public int createFileDocumentWithDocType(ActiveXComponent S4App,String fileFullName,long docType,long archiveID,String designation,String docName,long sectionID,long registerInCancMode,boolean registerAsCancProject){
        return Dispatch.call(S4App,"CreateFileDocumentWithDocType3",fileFullName,docType,archiveID,designation,docName,sectionID,registerInCancMode,registerAsCancProject).getInt();
    }
    public int createFileDocumentWithDocType(ActiveXComponent S4App,String fileFullName,long docType,long archiveID,String designation,String docName,long sectionID){
        return Dispatch.call(S4App,"CreateFileDocumentWithDocType2",fileFullName,docType,archiveID,designation,docName,sectionID).getInt();
    }
    public int createFileDocumentWithDocType(ActiveXComponent S4App,String fileFullName,long docType,long archiveID,String designation,String docName){
        return Dispatch.call(S4App,"CreateFileDocumentWithDocType1",fileFullName,docType,archiveID,designation,docName).getInt();
    }
    public int createFileDocumentWithDocType(ActiveXComponent S4App,String fileFullName,long docType){
        return Dispatch.call(S4App,"CreateFileDocumentWithDocType",fileFullName,docType).getInt();
    }
    public int createDocVersion(ActiveXComponent S4App,long baseDocId,long baseVersionId,String versionCode,String versionNote,String versionFileName,long reasonCode,long makeActual){
        return Dispatch.call(S4App,"CreateDocVersion",baseDocId,baseVersionId,versionCode,versionNote,versionFileName,reasonCode,makeActual).getInt();
    }
    public int createDocVersion(ActiveXComponent S4App,long baseDocId,long baseVersionId,String versionCode,String versionNote,String versionFileName,long reasonCode,long makeActual,long versionState,String otdRegNum,String prevOtdRegNum,String dublOtdRegNum){
        return Dispatch.call(S4App,"CreateDocVersion2",baseDocId,baseVersionId,versionCode,versionNote,versionFileName,reasonCode,makeActual,versionState,otdRegNum,prevOtdRegNum,prevOtdRegNum,dublOtdRegNum).getInt();
    }
    public int login(ActiveXComponent S4App){
        return Dispatch.call(S4App,"Login").getInt();
    }
    public int isLoggedIn(ActiveXComponent S4App){
        return Dispatch.call(S4App,"IsLoggedIn").getInt();
    }
    public void openQuery(ActiveXComponent S4App,String sql){
        //errorMessage2.toDispatch();
        //ComThread.InitSTA(true);
        Dispatch.call(S4App,"OpenQuery",sql);
    }
    public void closeQuery(ActiveXComponent S4App){
        Dispatch.call(S4App,"CloseQuery");
    }
    public int queryGoNext(ActiveXComponent S4App){
        return Dispatch.call(S4App,"QueryGoNext").getInt();
    }
    public void queryGoFirst(ActiveXComponent S4App){
        Dispatch.call(S4App,"QueryGoFirst");
    }
    public int queryEOF(ActiveXComponent S4App){
        return Dispatch.call(S4App,"QueryEOF").getInt();
    }
    public int queryRecordCount(ActiveXComponent S4App){return Dispatch.call(S4App,"QueryRecordCount").getInt();}
    public String queryFieldByName(ActiveXComponent S4App,String fieldName){
        return Dispatch.call(S4App,"QueryFieldByName",fieldName).getString();
    }
    public void openDocument(ActiveXComponent S4App,long id){
        Dispatch.call(S4App,"OpenDocument",id);
    }
    public void closeDocument(ActiveXComponent S4App){
        Dispatch.call(S4App,"CloseDocument");
    }
    public void openDocVersion(ActiveXComponent S4App,long id,long versionId){
        Dispatch.call(S4App,"OpenDocVersion",id,versionId);
    }
    public void checkIn(ActiveXComponent S4App){
        Dispatch.call(S4App,"CheckIn");
    }
    public void checkIn(ActiveXComponent S4App,long actualizeNewVersions){
        Dispatch.call(S4App,"CheckIn2",actualizeNewVersions);
    }
    public void checkOut(ActiveXComponent S4App){
        Dispatch.call(S4App,"CheckOut");
    }
    public void saveChanges(ActiveXComponent S4App){
        Dispatch.call(S4App,"SaveChanges");
    }
    public void saveWorkCopy(ActiveXComponent S4App,long id){
        Dispatch.call(S4App,"SaveWorkCopy",id);
    }
    public int chooseArchiveName(ActiveXComponent S4App){
       return Dispatch.call(S4App,"ChooseArchiveName").getInt();
    }
    public void showVersionList(ActiveXComponent S4App){
        Dispatch.call(S4App,"ShowVersionList");
    }
    public int showVersionList2(ActiveXComponent S4App){
        return Dispatch.call(S4App,"ShowVersionList2").getInt();
    }
    public void showVersionsList(ActiveXComponent S4App,long id){
        Dispatch.call(S4App,"ShowVersionsList",id);
    }
    public int createOTDCopy(ActiveXComponent S4App,long id,long versionId){
        return Dispatch.call(S4App,"CreateOTDCopy",id,versionId).getInt();
    }
    public void closeOTDCopies(ActiveXComponent S4App){
        Dispatch.call(S4App,"CloseOTDCopies");
    }
    public void openOTDCopies(ActiveXComponent S4App,long id,long versionId){
        Dispatch.call(S4App,"OpenOTDCopies",id,versionId);
    }
    public String getDocBasicFields(ActiveXComponent S4App) {
        return Dispatch.call(S4App,"GetDocBasicFields").getString();
    }
    public String getDocNameInMessages(ActiveXComponent S4App) {
        return Dispatch.call(S4App,"GetDocNameInMessages").getString();
    }
    public int fieldExists(ActiveXComponent S4App,String fieldName){
        return Dispatch.call(S4App,"FieldExists",fieldName).getInt();
    }
    public String getFieldValue(ActiveXComponent S4App,String fieldName){
        return Dispatch.call(S4App,"GetFieldValue",fieldName).getString();
    }
    public String getFieldValue_DocVersion(ActiveXComponent S4App,String fieldName){
        return Dispatch.call(S4App,"GetFieldValue_DocVersion",fieldName).getString();
    }
    public void setFieldValue(ActiveXComponent S4App,String fieldName,String newValueField){
        Dispatch.call(S4App,"SetFieldValue",fieldName,newValueField);
    }
    public void setFieldValue_DocVersion(ActiveXComponent S4App,String fieldName,String newValueField){
        Dispatch.call(S4App,"SetFieldValue_DocVersion",fieldName,newValueField);
    }
    public int getDocActualVersionID(ActiveXComponent S4App){
        return Dispatch.call(S4App,"GetDocActualVersionID").getInt();
    }
    public int getDocVersionID(ActiveXComponent S4App){
        return Dispatch.call(S4App,"GetDocVersionID").getInt();
    }
    public int getDocMaxVersionID(ActiveXComponent S4App,long id){
        return Dispatch.call(S4App,"GetDocMaxVersionID",id).getInt();
    }
    public String getDocFilename(ActiveXComponent S4App,long id){
        return Dispatch.call(S4App,"GetDocFilename",id).getString();
    }
    public int getDocID_ByDesignation(ActiveXComponent S4App,String designation){
        return Dispatch.call(S4App,"GetDocID_ByDesignation",designation).getInt();
    }
    public int getDocStatus(ActiveXComponent S4App){
        return Dispatch.call(S4App,"GetDocStatus").getInt();
    }
    public int getGroupIDForUser(ActiveXComponent S4App,long id){
        return Dispatch.call(S4App,"GetGroupIDForUser",id).getInt();
    }
    public String getUserLoginName_ByUserID(ActiveXComponent S4App,long id){
        return Dispatch.call(S4App,"GetUserLoginName_ByUserID",id).getString();
    }
    public String nfoGetUserFullNameByUserID(ActiveXComponent S4App,long userId){
        return Dispatch.call(S4App,"nfoGetUserFullNameByUserID",userId).getString();
    }
    public void startSelectArticles(ActiveXComponent S4App){
        Dispatch.call(S4App,"StartSelectArticles");
    }
    public void selectArticles(ActiveXComponent S4App){
        Dispatch.call(S4App,"SelectArticles");
    }
    public void endSelectArticles(ActiveXComponent S4App){
        Dispatch.call(S4App,"EndSelectArticles");
    }
    public int selectedArticlesCount(ActiveXComponent S4App){
        return Dispatch.call(S4App,"SelectedArticlesCount").getInt();
    }
    public void showSearch(ActiveXComponent S4App){
        Dispatch.call(S4App,"ShowSearch");
    }
    public void hideSearch(ActiveXComponent S4App){
        Dispatch.call(S4App,"HideSearch");
    }
    public void selectArticlesBySectID(ActiveXComponent S4App,long sectId,long locateOnArticle){
        Dispatch.call(S4App,"SelectArticlesBySectID",sectId,locateOnArticle);
    }
    public void selectArticlesSample(ActiveXComponent S4App,long artKind,String sampleName,String SQLText,String orderBy){
        Dispatch.call(S4App,"SelectArticlesSample",artKind,sampleName,SQLText,orderBy);
    }
    public int getSelectedArticleID(ActiveXComponent S4App,long num){
        return Dispatch.call(S4App,"GetSelectedArticleID",num).getInt();
    }
    public int linkDocToArticle(ActiveXComponent S4App,long artId,long docId,long linkType,long linkToIsp){
        return Dispatch.call(S4App,"LinkDocToArticle",artId,docId,linkType,linkToIsp).getInt();
    }
    public int linkDocToArticle(ActiveXComponent S4App,long artId,long artVerId,long docId,long linkType,long linkToIsp){
        return Dispatch.call(S4App,"LinkDocToArticle",artId,artVerId,docId,linkType,linkToIsp).getInt();
    }
    public int linkDocVersionToArticleVersion(ActiveXComponent S4App,long artId,long artVerId,long docId,long docVersionId,long linkType,long linkToIsp){
        return Dispatch.call(S4App,"LinkDocVersionToArticleVersion",artId,artVerId,docId,docVersionId,linkType,linkToIsp).getInt();
    }
    public int getArtID_ByDesignation(ActiveXComponent S4App,String designation){
        return Dispatch.call(S4App,"GetArtID_ByDesignation",designation).getInt();
    }
    public boolean getImageFromScanner(ActiveXComponent S4App,String fileName,long imagesToScan,long colorFormat,long resolution,long paperSize,boolean selectSource,boolean enableADF,boolean duplex,boolean showForm){
        return Dispatch.call(S4App,"GetImageFromScanner",fileName,imagesToScan,colorFormat,resolution,paperSize,selectSource,enableADF,duplex,showForm).getBoolean();
    }
}
