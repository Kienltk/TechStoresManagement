package controller;

import entity.ProductReceipt;
import entity.Receipt;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.HistoryModel;

public class HistoryController {
    private HistoryModel model;
    private int itemsPerPage = 10;

    public HistoryController() {
        model = new HistoryModel();
    }

    public void loadReceipts(TableView<Receipt> receiptTable, String customerName) {
        ObservableList<Receipt> receipts = model.getReceipts(customerName);
        receiptTable.setItems(receipts);
    }

    public String getCustomerPhone(String customerName) {
        return model.getCustomerPhone(customerName);
    }

    public String getStoreManager(String storeName) {
        return model.getStoreManager(storeName);
    }

    public String getCashier(int receiptId) {
        return model.getCashier(receiptId);
    }


}
