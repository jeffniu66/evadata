package cn.oge.sci;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.oge.kdm.rdp.center.evaluation.domain.EvaluationData;
import cn.oge.kdm.rdp.center.evaluation.domain.EvaluationRule;
import cn.oge.kdm.service.dto.RTDataSet;
import cn.oge.sci.data.DataLoader;
import cn.oge.sci.data.kdm.KdmDubboApi;
import cn.oge.sci.evadata.EvadataMgt;
import cn.oge.sci.evadata.StatInfo;
import cn.oge.sci.evadata.util.EvaUtils;
import cn.oge.sci.util.DubboDataloader;
import cn.oge.sci.util.RestEvaService;

public class JEvadataMainRest {

	private static Logger logger = LoggerFactory.getLogger(JEvadataMainRest.class);

	private static DataLoader dataLoader;

	public static StatInfo calc() {
		return calc("211.154.164.67", 5080, 20883, 1);
	}

	private static void init(String host, int restPort, int dubboPort) {
		if (dataLoader == null) {
			String dubboUrl = "dubbo://" + host + ":" + dubboPort;
			// FOR TEST
			// String dubboUrl = "dubbo://211.154.164.67:20883";
			// 通过KdmDubboApi获取实时数据
			dataLoader = new KdmDubboApi(dubboUrl);
			// 通过RestEvaService获取配置/规则
			RestEvaService.setRestUrl(host, restPort);
			// 通过DubboDataloader 存评价结果
			DubboDataloader.setDubboUrl(host, dubboPort);

			// 先获取编码规则
			cacheEvaRulesList();
		}
	}

	private static List<EvaluationRule> cacheRules = new ArrayList<EvaluationRule>();
	private static Long LastCacheRulesTime = 0L;// 记录上次缓存Rule规则的时间
	private static int ReqPageSize = 500;// 每次请求数量
	private static int cachePages = 0;// 缓存页码数
	private static int cacheTotal = 0;// 缓存总数
	private static Long MaxCacheTime = 1000 * 60 * 10L;// 最大缓存时间10分钟
	private static boolean isConvergence = true; //收敛标识

