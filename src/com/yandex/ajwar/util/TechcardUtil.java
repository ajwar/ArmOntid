package com.yandex.ajwar.util;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import com.jacob.com.Dispatch;
import org.apache.log4j.Logger;

/**
 * Created by 53189 on 14.02.2017.
 */
public class TechcardUtil extends ActiveXComponent{
    private static final Logger log=Logger.getLogger(TechcardUtil.class);

    public TechcardUtil(String programId) {
        super(programId);
    }

    //константы
    public int getVersion(ActiveXComponent tech){return tech.getPropertyAsInt("Version");}

    //методы
    public int loaded(ActiveXComponent tech){
        return Dispatch.call(tech,"Loaded").getInt();
    }
    public int ready(ActiveXComponent tech){
        return Dispatch.call(tech,"Ready").getInt();
    }
    public int isCanCreateTP(ActiveXComponent tech){return Dispatch.call(tech,"IsCanCreateTP").getInt();}

    public static TechcardUtil getInstance(){
        TechcardUtil techcard=new TechcardUtil("TPServer.TApplication");
        int i=0;
        while (true){
            try {
                if (techcard.ready(techcard)==1) break;
                Thread.currentThread().sleep(100);
                if (++i>600) {
                    log.error("Не удалось загрузить сервер Techcard");
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return techcard;
    }
    public static TechcardUtil returnAndCreateThreadtechcard(){
        ComThread.InitMTA(true);
        TechcardUtil techcardUtil=TechcardUtil.getInstance();
        return techcardUtil;
    }
    public static void closeThreadTechcard(){
        ComThread.Release();
        ComThread.quitMainSTA();
    }
}
