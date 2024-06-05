package com.clinitalPlatform.enums;

public enum ConsultationPeriodEnum {

	MIN05(5),MIN10(10),MIN15(15), MIN20(20),MIN25(25), MIN30(30);

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
