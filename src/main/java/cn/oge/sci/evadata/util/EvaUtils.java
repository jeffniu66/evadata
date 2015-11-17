package cn.oge.sci.evadata.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.oge.kdm.rdp.center.evaluation.domain.EvaluationRule;
import cn.oge.kdm.service.dto.RTDataSet;
import cn.oge.kdm.service.dto.RTValue;

import com.oge.rtdsp.response.TimeValue;

public class EvaUtils {

	public static List<TimeValue> getTimeValueList(RTDataSet rtds) {

		List<TimeValue> tvList = new ArrayList<TimeValue>();
		List<RTValue> rtvalList = rtds.getRTDataValues();
		for (RTValue rtval : rtvalList) {
			TimeValue tv = new TimeValue();
			tv.Time = new Date(rtval.getTime());
			tv.Value = rtval.getValue();
			tvList.add(tv);
		}

		return tvList;
	}

	public static String getKksString(List<? extends EvaluationRule> ruleList) {

		if (ruleList.size() > 0) {
			StringBuffer sbKks = new StringBuffer();
			for (EvaluationRule rule : ruleList) {
				sbKks.append(",").append(rule.getKksCode());
			}

			return sbKks.substring(1);
		}

		return "";
	}
}
