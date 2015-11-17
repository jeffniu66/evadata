package cn.oge.sci.evadata;

import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Test;

import cn.oge.kdm.rdp.center.evaluation.dto.EvaluationDataDTO;
import cn.oge.kdm.service.dto.KKSEva;
import cn.oge.sci.JEvadataMain;
import cn.oge.sci.JEvadataMainRest;
import cn.oge.sci.util.RestDataloader;

public class RestTest {

	@Test
	public void test1() {
		String host = "192.168.1.147";
		int restPort = 8082;
		int dubboPort = 20883;
		for (int i = 0; i < 1000; i++) {
			JEvadataMainRest.calc(host, restPort, dubboPort, 1);
			System.out.println("-------" + (i + 1));

			// System.out.println("evadata=============" +
			// JSONArray.fromObject(evadata));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void test2() {
		String host = "10.1.128.222";
		int restPort = 8080;
		int dubboPort = 20884;
		for (int i = 0; i < 1000; i++) {
			JEvadataMainRest.calc(host, restPort, dubboPort, 1);
			System.out.println("-------" + (i + 1));

			// System.out.println("evadata=============" +
			// JSONArray.fromObject(evadata));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testGetKKSEva() {
		String host = "211.154.164.67";
		int port = 5080;

		for (int i = 0; i < 1000; i++) {
			List<KKSEva> evaList = RestDataloader.getEvaList(host, port);
			System.out.println("-------" + (i + 1));

			System.out.println("evadata=============" + JSONArray.fromObject(evaList));
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testStateInfo() {
		StatInfo statInfo = JEvadataMain.calc();
		System.out.println(statInfo);
	}

	@Test
	public void testDateToJson() {
		EvaluationDataDTO evaData = new EvaluationDataDTO();
		Date time = new Date();
		evaData.setTime(time);
		System.out.println(JSONObject.fromObject(evaData));
	}
}
