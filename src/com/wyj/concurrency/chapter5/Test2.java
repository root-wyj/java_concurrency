package com.wyj.concurrency.chapter5;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * FutureTask
 * @author wuyingjie
 * @date 2018年2月23日
 */

public class Test2 {
	private final FutureTask<Integer> future = new FutureTask<>(
			new Callable<Integer>() {
		public Integer call() throws Exception {
			System.out.println("futureTask:"+Thread.currentThread().getName());
			Thread.sleep(5000);
			return 6;
		};
	});
	
	private final Thread thread = new Thread(future);
	
	//在构造函数或者静态初始化方法中启动线程 是非常不明智的
	public void start() {thread.start();}
	
	public int get() throws InterruptedException{
		try {
			return future.get();
		} catch (ExecutionException e) {
			Throwable t = e.getCause();
			
			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			} else if (t instanceof Error) {
				throw (Error)t;
			} else {
				throw new IllegalStateException("bu dui", t);
			}
			
		}
	}
	
	public static void main(String[] args) throws InterruptedException {
		Test2 t = new Test2();
		t.start();
		System.out.println("start");
		System.out.println(t.get());
	}
}
