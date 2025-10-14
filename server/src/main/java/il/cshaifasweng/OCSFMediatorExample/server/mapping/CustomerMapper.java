package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.dto.CustomerDTO;

public final class CustomerMapper {
    private CustomerMapper() {}

    public static CustomerDTO toDto(Customer c) {
        return (c == null) ? null : CustomerDTO.from(c);
    }
}
