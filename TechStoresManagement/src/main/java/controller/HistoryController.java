package controller;

import entity.ProductReceipt;
import entity.Receipt;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.HistoryModel;

public class HistoryController {
    private HistoryModel model;
    private int itemsPerPage = 12;

    public HistoryController() {
        model = new HistoryModel();
    }

    // Load receipts with pagination
    public void loadReceipts(TableView<Receipt> receiptTable, String customerName, int currentPage) {
        ObservableList<Receipt> allReceipts = model.getReceipts(customerName);

        // Tính toán số trang
        int totalItems = allReceipts.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        // Giới hạn dữ liệu trong trang hiện tại
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);

        if (fromIndex <= toIndex) {
            ObservableList<Receipt> paginatedReceipts = FXCollections.observableArrayList(allReceipts.subList(fromIndex, toIndex));
            receiptTable.setItems(paginatedReceipts);
        }
    }

    // Hàm lấy số trang tổng cộng
    public int getTotalPages(String customerName) {
        ObservableList<Receipt> allReceipts = model.getReceipts(customerName);
        return (int) Math.ceil((double) allReceipts.size() / itemsPerPage);
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
