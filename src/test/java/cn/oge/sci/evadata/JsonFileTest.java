package cn.oge.sci.evadata;

import java.util.List;

import cn.oge.kdm.service.dto.RTDataSet;
import cn.oge.sci.evadata.util.EvaUtils;
import cn.oge.sci.util.GetData;
import cn.oge.sci.util.KdmJsonUtils;

import com.oge.rtdatanalysis.RtDataToBeAnalyzed;
import com.oge.rtdatanalysis.SingleVariableConvergentAnalysis;
import com.oge.rtdsp.response.TimeValue;

public class JsonFileTest {
	public static void main(String[] args) {
		
		SingleVariableConvergentAnalysis analyst = new SingleVariableConvergentAnalysis();
		RtDataToBeAnalyzed testdata = new RtDataToBeAnalyzed(GetData.KKS);
		
		String filepath = "target/data1.json";
		String json = KdmJsonUtils.readFile(filepath);
		List<RTDataSet> rtdsList = KdmJsonUtils.getRTDataSet(json);
		List<TimeValue> tvList = EvaUtils.getTimeValueList(rtdsList.get(0));
		testdata.addData(tvList);
		testdata.fragmentSize = 20;
		testdata.exceptionRate = 0.06F; // 0.05
		testdata.tolerance = 2.0F; // 2
		testdata.absDifference = 5;
		testdata.vaRatio = 0.05F;
		analyst.setDataToBeAnalyzed(testdata);

		analyst.analyzeData();
		testdata.printConvergentIntervals();

		System.out.println("=======================================================");
		String filepath2 = "target/data2.json";
		String json2 = KdmJsonUtils.readFile(filepath2);
		List<RTDataSet> rtdsList2 = KdmJsonUtils.getRTDataSet(json2);
		List<TimeValue> tvList2 = EvaUtils.getTimeValueList(rtdsList2.get(0));
		testdata.addData(tvList2);
		analyst.analyzeData();
		testdata.printConvergentIntervals();

		//testdata.deleteData(100);

		System.out.println("=======================================================");
		// analyst.reAnalyzeData();
		testdata.printConvergentIntervals();

		
		/*while (true) {
			testdata.clearData();
			testdata.addData(tvList2);
			analyst.analyzeData();
			testdata.printConvergentIntervals();
		}*/
	}
}
