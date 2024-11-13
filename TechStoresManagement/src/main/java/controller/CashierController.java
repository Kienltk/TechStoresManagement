package controller;

import entity.Customer;
import entity.Product;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.CashierModel;

import java.util.List;
import java.util.Map;

import static model.CashierModel.getOne;

public class CashierController {
    // Tìm khách hàng theo số điện thoại
    public static String searchCustomerByPhone(String phoneNumber) {
        return CashierModel.searchCustomerByPhone(phoneNumber);
    }


    public static double getTotalPurchasePrice(Map<Integer, Integer> cartItems) {
        double totalPurchasePrice = 0;
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            Product product = getOne(productId);

            if (product != null) {
                totalPurchasePrice += product.getPurchasePrice() * quantity;
            }
        }
        return totalPurchasePrice;
    }

    // Xử lý đơn hàng
    public static boolean processOrder(String customerPhone, Map<Integer, Integer> cartItems, double total, String employeeName, int idStore) {
        int customerId = CashierModel.getCustomerIdByPhone(customerPhone);
        if (customerId == -1) {
            System.out.println("Customer not found");
            return false;
        }

        double profit = total - getTotalPurchasePrice(cartItems);

        // Tạo hóa đơn
        if (CashierModel.createReceipt(customerId, idStore, employeeName, total, profit, cartItems) != -1) {
            return true;
        }

        // Cập nhật số lượng sản phẩm trong kho và làm trống giỏ hàng
        for (Map.Entry<Integer, Integer> entry : cartItems.entrySet()) {
            int productId = entry.getKey();
            int quantity = entry.getValue();
            CashierModel.handlePurchase(productId, quantity, idStore);
        }

        cartItems.clear();
        return false;
    }

}


