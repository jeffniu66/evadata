package cn.oge.sci.data.kdm;

import java.util.List;

import net.sf.json.JSONArray;

import org.junit.Test;

import cn.oge.kdm.service.dto.RTDataSet;
import cn.oge.sci.data.DataLoader;
import cn.oge.sci.data.kdm.KdmDubboApi;

public class TestKdmDubboApi {
	private static String dubboUrl = "dubbo://211.154.164.67:20883";
	private static String KKS = "EB001HP0MKC01MK012BL01J1CC001AA06";// 上导轴承X向摆度

	@Test
	public void test1() {
		DataLoader dataLoader = new KdmDubboApi(dubboUrl);
		try {
			List<RTDataSet> snapshot = dataLoader.getRTDataSnapshot(KKS);
			System.out.println(JSONArray.fromObject(snapshot));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test2() {
		DataLoader dataLoader = new KdmDubboApi("dubbo://192.168.1.121:20883");
		try {
			List<RTDataSet> snapshot = dataLoader.getRTDataSnapshot(KKS);
			System.out.println(JSONArray.fromObject(snapshot));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
