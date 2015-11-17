package cn.oge.sci.evadata;

import java.util.Map;

/**
 * <h1>评价判定</h1>
 * 
 * @author jimcoly
 *
 */
public class DataEvaluate {

	/**
	 * 小于等于低低值 L 大于高高值 H
	 * 
	 * @param value
	 * @param rule
	 * @return
	 */
	public static String evalStatus(double value, Map<String, Object> rule) {

		// A,B,C,D，是否递增，默认递减
		boolean isIncrease = Boolean.valueOf(rule.get("isIncrease").toString());

		double H1 = Double.valueOf(rule.get("highLine").toString());
		double M1 = Double.valueOf(rule.get("middleLine").toString());
		double L1 = Double.valueOf(rule.get("lowLine").toString());

		/** 低低值 */
		double LowNA = Double.valueOf(rule.get("lowInvalid").toString());
		/** 高高值 */
		double HeightNA = Double.valueOf(rule.get("highInvalid").toString());

		// 2、判断是否配置高高值，低低值
		if (LowNA == 0.0d & HeightNA == 0.0d) { // 都为0
			// 则表示未配置高高值，低低值
			if (isIncrease) {// 递增 A<B<C<D
				if (value <= L1) {
					return "A";
				} else if (value <= M1 && value > L1) {
					return "B";
				} else if (value <= H1 && value > M1) {
					return "C";
				} else if (value > H1) {
					return "D";
				}
			} else {// 递减 A>B>C>D
				if (value <= L1) {
					return "D";
				} else if (value <= M1 && value > L1) {
					return "C";
				} else if (value <= H1 && value > M1) {
					return "B";
				} else if (value > H1) {
					return "A";
				}
			}
		} else {// 配置了高高值、低低值
			if (value <= LowNA) { // 低低值
				return "L";
			} else if (value > HeightNA) { // //高高值
				return "H";
			}
			// 递增 A<B<C<D
			if (isIncrease) {
				if (value <= L1 && value > LowNA) {
					return "A";
				} else if (value <= M1 && value > L1) {
					return "B";
				} else if (value <= H1 && value > M1) {
					return "C";
				} else if (value <= HeightNA && value > H1) {
					return "D";
				}
			} else {// 递减 A>B>C>D
				if (value <= L1 && value > LowNA) {
					return "D";
				} else if (value <= M1 && value > L1) {
					return "C";
				} else if (value <= H1 && value > M1) {
					return "B";
				} else if (value <= HeightNA && value > H1) {
					return "A";
				}
			}
		}
		
		// logger.warn("getStatusByValue waring, can't read rtvalue " + value);
		return "N";// 数据无法判断
	}

}
