package com.yandex.ajwar.util;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import com.yandex.ajwar.MainApp;
import javafx.scene.control.Alert;
import org.apache.log4j.Logger;

/**
 * Created by 53189 on 09.02.2017.
 */
public class ImbaseUtil extends ActiveXComponent{
    private static final Logger log=Logger.getLogger(ImbaseUtil.class);
    public ImbaseUtil(String programId) {
        super(programId);
    }

    public int ready(ActiveXComponent im){
        return Dispatch.call(im,"Ready").getInt();
    }
    public void showKeysInfo(ActiveXComponent im,String keys){
        Dispatch.call(im,"ShowKeysInfo",keys);
    }
    public int selectCatalog(ActiveXComponent im,String prompt){
        return Dispatch.call(im,"SelectCatalog",prompt).getInt();
    }
    public int selectFolder(ActiveXComponent im,long catalogId,String startPath,String caption,String path,String catalogRec,String folderRec,String keysList){
        return Dispatch.call(im,"SelectFolder",catalogId,startPath,caption,path,catalogRec,folderRec,keysList).getInt();
    }
    public void openQuery(ActiveXComponent im,String query){
        Dispatch.call(im,"OpenQuery",query);
    }
    public String openQuery2(ActiveXComponent im,String query){
        return Dispatch.call(im,"OpenQuery2",query).getString();
    }
    public void openQuery3(ActiveXComponent im,String query){
        Dispatch.call(im,"OpenQuery3",query);
    }

    public static ImbaseUtil getInstance(){
        ImbaseUtil imbase=new ImbaseUtil("Imbase.ImDataBase");
        int i=0;
        while (true){
            try {
                if (imbase.ready(imbase)==1) break;
                Thread.currentThread().sleep(100);
                if (++i>600) {
                    log.error("Не удалось загрузить сервер Imbase");
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return imbase;
    }
    public static ImbaseUtil returnAndCreateThreadImbase(){
        ComThread.InitMTA(true);
        ImbaseUtil im=ImbaseUtil.getInstance();
        return im;
    }
    public static void closeThreadImbase(){
        ComThread.Release();
        ComThread.quitMainSTA();
    }
}
