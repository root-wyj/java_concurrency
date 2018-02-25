package com.wyj.concurrency.chapter5;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created
 * Author: wyj
 * Email: 18346668711@163.com
 * Date: 2018/2/25
 */
public class Test1 {

    public static void main(String[] args) {
        test1_1();

    }

    private static void test1_pre(Vector<Integer> list) {
        for (int i=0; i<100; i++) {
            list.add(i);
        }
    }

    public static void test1_1() {
        Vector<Integer> list = new Vector<>();
        test1_pre(list);



        for (int i : list) {
            System.out.println(i);
            list.add(i);
        }

        System.out.println(list);
    }

}
