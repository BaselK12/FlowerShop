package il.cshaifasweng.OCSFMediatorExample.entities.converters;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Gender;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, String> {

    @Override
    public String convertToDatabaseColumn(Gender gender) {
        if (gender == null) return null;
        return switch (gender) {
            case Male   -> "Male";
            case Female -> "Female";
            case Other  -> "Other";
        };
    }

    @Override
    public Gender convertToEntityAttribute(String dbValue) {
        if (dbValue == null) return null;
        return switch (dbValue.toLowerCase()) {
            case "male"   -> Gender.Male;
            case "female" -> Gender.Female;
            case "other"  -> Gender.Other;
            default -> throw new IllegalArgumentException("Unknown gender: " + dbValue);
        };
    }
}