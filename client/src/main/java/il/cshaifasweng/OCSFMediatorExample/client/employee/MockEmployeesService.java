package il.cshaifasweng.OCSFMediatorExample.client.employee;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MockEmployeesService {
    private final List<EmployeeVM> data = new ArrayList<>();

    public MockEmployeesService() {
        // sample rows
        data.add(new EmployeeVM(1001, "Noa Levi", "Female", "noa@flowershop.co", "052-123-4567",
                "Manager", true, 17800, LocalDate.of(2023, 2, 14)));
        data.add(new EmployeeVM(1002, "Omar Barakat", "Male", "omar@flowershop.co", "053-987-2211",
                "Florist", true, 11200, LocalDate.of(2024, 6, 1)));
        data.add(new EmployeeVM(1003, "Yael Cohen", "Female", "yael@flowershop.co", "050-444-9090",
                "Cashier", false, 9800, LocalDate.of(2022, 11, 3)));
        data.add(new EmployeeVM(1004, "Amit Peretz", "Male", "amit@flowershop.co", "054-321-7788",
                "Driver", true, 10100, LocalDate.of(2025, 3, 19)));
        data.add(new EmployeeVM(1005, "Rana Khoury", "Female", "rana@flowershop.co", "058-888-5577",
                "Admin", true, 12500, LocalDate.of(2021, 8, 9)));
    }

    public List<EmployeeVM> loadAll() {
        // return a shallow copy so Table edits won't affect backing unintentionally
        return new ArrayList<>(data);
    }

    public void deleteById(long id) {
        data.removeIf(e -> e.getId() == id);
    }
}
