package controller;

import entity.Product;
import entity.Warehouse;
import model.WarehouseModel;

import java.util.List;

public class WarehouseController {
    private WarehouseModel warehouseModel;

    public WarehouseController() {
        warehouseModel = new WarehouseModel();
    }

    // Lấy danh sách cửa hàng
    public List<Warehouse> getWarehouses(String search) {
        return warehouseModel.getWarehouses(search);
    }

    // Thêm cửa hàng
    public void addWarehouse(String name, String address) {
        warehouseModel.addWarehouse(name, address);
    }

    // Sửa cửa hàng
    public void updateWarehouse(int id, String name, String address, Integer managerId) {
        warehouseModel.updateWarehouse(id, name, address, managerId);
    }


    // Xóa cửa hàng
    public boolean deleteWarehouse(int warehouseId) {
        if (!warehouseModel.hasInventory(warehouseId)) {
            warehouseModel.deleteWarehouse(warehouseId);
            return true;
        }
        return false;
    }

    public boolean isWarehouseNameDuplicate(String name) {
        return warehouseModel.isWarehouseNameDuplicate(name);
    }

    public boolean isWarehouseAddressDuplicate(String address) {
        return warehouseModel.isWarehouseAddressDuplicate(address);
    }


    public List<Product> getProductsByWarehouseId(int warehouseId) {
        return warehouseModel.getProductsByWarehouseId(warehouseId);
    }
}
