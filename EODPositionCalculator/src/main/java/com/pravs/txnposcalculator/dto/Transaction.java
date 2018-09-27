package com.pravs.txnposcalculator.dto;

public class Transaction {
	private Integer transactionId;
	private String instrument;
	private String transactionType;
	private Integer transactionQuantity;
	
	public Integer getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(Integer transactionId) {
		this.transactionId = transactionId;
	}
	public String getInstrument() {
		return instrument;
	}
	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}
	public String getTransactionType() {
		return transactionType;
	}
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}
	public Integer getTransactionQuantity() {
		return transactionQuantity;
	}
	public void setTransactionQuantity(Integer transactionQuantity) {
		this.transactionQuantity = transactionQuantity;
	}	
}
