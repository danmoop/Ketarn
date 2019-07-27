package com.danmoop.novanode.MainApplication.misc;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

// This class is a part of JMoney, I had to use only this Currency class
public class Currency
{
    public static String[] currencies = {"USD", "CAD", "AUD", "GBP", "RUB", "EUR"};

    private static List<String> currencyList  = new ArrayList<>();

    private Map<String, String> currencySigns;

    private String currencyType;
    private double amount;

    private boolean isSignHidden;

    private NumberFormat formatter;

    public Currency(double amount, String currencyType)
    {

        this.currencyType = currencyType;
        this.amount = amount;
        createCurrencySigns();

        currencyList.addAll(Arrays.asList(currencies));

        if(!currencyList.contains(currencyType))
        {
            currencyList.add(currencyType);
            currencySigns.put(currencyType, "(sign)");
        }

        isSignHidden = false;
    }

    public String getCurrencyType()
    {
        return currencyType;
    }

    public double getAmount()
    {
        return Math.round(amount * 100.0) / 100.0;
    }

    public String getAmountString()
    {
        return formatter.format(Math.round(amount * 100.0) / 100.0).replace(",", ".");
    }

    public String getFormattedAmount()
    {
        NumberFormat currencyFormat = new DecimalFormat("#,###.##");

        String result = currencyFormat.format(getAmount());

        return isSignHidden ? result : currencySigns.get(currencyType) + result;
    }

    public void setAmount(double amount)
    {
        this.amount = amount;
    }

    public void add(double amount)
    {
        setAmount(getAmount() + amount);
    }

    public void subtract(double amount)
    {
        setAmount(getAmount() - amount);
    }

    public String getText()
    {
        return currencySigns.get(currencyType) + formatter.format(amount);
    }


    public int cents()
    {
        return (int) (amount * 100);
    }

    private void createCurrencySigns()
    {
        currencySigns = new HashMap<>();

        currencySigns.put("USD", "$");
        currencySigns.put("CAD", "$");
        currencySigns.put("AUD", "$");
        currencySigns.put("GBP", "£");
        currencySigns.put("RUB", "\u20BD");
        currencySigns.put("EUR", "€");

        formatter = new DecimalFormat("###.#####");
    }

    public void setCurrencyType(String currencyType)
    {
        this.currencyType = currencyType;
    }

    public static List<String> getCurrencyList()
    {
        return currencyList;
    }

    public void setCurrencySign(String sign)
    {
        currencySigns.put(currencyType, sign);
    }

    public boolean isEqualTo(Currency cur)
    {
        if(this.currencyType == cur.currencyType && this.amount == cur.amount)
            return true;

        return false;
    }

    public void hideCurrencySign()
    {
        isSignHidden = true;
    }

    public void showCurrencySign()
    {
        isSignHidden = false;
    }
}