package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.EmployeesDTO;
import il.cshaifasweng.OCSFMediatorExample.server.model.Employee;

public final class EmployeeMapper {
    private EmployeeMapper() {}

    // Entity -> Read DTO
    public static EmployeesDTO.Employee toDto(Employee e) {
        EmployeesDTO.Employee dto = new EmployeesDTO.Employee();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setGender(e.getGender());           // String now
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setRole(e.getRole());               // String now
        dto.setActive(e.isActive());
        dto.setSalary(e.getSalary());
        dto.setHireDate(e.getHireDate());
        return dto;
    }

    // Read DTO -> Entity (full copy)
    public static Employee fromDto(EmployeesDTO.Employee dto) {
        Employee e = new Employee();
        e.setId(dto.getId());
        e.setName(dto.getName());
        e.setGender(safe(dto.getGender()));
        e.setEmail(dto.getEmail());
        e.setPhone(dto.getPhone());
        e.setRole(safe(dto.getRole()));
        e.setActive(dto.isActive());
        e.setSalary(dto.getSalary());
        e.setHireDate(dto.getHireDate());
        return e;
    }

    // Create DTO -> Entity
    public static Employee fromCreate(EmployeesDTO.Create c) {
        Employee e = new Employee();
        e.setName(c.getName());
        e.setGender(safe(c.getGender()));
        e.setEmail(c.getEmail());
        e.setPhone(c.getPhone());
        e.setRole(safe(c.getRole()));
        e.setActive(c.isActive());
        e.setSalary(c.getSalary());
        return e;
    }

    // Update DTO -> apply partial changes to existing entity
    public static void applyUpdate(Employee e, EmployeesDTO.Update u) {
        if (u.getName()  != null) e.setName(u.getName());
        if (u.getGender()!= null) e.setGender(safe(u.getGender()));
        if (u.getEmail() != null) e.setEmail(u.getEmail());
        if (u.getPhone() != null) e.setPhone(u.getPhone());
        if (u.getRole()  != null) e.setRole(safe(u.getRole()));
        e.setActive(u.isActive());
        e.setSalary(u.getSalary());
    }

    private static String safe(String s) {
        return s == null ? null : s.trim();
    }
}
