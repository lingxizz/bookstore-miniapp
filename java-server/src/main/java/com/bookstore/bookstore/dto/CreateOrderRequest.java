package com.bookstore.bookstore.dto;

public class CreateOrderRequest {
    private Integer amount;
    private Integer coinAmount;

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
    public Integer getCoinAmount() { return coinAmount; }
    public void setCoinAmount(Integer coinAmount) { this.coinAmount = coinAmount; }
}