	/**
	 * <p>
	 * 评价配置（或者是Rule规则）就rdp提供的接口是分页获取的，因此需要分页处理
	 * </p>
	 * <ul>
	 * <li>2015.8.15 - 增加缓存评价规则功能</li>
	 * </ul>
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public static StatInfo calc(String host, int restPort, int dubboPort, int isConverg) {
		
		if(isConverg == 0){
			isConvergence = false;
		}else if(isConverg == 0){
			isConvergence = true;
		}
		
		init(host, restPort, dubboPort);

		long nowTime = new Date().getTime();
		if (MaxCacheTime < (nowTime - LastCacheRulesTime) || cacheRules == null) {
			// 重新获取规则
			cacheEvaRulesList();
		}

		if (cacheRules == null || cacheRules.isEmpty()) {
			return new StatInfo();
		}

		int paseSize = ReqPageSize;
		int pages = cachePages;
		List<EvaluationData> evadataRet = new ArrayList<EvaluationData>();
		List<RTDataSet> rtdsList = new ArrayList<RTDataSet>();
		for (int i = 0; i < pages; i++) {
			try {

				int fromIndex = i * paseSize;
				int toIndex = (i + 1) * paseSize;
				if (toIndex > cacheTotal) {
					toIndex = cacheTotal;
				}
				List<EvaluationRule> rules = cacheRules.subList(fromIndex, toIndex);

				// 加载数据
				List<RTDataSet> dataList = dataLoader.getRTDataSnapshot(EvaUtils.getKksString(rules));
				rtdsList.addAll(dataList);
				if (dataList == null || dataList.isEmpty()) {
					System.out.println("在第" + (i + 1) + "页没有数据！！！");
					continue;
				}
				// 进入收敛评价
				List<EvaluationData> evadataList = EvadataMgt.execute(rules, dataList, isConvergence);
				evadataRet.addAll(evadataList);
				DubboDataloader.saveEvaResult(evadataList);
			} catch (Exception ex) {
				logger.error("收敛评价出错", ex);
			}
		}
		// 需要评价的的RTDataSet个数
		int rtDataSetNum = rtdsList.size();
		// 获取收敛后的实时数据个数
		int convergenceNumber = evadataRet.size();
		// 获取评价后状态的个数
		int stateNumberA = 0;
		int stateNumberB = 0;
		int stateNumberC = 0;
		int stateNumberD = 0;
		for (EvaluationData evaData : evadataRet) {
			if (evaData.getState().name().equals("A")) {
				stateNumberA++;
			}
			if (evaData.getState().name().equals("B")) {
				stateNumberB++;
			}
			if (evaData.getState().name().equals("C")) {
				stateNumberC++;
			}
			if (evaData.getState().name().equals("D")) {
				stateNumberD++;
			}
		}
		StatInfo statInfo = new StatInfo();
		statInfo.setRuleNum(cacheTotal);
		statInfo.setRtDataSetNum(rtDataSetNum);
		statInfo.setConvergenceNum(convergenceNumber);
		statInfo.setStateNumA(stateNumberA);
		statInfo.setStateNumB(stateNumberB);
		statInfo.setStateNumC(stateNumberC);
		statInfo.setStateNumD(stateNumberD);
		return statInfo;
	}

	public static void cacheEvaRulesList() {

		int pageSize = ReqPageSize;
		Map<String, Object> page = RestEvaService.getEvaRulePage(0, pageSize);

		int pages = Integer.valueOf(page.get("totalPages").toString());
		if (pages == 0) {
			System.out.println("：：：没有获取到评价规则，请检查是否已经配置评价规则！！！");
		}

		List<EvaluationRule> ruleList = new ArrayList<EvaluationRule>();
		for (int i = 0; i < pages; i++) {
			// 加载规则
			List<EvaluationRule> rules = RestEvaService.getEvaRuleList(i, pageSize);
			ruleList.addAll(rules);
		}
		cacheRules = ruleList;
		cachePages = pages;
		cacheTotal = (Integer) page.get("totalElements");
	}

	/**
	 * <p>
	 * 评价配置（或者是Rule规则）就rdp提供的接口是分页获取的，因此需要分页处理
	 * </p>
	 * 没有缓存
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public static StatInfo calcNoCache(String host, int restPort, int dubboPort) {

		init(host, restPort, dubboPort);

		int pageSize = 500;
		Map<String, Object> page = RestEvaService.getEvaRulePage(0, pageSize);
		// 配置数量
		int ruleNum = (Integer) page.get("totalElements");
		int pages = Integer.valueOf(page.get("totalPages").toString());
		if (pages == 0) {
			System.out.println("：：：没有获取到评价规则，请检查是否已经配置评价规则！！！");
		}
		List<EvaluationData> evadataRet = new ArrayList<EvaluationData>();
		List<RTDataSet> rtdsList = new ArrayList<RTDataSet>();
		for (int i = 0; i < pages; i++) {
			try {
				// logger.info("：：批次[{}]->加载KKSEva配置信息{}条！", i + 1,
				// subKksEvaList.size());

				// 加载规则
				List<EvaluationRule> rules = RestEvaService.getEvaRuleList(i, pageSize);
				// 加载数据
				List<RTDataSet> dataList = dataLoader.getRTDataSnapshot(EvaUtils.getKksString(rules));
				rtdsList.addAll(dataList);
				if (dataList == null || dataList.isEmpty()) {
					System.out.println("在第" + (i + 1) + "页没有数据！！！");
					continue;
				}
				// 进入收敛评价
				List<EvaluationData> evadataList = EvadataMgt.execute(rules, dataList);
				evadataRet.addAll(evadataList);
				DubboDataloader.saveEvaResult(evadataList);
			} catch (Exception ex) {
				logger.error("收敛评价出错", ex);
			}
		}
		// 需要评价的的RTDataSet个数
		int rtDataSetNum = rtdsList.size();
		// 获取收敛后的实时数据个数
		int convergenceNumber = evadataRet.size();
		// 获取评价后状态的个数
		int stateNumberA = 0;
		int stateNumberB = 0;
		int stateNumberC = 0;
		int stateNumberD = 0;
		for (EvaluationData evaData : evadataRet) {
			if (evaData.getState().name().equals("A")) {
				stateNumberA++;
			}
			if (evaData.getState().name().equals("B")) {
				stateNumberB++;
			}
			if (evaData.getState().name().equals("C")) {
				stateNumberC++;
			}
			if (evaData.getState().name().equals("D")) {
				stateNumberD++;
			}
		}
		StatInfo statInfo = new StatInfo();
		statInfo.setRuleNum(ruleNum);
		statInfo.setRtDataSetNum(rtDataSetNum);
		statInfo.setConvergenceNum(convergenceNumber);
		statInfo.setStateNumA(stateNumberA);
		statInfo.setStateNumB(stateNumberB);
		statInfo.setStateNumC(stateNumberC);
		statInfo.setStateNumD(stateNumberD);
		return statInfo;
	}
}
