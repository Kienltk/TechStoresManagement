package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.GeneralModel;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;

public class DateComboBoxExample extends Application {

    private ComboBox<String> criteriaComboBox;
    private ComboBox<Integer> monthComboBox;
    private ComboBox<Integer> yearComboBox;
    private GeneralModel generalModel = new GeneralModel();
    private Map<Integer, Map<String, BigDecimal>> financialData;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        criteriaComboBox = new ComboBox<>();
        criteriaComboBox.getItems().addAll("Ngày", "Tháng", "Năm");
        criteriaComboBox.setValue("Tháng");

        // ComboBox cho tháng (1-12)
        monthComboBox = new ComboBox<>();
        for (int i = 1; i <= 12; i++) {
            monthComboBox.getItems().add(i);
        }

        // ComboBox cho năm, lấy từ cơ sở dữ liệu
        yearComboBox = new ComboBox<>();
        yearComboBox.getItems().addAll(generalModel.getAvailableYears());

        // Đặt giá trị cho tháng và năm mặc định
        monthComboBox.setValue(Calendar.getInstance().get(Calendar.MONTH) + 1); // Tháng hiện tại
        yearComboBox.setValue(Calendar.getInstance().get(Calendar.YEAR)); // Năm hiện tại

        // In ra tiêu chí mặc định ngay lập tức
        if (criteriaComboBox.getValue().equals("Tháng")) {
            monthComboBox.setVisible(false); // Ẩn ComboBox tháng
        }
        System.out.println("Tiêu chí: Tháng - Năm: " + yearComboBox.getValue());

        criteriaComboBox.setOnAction(event -> handleCriteriaChange());
        yearComboBox.setOnAction(event -> handleCriteriaChange());
        monthComboBox.setOnAction(event -> handleCriteriaChange());

        VBox layout = new VBox(10);
        layout.getChildren().addAll(new Label("Chọn tiêu chí:"), criteriaComboBox, monthComboBox, yearComboBox);
        Scene scene = new Scene(layout, 300, 200);

        primaryStage.setTitle("Date ComboBox Example");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Gọi hàm để xử lý khi khởi động
        initializeData();
    }

    // Hàm xử lý khởi tạo ban đầu
    private void initializeData() {
        // Lấy dữ liệu ngay khi chương trình khởi chạy dựa trên tiêu chí mặc định
        financialData = generalModel.getFinancialDataByMonth(yearComboBox.getValue());
        System.out.println(financialData); // In ra dữ liệu ban đầu
    }

    private void handleCriteriaChange() {
        String selectedCriteria = criteriaComboBox.getValue();

        switch (selectedCriteria) {
            case "Tháng":
                monthComboBox.setVisible(false); // Ẩn ComboBox tháng
                yearComboBox.setVisible(true); // Hiện ComboBox năm
                System.out.println("Tiêu chí: Tháng");

                // Cập nhật dữ liệu khi thay đổi năm
                yearComboBox.setOnAction(event -> {
                    System.out.println("Tiêu chí: Tháng - Năm: " + yearComboBox.getValue());
                    financialData = generalModel.getFinancialDataByMonth(yearComboBox.getValue());
                    System.out.println(financialData);
                });
                // Gọi lại để xử lý khi chương trình khởi động
                financialData = generalModel.getFinancialDataByMonth(yearComboBox.getValue());
                System.out.println(financialData);
                break;

            case "Năm":
                monthComboBox.setVisible(false); // Ẩn ComboBox tháng
                yearComboBox.setVisible(false); // Ẩn ComboBox năm
                System.out.println("Tiêu chí: Năm");
                financialData = generalModel.getFinancialDataByYear();
                System.out.println(financialData);
                break;

            case "Ngày":
                monthComboBox.setVisible(true); // Hiện ComboBox tháng
                yearComboBox.setVisible(true); // Hiện ComboBox năm
                System.out.println("Tiêu chí: Ngày - Tháng: " + monthComboBox.getValue() + " - Năm: " + yearComboBox.getValue());

                // Cập nhật dữ liệu khi thay đổi tháng hoặc năm
                yearComboBox.setOnAction(event -> {
                    System.out.println("Tiêu chí: Ngày - Tháng: " + monthComboBox.getValue() + " - Năm: " + yearComboBox.getValue());
                    financialData = generalModel.getFinancialDataByDay(yearComboBox.getValue(), monthComboBox.getValue());
                    System.out.println(financialData);
                });
                monthComboBox.setOnAction(event -> {
                    System.out.println("Tiêu chí: Ngày - Tháng: " + monthComboBox.getValue() + " - Năm: " + yearComboBox.getValue());
                    financialData = generalModel.getFinancialDataByDay(yearComboBox.getValue(), monthComboBox.getValue());
                    System.out.println(financialData);
                });

                // Gọi lại khi khởi chạy để lấy dữ liệu ngay lập tức
                financialData = generalModel.getFinancialDataByDay(yearComboBox.getValue(), monthComboBox.getValue());
                System.out.println(financialData);
                break;
        }
    }


}
