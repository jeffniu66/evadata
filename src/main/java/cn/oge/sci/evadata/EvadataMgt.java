package cn.oge.sci.evadata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.oge.kdm.rdp.center.evaluation.constants.EvaluationState;
import cn.oge.kdm.rdp.center.evaluation.domain.EvaluationData;
import cn.oge.kdm.rdp.center.evaluation.domain.EvaluationRule;
import cn.oge.kdm.rdp.center.evaluation.dto.EvaluationDataDTO;
import cn.oge.kdm.service.dto.KKSEva;
import cn.oge.kdm.service.dto.RTDataSet;
import cn.oge.kdm.service.dto.RTValue;

/**
 * <h1>评价管理</h1>
 * 
 * @author jimcoly
 *
 */
public class EvadataMgt {

	private static Logger logger = LoggerFactory.getLogger(EvadataMgt.class);

	/**
	 * 缓存上次的评价状态
	 */
	private static Map<String, String> LastStateCache = new HashMap<String, String>();

	/**
	 * 执行评价
	 * <ul>
	 * <li>1. 根据rule配置的KKS获取实时数据</li>
	 * <li>2. 将数据进行收敛过滤</li>
	 * <li>3. 将数据进行评价判定（ABCD等级评定）</li>
	 * <li>4. 返回评定结果</li>
	 * </ul>
	 * 
	 * @param ruleList
	 *            评价规则
	 * @param rtdsList
	 *            评价数据
	 * @return 评价结果
	 */
	public static List<EvaluationData> execute(List<? extends EvaluationRule> ruleList, List<RTDataSet> rtdsList) {
		return execute(ruleList, rtdsList, true);
	}

	/**
	 * @param ruleList
	 *            评价规则
	 * @param rtdsList
	 * @param isConvergence
	 *            是否收敛
	 * @return 评价结果
	 */
	public static List<EvaluationData> execute(List<? extends EvaluationRule> ruleList, List<RTDataSet> rtdsList,
			boolean isConvergence) {

		System.out.println("isConvergence============================" + isConvergence);
		
		List<EvaluationData> evaResultList = new ArrayList<EvaluationData>();
		logger.info("===执行收敛算法逻辑之前数据的数据量为：{}", rtdsList.size());

		// =================================================
		// 从rtdsList得到一个Map映射（key为kksCode，value为rtds）
		// 方便后面根据kksCode去索引查找
		Map<String, RTDataSet> rtdsMap = new HashMap<String, RTDataSet>();
		for (RTDataSet rtds : rtdsList) {
			rtdsMap.put(rtds.getKksCode(), rtds);
		}

		// =================================================
		// 循环评价量，逐个对其值进行判断
		for (EvaluationRule rule : ruleList) {

			String kksCode = rule.getKksCode();
			// 如果没有kks值就跳到下个
			if (!rtdsMap.containsKey(kksCode)) {
				continue;
			}

			RTDataSet rtds = rtdsMap.get(kksCode);
			List<RTValue> rtvalueList = rtds.getRTDataValues();
			if (rtvalueList == null || rtvalueList.isEmpty()) {
				continue;
			}

			if (isConvergence) {
				// 执行收敛逻辑
				rtvalueList = DataConvergence.convergent(rtvalueList, kksCode);
			}

			if (rtvalueList == null) {
				continue;
			}

			Map<String, Object> evaRule = rule.getRule();
			String ruleId = rule.getId();

			String lastState = "";
			if (LastStateCache.containsKey(kksCode)) {
				lastState = LastStateCache.get(kksCode);
			}

			for (RTValue rtv : rtvalueList) {

				double dval = Double.valueOf(rtv.getValue() + "");
				String status = DataEvaluate.evalStatus(dval, evaRule);

				// 比较上次评价状态
				if (lastState.equals(status)) {
					continue;
				}
				lastState = status;

				EvaluationDataDTO evadata = new EvaluationDataDTO();
				evadata.setRuleId(ruleId);
				evadata.setKksCode(kksCode);
				evadata.setTime(new Date(rtv.getTime()));
				evadata.setState(EvaluationState.valueOf(status));
				evadata.setValue(dval);
				evaResultList.add(evadata);
			}

			// 更新缓存状态
			LastStateCache.put(kksCode, lastState);
		}
		logger.info("：：：：执行收敛算法逻辑之后数据的数据量为：{}", evaResultList.size());
		return evaResultList;
	}

	public static String getKks(List<KKSEva> ruleList) {
		StringBuffer sbKks = new StringBuffer();
		for (KKSEva kksEva : ruleList) {
			sbKks.append(",").append(kksEva.getKksCode());
		}
		String kks = sbKks.substring(1);
		return kks;
	}

}
