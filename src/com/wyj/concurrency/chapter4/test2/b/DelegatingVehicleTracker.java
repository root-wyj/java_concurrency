package com.wyj.concurrency.chapter4.test2.b;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 使用委托的线程安全
 * @author wuyingjie
 * @date 2018年2月12日
 */

public class DelegatingVehicleTracker {
	private final ConcurrentMap<String, Point> locations;
	
	public DelegatingVehicleTracker(Map<String, Point> locations) {
		this.locations = new ConcurrentHashMap<>(locations);
	}
	
	public Map<String, Point> getLocations() {
		return Collections.unmodifiableMap(new HashMap<>(locations));
	}
	
//	public Map<String, Point> getLocations() {
//		return Collections.unmodifiableMap(locations);
//	}
	
	public Point getLocation(String id) {
		return locations.get(id);
	}
	
	public void setLocation(String id, int x, int y) {
		locations.put(id, new Point(x, y));
	}
	
}
