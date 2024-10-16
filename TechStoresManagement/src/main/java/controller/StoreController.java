package controller;

import entity.Employee;
import entity.Product;
import entity.Store;
import model.StoreModel;

import java.util.List;
import java.util.Optional;

public class StoreController {
    private StoreModel storeModel;

    public StoreController() {
        storeModel = new StoreModel();
    }

    // Lấy danh sách cửa hàng
    public List<Store> getStores(String search) {
        return storeModel.getStores(search);
    }

    // Thêm cửa hàng
    public void addStore(String name, String address) {
        storeModel.addStore(name, address);
    }

    // Sửa cửa hàng
    public void updateStore(int id, String name, String address, Integer managerId) {
        storeModel.updateStore(id, name, address, managerId);
    }


    // Xóa cửa hàng
    public boolean deleteStore(int storeId) {
        if (!storeModel.hasInventory(storeId)) {
            storeModel.deleteStore(storeId);
            return true;
        }
        return false;
    }

    // Tính toán tài chính của cửa hàng
    public double[] calculateFinancials(int storeId) {
        return storeModel.calculateFinancials(storeId);
    }

    public boolean isStoreNameDuplicate(String name) {
        return storeModel.isStoreNameDuplicate(name);
    }

    public boolean isStoreAddressDuplicate(String address) {
        return storeModel.isStoreAddressDuplicate(address);
    }


    public List<Product> getProductsByStoreId(int storeId) {
        return storeModel.getProductsByStoreId(storeId);
    }

}
