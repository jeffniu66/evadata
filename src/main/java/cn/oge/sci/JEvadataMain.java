package cn.oge.sci;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import cn.oge.kdm.rdp.center.evaluation.constants.EvaluationRuleType;
import cn.oge.kdm.rdp.center.evaluation.domain.EvaluationData;
import cn.oge.kdm.rdp.center.evaluation.domain.EvaluationRule;
import cn.oge.kdm.rdp.center.evaluation.facade.EvaluationFacade;
import cn.oge.kdm.service.dto.RTDataSet;
import cn.oge.sci.data.DataLoader;
import cn.oge.sci.data.kdm.KdmDubboApi;
import cn.oge.sci.evadata.EvadataMgt;
import cn.oge.sci.evadata.StatInfo;
import cn.oge.sci.evadata.util.EvaUtils;
import cn.oge.sci.util.DubboDataloader;

public class JEvadataMain {

	private static Logger logger = LoggerFactory.getLogger(JEvadataMain.class);

	private static DataLoader dataLoader;

	public static StatInfo calc() {
		return calc("211.154.164.67", 5080);
	}

	private static void init(String dubboUrl) {
		if (dataLoader == null) {
			dataLoader = new KdmDubboApi(dubboUrl);
		}
	}

	/**
	 * <p>
	 * 评价配置（或者是Rule规则）就rdp提供的接口是分页获取的，因此需要分页处理
	 * </p>
	 * 
	 * @param host
	 * @param port
	 * @return
	 */
	public static StatInfo calc(String host, int port) {

		String dubboUrl = "dubbo://" + host + ":" + port;
		init(dubboUrl);

		EvaluationFacade evaService = DubboDataloader.getRdpService(dubboUrl, EvaluationFacade.class);

		// ===============================================
		// ：：：：spring data的Pageable，page的下标从0开始：：：：
		// ===============================================
		Pageable pageInfo = new PageRequest(0, 1000);

		boolean enabled = true;
		Page<? extends EvaluationRule> rules1 = evaService.findRulesByType(EvaluationRuleType.THRESHOLD, enabled,
				pageInfo);
		rules1.getTotalElements();
		int pages = rules1.getTotalPages();

		for (int i = 0; i < pages; i++) {
			try {
				// logger.info("：：批次[{}]->加载KKSEva配置信息{}条！", i + 1,
				// subKksEvaList.size());
				Page<? extends EvaluationRule> rules = evaService.findRulesByType(EvaluationRuleType.THRESHOLD,
						enabled, pageInfo);
				List<RTDataSet> dataList = dataLoader.getRTDataSnapshot(EvaUtils.getKksString(rules.getContent()));

				List<EvaluationData> evadataList = EvadataMgt.execute(rules.getContent(), dataList);
				// TODO TRY Catch
				DubboDataloader.saveEvaResult(evadataList);
			} catch (Exception ex) {
				logger.error("第一页评价错误", ex);
			}
		}
		StatInfo statInfo = new StatInfo();
		// TODO 写统计信息
		return statInfo;
	}
}
