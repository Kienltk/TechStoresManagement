package entity;

public class StoreFinancial {
    private int idStore;
    private String date;
    private double turnover;
    private double capital;
    private double profit;

    // Constructor, getter và setter
    public StoreFinancial(int idStore, String date, double turnover, double capital, double profit) {
        this.idStore = idStore;
        this.date = date;
        this.turnover = turnover;
        this.capital = capital;
        this.profit = profit;
    }

    public int getIdStore() {
        return idStore;
    }

    public void setIdStore(int idStore) {
        this.idStore = idStore;
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