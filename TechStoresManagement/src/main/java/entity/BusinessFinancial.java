package entity;

public class BusinessFinancial {
    private String date;
    private double turnover;
    private double capital;
    private double profit;

    // Constructor, getter và setter
    public BusinessFinancial(String date, double turnover, double capital, double profit) {
        this.date = date;
        this.turnover = turnover;
        this.capital = capital;
        this.profit = profit;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTurnover() {
        return turnover;
    }

    public void setTurnover(double turnover) {
        this.turnover = turnover;
    }

    public double getCapital() {
        return capital;
    }

    public void setCapital(double capital) {
        this.capital = capital;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }
}