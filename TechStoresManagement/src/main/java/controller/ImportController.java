package controller;

import entity.Import;
import entity.Product;
import javafx.collections.ObservableList;
import model.ImportManagementModel;

import java.util.ArrayList;
import java.util.List;

public class ImportController {
    private final ImportManagementModel model;

    public ImportController() {
        model = new ImportManagementModel();
    }
    public List<Import> getAllImportedStore(String searchitem){
        return model.getAllImportStore(searchitem);
    }
    public List<Import> getAllImportedWarehouse(String searchitem){
        return model.getAllImportWarehouse(searchitem);
    }

    public ArrayList<Product> getAllProducts() {
        return model.getAllProducts();
    }
}
