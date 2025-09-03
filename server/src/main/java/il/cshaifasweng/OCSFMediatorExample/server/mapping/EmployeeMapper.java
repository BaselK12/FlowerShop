package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO;
import il.cshaifasweng.OCSFMediatorExample.server.model.Employee;
import il.cshaifasweng.OCSFMediatorExample.server.model.Gender;
import il.cshaifasweng.OCSFMediatorExample.server.model.Role;

public final class EmployeeMapper {
    private EmployeeMapper() {}

    public static EmployeesDTO.Employee toDto(Employee e) {
        EmployeesDTO.Employee dto = new EmployeesDTO.Employee();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setGender(e.getGender().name());
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setRole(e.getRole().name());
        dto.setActive(e.isActive());
        dto.setSalary(e.getSalary());
        dto.setHireDate(e.getHireDate());
        return dto;
    }

    public static Employee fromDto(EmployeesDTO.Employee dto) {
        Employee e = new Employee();
        e.setId(dto.getId());
        e.setName(dto.getName());
        e.setGender(Gender.valueOf(dto.getGender().toUpperCase()));
        e.setEmail(dto.getEmail());
        e.setPhone(dto.getPhone());
        e.setRole(Role.valueOf(dto.getRole().toUpperCase()));
        e.setActive(dto.isActive());
        e.setSalary(dto.getSalary());
        e.setHireDate(dto.getHireDate());
        return e;
    }
}
