package com.pravs.txnposcalculator.dto;

public class EODPosition {
	private String instrument;
	private Integer account;
	private String accountType;
	private Integer quantity;
	private Integer delta;
	private Integer originialQuantity;
	
	
	public String getInstrument() {
		return instrument;
	}
	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}
	public Integer getAccount() {
		return account;
	}
	public void setAccount(Integer account) {
		this.account = account;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public Integer getQuantity() {
		return quantity;
	}
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	public Integer getDelta() {
		return delta;
	}
	public void setDelta(Integer delta) {
		this.delta = delta;
	}

	public Integer getOriginialQuantity() {
		return originialQuantity;
	}

	public void setOriginialQuantity(Integer originialQuantity) {
		this.originialQuantity = originialQuantity;
	}
}

