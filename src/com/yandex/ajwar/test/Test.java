package com.yandex.ajwar.test;

import com.yandex.ajwar.util.S4AppUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 53189 on 04.01.2017.
 */
public class Test {
    public static void main(String[] args) {
        List<String> list=new ArrayList<>();
        S4AppUtil s4AppUtil=S4AppUtil.getInstance();
        s4AppUtil.login(s4AppUtil);
        int x;
        long b=System.currentTimeMillis();
        s4AppUtil.openQuery(s4AppUtil,"select designatio from doclist where doc_type=\"1000017\" and designatio like '%СП%'" );
        for (s4AppUtil.queryGoFirst(s4AppUtil); s4AppUtil.queryEOF(s4AppUtil) == 0; s4AppUtil.queryGoNext(s4AppUtil)) {
            String str=s4AppUtil.queryFieldByName(s4AppUtil,"designatio");
            list.add(str.substring(0,str.length()-2).trim());
            //System.out.println(str.substring(0,str.length()-2).trim()+"_");
        }
        //System.out.println(s4AppUtil.queryFieldByName(s4AppUtil,"designatio"));
        //System.out.println(s4AppUtil.queryFieldByName(s4AppUtil,"dt_code"));
        x=s4AppUtil.queryRecordCount(s4AppUtil);
        System.out.println(x+"  Количество всех Сканированных спецификаций");
        s4AppUtil.closeQuery(s4AppUtil);
        int sum=0;
        for (int i = 0; i <list.size() ; i++) {
            //System.out.println("select doc_id from doclist where designatio=\""+list.get(i)+"\" and doc_type=\"1\"");
            s4AppUtil.openQuery(s4AppUtil,"select doc_id from doclist where designatio=\""+list.get(i)+"\" and doc_type=\"1\"");
            //System.out.println(s4AppUtil.queryFieldByName(s4AppUtil,"doc_id")+"         "+list.get(i));
            if (!s4AppUtil.queryFieldByName(s4AppUtil,"doc_id").isEmpty()) sum++;
        }
        System.out.println(sum+"    Количество совпадений разных СП");
        System.out.println(x-sum+"    разница");
        System.out.println((System.currentTimeMillis()-b)+" Столько времени заняла проверка");
        s4AppUtil.closeQuery(s4AppUtil);
    }
}
