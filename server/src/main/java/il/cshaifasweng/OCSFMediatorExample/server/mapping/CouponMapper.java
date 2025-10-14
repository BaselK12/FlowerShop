package il.cshaifasweng.OCSFMediatorExample.server.mapping;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Coupon;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.CouponDTO;

public final class CouponMapper {
    private CouponMapper() {}
    public static CouponDTO toDTO(Coupon c) {
        return new CouponDTO(
                c.getId(),
                c.getCode(),
                c.getTitle(),
                c.getDescription(),
                c.getExpiration(),
                c.isActiveToday()
        );
    }
}
