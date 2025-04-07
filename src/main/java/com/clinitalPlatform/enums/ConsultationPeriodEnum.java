package com.clinitalPlatform.enums;

public enum ConsultationPeriodEnum {

	MIN05(5),MIN10(10),MIN15(15), MIN20(20),MIN25(25), MIN30(30) , MIN35(35) , MIN40(40) , MIN45(45) , MIN50(50) , MIN55(55) ;

	int value;

	ConsultationPeriodEnum(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

}
