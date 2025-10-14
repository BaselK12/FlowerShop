package il.cshaifasweng.OCSFMediatorExample.server.handlers.account;

import il.cshaifasweng.OCSFMediatorExample.entities.domain.Customer;
import il.cshaifasweng.OCSFMediatorExample.entities.dto.CustomerDTO;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewResponse;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.UpdateCustomerProfileResponse;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SendToClientEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.AccountOverviewRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.UpdateCustomerProfileRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.session.TX;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;


public class AccountHandler {
    private final ServerBus bus;

    public AccountHandler(ServerBus bus) {
        this.bus = bus;

        // Overview
        bus.subscribe(AccountOverviewRequestedEvent.class, this::onOverview);

        // Update
        bus.subscribe(UpdateCustomerProfileRequestedEvent.class, this::onUpdateProfile);
    }

    /* ===== Handlers ===== */

    private void onOverview(AccountOverviewRequestedEvent evt) {
        // 1) decide the effective id WITHOUT mutating a captured var
        final long effectiveId;
        final long requestedId = evt.getRequest().getCustomerId();

        if (requestedId > 0) {
            effectiveId = requestedId;
        } else {
            Long mapped = il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry.get(evt.getClient());
            if (mapped == null) {
                send(evt.getClient(), AccountOverviewResponse.fail("Not logged in"));
                return;
            }
            effectiveId = mapped;
        }

        // 2) use the final var inside the lambda
        CustomerDTO dto = TX.call(s -> {
            Customer c = s.get(Customer.class, effectiveId);
            return (c == null) ? null : CustomerDTO.from(c);
        });

        // 3) respond
        if (dto == null) {
            send(evt.getClient(), AccountOverviewResponse.fail("Customer not found"));
        } else {
            send(evt.getClient(), AccountOverviewResponse.success(dto));
        }
    }



    private void onUpdateProfile(UpdateCustomerProfileRequestedEvent evt) {
        var r = evt.getRequest();

        // resolve effective id (allow 0 = infer from socket)
        final long requestedId = r.getCustomerId();
        final long effectiveId;
        if (requestedId > 0) {
            effectiveId = requestedId;
        } else {
            Long mapped = SessionRegistry.get(evt.getClient());
            if (mapped == null) {
                send(evt.getClient(), UpdateCustomerProfileResponse.fail("Not logged in"));
                return;
            }
            effectiveId = mapped;
        }

        final String newName  = safe(r.getDisplayName());
        final String newEmail = safe(r.getEmail());
        final String newPhone = safe(r.getPhone());

        if (newName.isEmpty() || newEmail.isEmpty()) {
            send(evt.getClient(), UpdateCustomerProfileResponse.fail("Name and email are required"));
            return;
        }

        try {
            CustomerDTO updated = TX.call(s -> {
                Customer c = s.get(Customer.class, effectiveId);
                if (c == null) return null;

                // unique email among active customers
                Long dup = s.createQuery(
                                "select count(c.id) from Customer c " +
                                        "where c.email = :email and c.id <> :id and c.active = true", Long.class)
                        .setParameter("email", newEmail)
                        .setParameter("id", effectiveId)
                        .uniqueResult();

                if (dup != null && dup > 0) {
                    throw new IllegalStateException("Email already in use");
                }

                c.setDisplayName(newName);
                c.setEmail(newEmail);
                c.setPhone(newPhone);
                s.merge(c);

                return CustomerDTO.from(c);
            });

            if (updated == null) {
                send(evt.getClient(), UpdateCustomerProfileResponse.fail("Customer not found"));
            } else {
                send(evt.getClient(), UpdateCustomerProfileResponse.success(updated));
            }
        } catch (IllegalStateException dupe) {
            send(evt.getClient(), UpdateCustomerProfileResponse.fail(dupe.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            send(evt.getClient(), UpdateCustomerProfileResponse.fail("Server error"));
        }
    }


    /* ===== Helpers ===== */

    private void send(ConnectionToClient client, Object payload) {
        bus.publish(new SendToClientEvent(payload, client));
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
}
