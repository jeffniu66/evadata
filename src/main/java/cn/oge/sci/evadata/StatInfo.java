package cn.oge.sci.evadata;

public class StatInfo {
	/** 评价之前需要配置的个数 */
	private int ruleNum;
	/** 评价之前需要评价的的RTDataSet个数 */
	private int rtDataSetNum;
	/** 收敛的个数 */
	private int convergenceNum;
	/** 评价后的状态A的个数 */
	private int stateNumA;
	/** 评价后的状态B的个数 */
	private int stateNumB;
	/** 评价后的状态C的个数 */
	private int stateNumC;
	/** 评价后的状态D的个数 */
	private int stateNumD;

	public int getRuleNum() {
		return ruleNum;
	}

	public void setRuleNum(int ruleNum) {
		this.ruleNum = ruleNum;
	}

	public int getRtDataSetNum() {
		return rtDataSetNum;
	}

	public void setRtDataSetNum(int rtDataSetNum) {
		this.rtDataSetNum = rtDataSetNum;
	}

	public int getConvergenceNum() {
		return convergenceNum;
	}

	public void setConvergenceNum(int convergenceNum) {
		this.convergenceNum = convergenceNum;
	}

	public int getStateNumA() {
		return stateNumA;
	}

	public void setStateNumA(int stateNumA) {
		this.stateNumA = stateNumA;
	}

	public int getStateNumB() {
		return stateNumB;
	}

	public void setStateNumB(int stateNumB) {
		this.stateNumB = stateNumB;
	}

	public int getStateNumC() {
		return stateNumC;
	}

	public void setStateNumC(int stateNumC) {
		this.stateNumC = stateNumC;
	}

	public int getStateNumD() {
		return stateNumD;
	}

	public void setStateNumD(int stateNumD) {
		this.stateNumD = stateNumD;
	}
	
}
