package cn.oge.sci.huanange.shangdu;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;

import org.junit.Test;

import cn.oge.kdm.service.dto.RTDataSet;
import cn.oge.sci.util.RestDataloader;

public class GetData {

	//private static String host = "211.154.164.67";
	private static String host = "10.1.128.222";
	//private static int port = 5080;
	private static int port = 8080;
	//public static String KKS = "EB001HP0MKC01MK012BL01J1CC001AA06";// 上导轴承X向摆度
	public static String KKS = "AE587FP1HFK06HF602ZZ01J0AC001AA05";// 磨煤机F出口风粉混合物温度2
	//public static String KKS = "AE587FP2HND01HN524ZZ00M0DC002AA05";// A引风机机械侧评价量

	@Test
	public void testGetHist() {
		long nowTime = new Date().getTime();
		long interval = 1000 * 60 * 3;// 1000 * 60 = 1分钟

		// 前3分钟的数据
		long endTime2 = nowTime;
		long startTime2 = endTime2 - interval;
		getHist(KKS, startTime2, endTime2, "target/data2.json");

		// 前6-3分钟的数据
		long endTime1 = startTime2;
		long startTime1 = endTime1 - interval;
		getHist(KKS, startTime1, endTime1, "target/data1.json");
	}
	
	@Test
	public void testGetHist4() {
		long nowTime = new Date().getTime();
		long interval = 1000 * 60 * 60 * 24;// 24小时

		// 前24小时的数据
		long endTime2 = nowTime;
		long startTime2 = endTime2 - interval;
		getHist(KKS, startTime2, endTime2, "target/data2.json");

		// 前48-24小时的数据
		long endTime1 = startTime2;
		long startTime1 = endTime1 - interval;
		getHist(KKS, startTime1, endTime1, "target/data1.json");
	}

	/**
	 * 多个KKS
	 */
	@Test
	public void testGetHist2() {
		long nowTime = new Date().getTime();
		long interval = 1000 * 60 * 3;// 1000 * 60 = 1分钟

		// 前3分钟的数据
		long endTime = nowTime;
		long startTime = endTime - interval;
		String kks = "EB001HP0MKC01MK012BL01J1CC001BB02";// 上导轴承X向摆度低通峰峰值
		kks += "," + "EB001HP0MKC01MK012BL01J1CC001BB01";// 上导轴承X向摆度高通峰峰值
		getHist(kks, startTime, endTime, "target/data-multikks.json");
	}

	/**
	 * 主要用来查看500个kks，请求1分钟时获取的数据包大小情况<br/>
	 * 1.2m左右
	 */
	@Test
	public void testGetHist3() {
		long nowTime = new Date().getTime();
		long interval = 1000 * 60 * 1;// 1000 * 60 = 1分钟

		// 前1分钟的数据
		long endTime = nowTime;
		long startTime = endTime - interval;
		StringBuffer sbKks = new StringBuffer();
		for (int i = 0; i < 500; i++) {
			sbKks.append(",").append(KKS);
		}
		// 500个KKS(这里是相同的KKS，测试效果一样)
		getHistPost(sbKks.substring(1), startTime, endTime, "target/data-500.json");
	}

	public void getHist(String kks, long startTime, long endTime, String filePath) {

		List<RTDataSet> rtdsList = RestDataloader.getRTDataHistory(kks, startTime, endTime, host, port);
		JSONArray jsonArray = JSONArray.fromObject(rtdsList);
		System.out.println(JSONArray.fromObject(jsonArray));
		// 保存为json数据
		try {
			FileWriter file = new FileWriter(filePath);
			jsonArray.write(file);
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void getHistPost(String kks, long startTime, long endTime, String filePath) {

		List<RTDataSet> rtdsList = RestDataloader.getRTDataPost(kks, startTime, endTime, host, port,
				"getRTDataHistory", 2);
		JSONArray jsonArray = JSONArray.fromObject(rtdsList);
		System.out.println(JSONArray.fromObject(jsonArray));
		// 保存为json数据
		try {
			FileWriter file = new FileWriter(filePath);
			jsonArray.write(file);
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
