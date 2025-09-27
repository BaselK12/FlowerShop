package il.cshaifasweng.OCSFMediatorExample.entities.converters;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.EmployeeRole;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<EmployeeRole, String> {

    @Override
    public String convertToDatabaseColumn(EmployeeRole role) {
        if (role == null) return null;
        return switch (role) {
            case STORE_MANAGER -> "Store Manager";
            case CASHIER       -> "Cashier";
            case FLORIST       -> "Florist";
            case DELIVERY      -> "Delivery";
            case OTHER         -> "Other";
        };
    }

    @Override
    public EmployeeRole convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return switch (dbValue.toLowerCase()) {
            case "store manager" -> EmployeeRole.STORE_MANAGER;
            case "cashier"       -> EmployeeRole.CASHIER;
            case "florist"       -> EmployeeRole.FLORIST;
            case "delivery"      -> EmployeeRole.DELIVERY;
            case "other"         -> EmployeeRole.OTHER;
            default -> throw new IllegalArgumentException("Unknown role: " + dbValue);
        };
    }
}
