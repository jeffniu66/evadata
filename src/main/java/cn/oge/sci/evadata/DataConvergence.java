package cn.oge.sci.evadata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.oge.kdm.service.dto.RTValue;

import com.oge.rtdatanalysis.ConvergentInterval;
import com.oge.rtdatanalysis.RtDataToBeAnalyzed;
import com.oge.rtdatanalysis.SingleVariableConvergentAnalysis;
import com.oge.rtdsp.response.TimeValue;

/**
 * <h1>收敛算法判断方法</h1>
 * <p>
 * <b>方法一：利用方差和正态分布</b>
 * <ol>
 * <li>进入样本数：x1 x2 x3 ….xn</li>
 * <li>求平均值：m</li>
 * <li>计算方差 s^2=[(x1-m)^2+(x2-m)^2+…..(xn-m)^2]*(1/n)<br>
 * 方差是各个数据分别与其平均数之差的平方的和的平均数。方差是标准差的平方</li>
 * <li>计算每个样本和平均值的差的绝对值：|x1-m| , |x2-m|,|x3-m|…|xn-m|</li>
 * <li>判断（4）里的绝对值大小 落在 2倍s^2 的值 内的个数是否占到总个数的95%，如果占到了就是收敛，否则不收敛</li>
 * </ol>
 * <b>方法二：利用方差和平均值百分比</b>
 * <ol>
 * <li>判断：s^2/m 的值是否小于 0.05</li>
 * </ol>
 * 注： ***sis为analysis的缩写，取至单词最后部分***
 * 
 * @author jimcoly
 *
 */
public class DataConvergence {

	private static Logger logger = LoggerFactory.getLogger(DataConvergence.class);

	// 存储kksCode与RtDataToBeAnalyzed对应关系
	private static Map<String, RtDataToBeAnalyzed> sisMap = new HashMap<String, RtDataToBeAnalyzed>();

	// 存储RtDataToBeAnalyzed中实时点值的数据量
	private static Map<String, Long> countMap = new HashMap<String, Long>();

	// 存储最新实时数据的时间，避免收敛区间有重复值写入
	public static Map<String, Long> timeMap = new HashMap<String, Long>();

	public static SingleVariableConvergentAnalysis sisUtil = new SingleVariableConvergentAnalysis();

	private static int fragmentSize = 20; // 最小的收敛区间大小
	// tolerance这个参数不要设置0.05F，他默认是2.0F，程序可以不设置
	private static float tolerance = 2.0f;

	/**
	 * 收敛评价算法
	 * 
	 * @param rtvalueList
	 * @param eva
	 */
	public static List<RTValue> convergent(List<RTValue> rtvalueList, String kksCode) {

		/** 数据大小 */
		int dataSize = rtvalueList.size();

		/** 构造SingleVariableConvergentAnalysis参数 */
		/** RTValue的List数据转成double数组 */
		Date[] times = new Date[dataSize];
		double[] values = new double[dataSize];
		for (int j = 0; j < dataSize; j++) {
			RTValue value = rtvalueList.get(j);
			times[j] = new Date(value.getTime());
			Object oval = value.getValue();
			if (oval instanceof Double) {
				Double val = (Double) oval;
				values[j] = val;
			} else {

				// 过滤非单值数据，如波形数据不进行评价，直接跳出
				// TODO，应该在请求数据时做过滤
				return null;
			}
		}

		List<ConvergentInterval> convergentIntervalList = null;
		/** 获取已经评价过的数据 */
		RtDataToBeAnalyzed sisData = getSisData(kksCode);
		try {
			sisData.addData(kksCode, times, values); // 添加数据
			sisUtil.setDataToBeAnalyzed(sisData);
			sisUtil.analyzeData(); // 分析数据
			convergentIntervalList = sisData.getConvergentIntervals();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("运算收敛区间异常：" + e.getMessage() + e.getClass());
			return null;
		}

		// 如果没有发现收敛区间，则进行下个Eva判断
		if (convergentIntervalList == null || convergentIntervalList.isEmpty()) {
			logger.info(kksCode + " 未发现收敛区间，数据长度：" + sisData.getRecords().size());
			return null;
		}

		// 直接获取最后一个收敛区间
		ConvergentInterval zone = convergentIntervalList.get(convergentIntervalList.size() - 1);

		// 调试信息
		logger.info("收敛区间 size : " + convergentIntervalList.size() + ", 数据长度： " + sisData.getRecords().size()
				+ ", 最后收敛区间大小： " + zone.getSize());

		// 上次更新时间（发现第一次收敛全部写入）
		long lastUpdateTime = 0l;
		if (timeMap.containsKey(kksCode)) {
			lastUpdateTime = timeMap.get(kksCode);
		}

		List<TimeValue> recordList = sisData.getRecords();

		List<RTValue> retRtvList = new ArrayList<RTValue>();
		List<Integer> exPoints = zone.getExceptionPoints();

		for (int i = zone.getStartIndex(); i < zone.getEndIndex(); i++) {
			// 异常点
			if (exPoints.contains(i)) {
				continue;
			}
			TimeValue tv = recordList.get(i);
			if (tv.Time.getTime() < lastUpdateTime) {
				continue;
			}
			RTValue rtval = new RTValue();
			rtval.setTime(tv.Time.getTime());
			rtval.setValue(tv.Value);
			retRtvList.add(rtval);
		}
		timeMap.put(kksCode, retRtvList.get(retRtvList.size() - 1).getTime());

		// TODO 更优方案
		sisData.clearData();
		if (sisData.getCurrentRecordCnt() > 120) {
			sisData.deleteData(100);
		}

		return retRtvList;
	}

	/**
	 * 根据kksCode获取对向已经分析过的数据
	 * 
	 * @param kksCode
	 * @return
	 */
	public static RtDataToBeAnalyzed getSisData(String kksCode) {

		if (sisMap.containsKey(kksCode)) {
			// 对数据集合进行大小控制，防止膨胀消化内存
			RtDataToBeAnalyzed sisData = sisMap.get(kksCode);
			int cnt = sisData.getCurrentRecordCnt();
			if (cnt > fragmentSize * 2) {
				sisData.deleteData(fragmentSize);
			}
			return sisData;
		}

		RtDataToBeAnalyzed sisData = new RtDataToBeAnalyzed(kksCode);
		sisData.fragmentSize = fragmentSize;
		sisData.tolerance = tolerance; // 正常数据波动区间：波形最高值和最低值的离散范围
		sisData.exceptionRate = 0.05f; // 异常数据波动区间：最大=波形最高值*exceptionRate，
										// 最小 = 波形最低值*exceptionRate
		sisMap.put(kksCode, sisData);
		return sisData;
	}

	public static void countKksCode(String kksCode) {
		if (countMap.containsKey(kksCode)) {
			countMap.put(kksCode, countMap.get(kksCode) + 1);
		} else {
			countMap.put(kksCode, 0l);
		}
	}
}
