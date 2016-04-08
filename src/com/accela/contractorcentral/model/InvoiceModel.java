package com.accela.contractorcentral.model;

import java.util.Date;

import com.accela.framework.AMBaseModel;


public class InvoiceModel extends AMBaseModel{

	private String recordCustomId;

	private Double amount;

	private Double balanceDue;
	
	private Date applyDate;
	
	private Long invoiceId;

	public String getRecordCustomId() {
		return recordCustomId;
	}

	public void setRecordCustomId(String recordCustomId) {
		this.recordCustomId = recordCustomId;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Double getBalanceDue() {
		return balanceDue;
	}

	public void setBalanceDue(Double balanceDue) {
		this.balanceDue = balanceDue;
	}

	public Date getApplyDate() {
		return applyDate;
	}

	public void setApplyDate(Date applyDate) {
		this.applyDate = applyDate;
	}

	public Long getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(Long invoiceId) {
		this.invoiceId = invoiceId;
	}
}
