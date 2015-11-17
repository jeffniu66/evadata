package cn.oge.sci.huanange.shangdu;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.oge.rtdatanalysis.RtDataToBeAnalyzed;
import com.oge.rtdatanalysis.SingleVariableConvergentAnalysis;
import com.oge.rtdsp.response.TimeValue;

public class ConvergentTest {

	private static double[] datas = { 173.2688, 173.2688, 166.1124, 171.8733, 171.8733, 189.6847, 169.6412, 181.4102,
			181.4102, 180.3303, 167.7969, 162.922, 176.2006, 172.9418, 189.5554, 189.5554, 185.6083, 185.6083,
			161.4504, 166.8044, 173.1814, 167.0934, 171.3105, 180.8931, 163.9563, 163.1387, 163.1387, 192.2856,
			177.5544, 166.2074, 160.8914, 169.3902, 189.4185, 177.3034, 177.3034, 172.9532, 163.3364, 163.3364,
			175.3869, 168.1087, 167.1999, 173.0445, 190.0915, 166.7284, 183.0681, 173.4019, 168.0403, 180.0261,
			181.9958, 167.4204, 168.8198, 168.8198, 181.6764, 165.8804, 171.2801, 167.2189, 167.2189, 169.3636,
			178.8815, 172.8696, 179.4671, 185.2508, 170.7325, 195.4418, 195.4418, 189.1067, 164.5419, 173.3335,
			176.2577, 165.9032, 185.5779, 169.3027, 164.6065, 174.2841, 169.0442, 161.9751, 176.0638, 176.7178,
			170.2914, 154.0467, 180.9729, 172.611, 166.6523, 174.7404, 174.9686, 179.3112, 179.3112, 157.9254,
			182.5434, 169.2647, 159.1498, 159.1498, 182.8514, 161.3515, 169.1658, 169.1658, 179.1096, 159.5947,
			172.3904, 168.7285, 163.6749, 165.4545, 173.2232, 175.4211, 168.5156, 165.5876, 174.3678, 167.8121,
			170.8694, 170.8694, 178.3757, 172.1281, 166.0249, 166.0249, 176.0485, 164.7358, 191.5593, 191.5593,
			166.196, 166.196, 178.3149, 166.8196, 166.8196, 181.0223, 176.1056, 170.6755, 170.0176, 177.9803, 174.7252,
			167.4166, 180.1706, 180.1706, 174.8887, 173.5883, 174.7519, 178.3567, 166.5839, 177.87, 178.254, 159.762,
			159.762, 181.2657, 173.0141, 159.3057, 154.2406, 154.2406, 172.2345, 184.5169, 165.5191, 170.4055,
			170.4055, 167.6942, 170.8808, 178.0259, 179.6002, 179.6002, 170.0252, 165.4279, 173.3107, 173.3107,
			182.8438, 165.3138, 171.2687, 193.1716, 161.1348, 171.4284, 171.4284, 181.8285, 180.798, 165.002, 182.8096,
			174.9154, 189.3615, 170.5728, 189.8596, 189.8596, 172.6756, 171.1052, 171.1052, 161.7888 };

	@Test
	public void test1() {

		// =================================
		// 构造数据
		List<TimeValue> tvList = new ArrayList<TimeValue>();
		long time = 1438399614000l;
		for (int i = 0; i < datas.length; i++) {
			TimeValue tv = new TimeValue();
			time = time + 1000;
			//tv.Time = new Date(time + i * 1000);
			tv.Time = new Date(time);
			tv.Value = datas[i];
			tvList.add(tv);
		}
		// ---------------------------------

		SingleVariableConvergentAnalysis analyst = new SingleVariableConvergentAnalysis();
		RtDataToBeAnalyzed testdata = new RtDataToBeAnalyzed(GetData.KKS);

		testdata.fragmentSize = 50;
		testdata.exceptionRate = 0.06F; // 0.05
		testdata.tolerance = 2.0F; // 2
		testdata.absDifference = 5;
		testdata.vaRatio = 0.05F;

		testdata.addData(tvList);
		analyst.setDataToBeAnalyzed(testdata);

		analyst.analyzeData();
		testdata.printConvergentIntervals();
	}

	@Test
	public void test2() {

		// =================================
		// 构造数据
		List<TimeValue> tvList = new ArrayList<TimeValue>();
		long time = 1438399614000l;
		for (int i = 0; i < datas.length; i++) {
			TimeValue tv = new TimeValue();
			tv.Time = new Date(time + i * 1000);
			tv.Value = datas[i];
			tvList.add(tv);
		}
		// ---------------------------------

		SingleVariableConvergentAnalysis analyst = new SingleVariableConvergentAnalysis();
		RtDataToBeAnalyzed testdata = new RtDataToBeAnalyzed(GetData.KKS);

		testdata.fragmentSize = 50;
		testdata.exceptionRate = 0.06F; // 0.05
		testdata.tolerance = 2.0F; // 2
		testdata.absDifference = 5;
		testdata.vaRatio = 0.05F;

		testdata.addData(tvList.subList(0, 60));
		analyst.setDataToBeAnalyzed(testdata);
		analyst.analyzeData();
		testdata.printConvergentIntervals();

		testdata.addData(tvList.subList(60, 120));
		analyst.setDataToBeAnalyzed(testdata);
		analyst.analyzeData();
		testdata.printConvergentIntervals();
		int recordCnt = testdata.getCurrentRecordCnt();
		if (recordCnt > 100) {
			testdata.deleteData(100);
		}

		testdata.addData(tvList.subList(120, 180));
		analyst.setDataToBeAnalyzed(testdata);
		analyst.analyzeData();
		testdata.printConvergentIntervals();

	}

	@Test
	public void test3() {

		SingleVariableConvergentAnalysis analyst = new SingleVariableConvergentAnalysis();
		RtDataToBeAnalyzed testdata = new RtDataToBeAnalyzed(GetData.KKS);

		testdata.fragmentSize = 50;
		testdata.exceptionRate = 0.06F; // 0.05
		testdata.tolerance = 2.0F; // 2
		testdata.absDifference = 5;
		testdata.vaRatio = 0.05F;

		// =================================
		// 构造数据
		long time = 1438399614000l;
		for (int i = 0; i < datas.length; i++) {
			List<TimeValue> tvList = new ArrayList<TimeValue>();
			TimeValue tv = new TimeValue();
			tv.Time = new Date(time + i * 1000);
			tv.Value = datas[i];
			tvList.add(tv);

			testdata.addData(tvList);
			analyst.setDataToBeAnalyzed(testdata);
			analyst.analyzeData();
		}
		// analyst.analyzeData();
		// ---------------------------------
		testdata.printConvergentIntervals();
	}
}
