package controller;

import entity.ImportInvoice;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import model.ImportInvoiceModel;

public class ImportInvoiceController {
    private ImportInvoiceModel model;
    private int itemsPerPage = 12;

    public ImportInvoiceController() {
        model = new ImportInvoiceModel();
    }

    public void loadImportInvoiceWarehouse(TableView<ImportInvoice> importInvoiceTable, String search, int currentPage, int idWarehouse) {
        ObservableList<ImportInvoice> allReceipts = model.getImportInvoiceWarehouse(idWarehouse, search);

        // Tính toán số trang
        int totalItems = allReceipts.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        // Giới hạn dữ liệu trong trang hiện tại
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);

        if (fromIndex <= toIndex) {
            ObservableList<ImportInvoice> paginatedReceipts = FXCollections.observableArrayList(allReceipts.subList(fromIndex, toIndex));
            importInvoiceTable.setItems(paginatedReceipts);
        }
    }

    public int getTotalPagesByWarehouse(String search, int idWarehouse) {
        ObservableList<ImportInvoice> allReceipts = model.getImportInvoiceWarehouse(idWarehouse, search);
        return (int) Math.ceil((double) allReceipts.size() / itemsPerPage);
    }

    public void loadImportInvoiceStore(TableView<ImportInvoice> importInvoiceTable, String search, int currentPage, int idStore) {
        ObservableList<ImportInvoice> allReceipts = model.getImportInvoiceStore(idStore, search);

        // Tính toán số trang
        int totalItems = allReceipts.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        // Giới hạn dữ liệu trong trang hiện tại
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);

        if (fromIndex <= toIndex) {
            ObservableList<ImportInvoice> paginatedReceipts = FXCollections.observableArrayList(allReceipts.subList(fromIndex, toIndex));
            importInvoiceTable.setItems(paginatedReceipts);
        }
    }

    public int getTotalPagesByStore(String search, int idStore) {
        ObservableList<ImportInvoice> allReceipts = model.getImportInvoiceStore(idStore, search);
        return (int) Math.ceil((double) allReceipts.size() / itemsPerPage);
    }

    public void loadAllImportInvoice (TableView<ImportInvoice> importInvoiceTable, String search, int currentPage) {
        ObservableList<ImportInvoice> allReceipts = model.getAllImportInvoices(search);

        // Tính toán số trang
        int totalItems = allReceipts.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        // Giới hạn dữ liệu trong trang hiện tại
        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);

        if (fromIndex <= toIndex) {
            ObservableList<ImportInvoice> paginatedReceipts = FXCollections.observableArrayList(allReceipts.subList(fromIndex, toIndex));
            importInvoiceTable.setItems(paginatedReceipts);
        }
    }

    public int getTotalPagesAll(String search) {
        ObservableList<ImportInvoice> allReceipts = model.getAllImportInvoices(search);
        return (int) Math.ceil((double) allReceipts.size() / itemsPerPage);
    }

    public String getWarehouseManager(String warehouseName) {
        return model.getWarehouseManager(warehouseName);
    }

    public String getStoreManager(String storeName) {
        return model.getStoreManager(storeName);
    }

    public boolean updateImportStatus (String invoiceName, String status) {
        return model.updateImportStatus(invoiceName, status);
    }
}
