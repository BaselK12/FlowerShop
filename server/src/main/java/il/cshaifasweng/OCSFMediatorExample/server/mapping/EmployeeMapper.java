package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.EmployeeRole;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Gender;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.domain.Employee;

import java.time.LocalDate;

public class EmployeeMapper {

    // ==== Entity -> DTO (read model) ====
    public static EmployeesDTO.Employee toDTO(Employee entity) {
        if (entity == null) return null;
        return new EmployeesDTO.Employee(
                entity.getId(),
                entity.getName(),
                entity.getGender(), // enum -> String
                entity.getEmail(),
                entity.getPhone(),
                entity.getRole(),   // pass enum directly (EmployeeRole in DTO)
                entity.isActive(),
                entity.getSalary(),
                entity.getHireDate()
        );
    }

    // ==== DTO -> Entity (create new employee) ====
    // hireDate set to "today" by default, passwordHash provided externally
    public static Employee fromCreate(EmployeesDTO.Create dto, String passwordHash) {
        if (dto == null) return null;
        return new Employee(
                dto.getName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getGender(),      // convert String -> Gender
                dto.getRole(),          // convert String -> EmployeeRole
                dto.isActive(),
                dto.getSalary(),
                LocalDate.now(),                   // hire date assigned automatically
                passwordHash                       // must be provided from outside
        );
    }

    // ==== DTO -> Entity (update existing employee) ====
    // keeps original hireDate and passwordHash (must be provided externally)
    public static Employee fromUpdate(EmployeesDTO.Update dto,
                                      String passwordHash,
                                      LocalDate existingHireDate) {
        if (dto == null) return null;
        Employee emp = new Employee(
                dto.getName(),
                dto.getEmail(),
                dto.getPhone(),
                dto.getGender(),      // convert String -> Gender
                dto.getRole(),          // convert String -> EmployeeRole
                dto.isActive(),
                dto.getSalary(),
                existingHireDate,                  // preserve existing hire date
                passwordHash
        );
        //emp.setId(dto.getId());  // IMPORTANT: set ID for updates
        return emp;
    }

    // ==== Helpers: normalize enums ====
    private static EmployeeRole parseRole(String role) {
        if (role == null || role.isBlank()) return null;
        return EmployeeRole.valueOf(role.trim().toUpperCase().replace(" ", "_"));
    }

    private static Gender parseGender(String gender) {
        if (gender == null || gender.isBlank()) return null;
        return Gender.valueOf(gender.trim().toUpperCase());
    }
}
