package il.cshaifasweng.OCSFMediatorExample.server;

import il.cshaifasweng.OCSFMediatorExample.entities.messages.GetOrdersRequest;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.GetOrdersRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.GetCouponsRequest;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.GetCouponsRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.DeleteFlowerRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.AdminDashboard.SaveFlowerRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCatalogRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetCategoriesRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Catalog.GetPromotionsRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CheckOut.ConfirmRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.GetComplaintsRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.CreateBouquet.GetFlowersRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.CreateEmployeeRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.DeleteEmployeeRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.GetEmployeesRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Employee.UpdateEmployeeRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.RegisterRequest;
import il.cshaifasweng.OCSFMediatorExample.server.bus.ServerBus;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.*;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Catalog.GetCatalogRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Catalog.GetCategoriesRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Catalog.GetPromotionsRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Flowers.DeleteFlowerRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Flowers.GetFlowersRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Flowers.SaveFlowerRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ObservableServer;
import il.cshaifasweng.OCSFMediatorExample.server.ocsf.ConnectionToClient;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AccountOverviewRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.UpdateCustomerProfileRequest;
import il.cshaifasweng.OCSFMediatorExample.server.session.SessionRegistry;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.GetPaymentsRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.AddPaymentRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Account.RemovePaymentRequest;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.GetPaymentsRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.AddPaymentRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.RemovePaymentRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.GetCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.AddToCartRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.CartUpdateRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.ContinueShoppingRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Cart.CheckoutRequest;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart.GetCartRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart.AddToCartRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart.CartUpdateRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart.ContinueShoppingRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Cart.CheckoutRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.SubmitComplaintRequest;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.SubmitComplaintRequestEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Complaint.UpdateComplaintRequest;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.UpdateComplaintRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetStoresRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.Reports.GetReportRequest;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Reports.GetStoresRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Reports.GetReportRequestedEvent;






import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.AccountOverviewRequestedEvent;
import il.cshaifasweng.OCSFMediatorExample.server.bus.events.Account.UpdateCustomerProfileRequestedEvent;


// your existing entities/messages
import il.cshaifasweng.OCSFMediatorExample.entities.messages.LoginRequest;
import il.cshaifasweng.OCSFMediatorExample.entities.messages.ErrorResponse;

public class SimpleServer extends ObservableServer {
	private final ServerBus bus;

	public SimpleServer(int port, ServerBus bus) {
		super(port);
		this.bus = bus;
	}

	@Override
	protected synchronized void clientDisconnected(ConnectionToClient client) {
		try {
			// remove the numeric id mapping if any
			SessionRegistry.clear(client);
		} catch (Exception ignored) {}

		try {
			// THIS is the missing piece: drop the username<->session mapping
			il.cshaifasweng.OCSFMediatorExample.server.session.SessionManager.get().logout(client);
		} catch (Exception ignored) {}

		super.clientDisconnected(client);
	}



	@Override
	protected void handleMessageFromClient(Object msg, ConnectionToClient client) {
		try {
			if (msg instanceof String s) {
				switch (s) {
					case "CustomerLoginPage Back" -> bus.publish(new CustomerLoginNavEvent("BACK", client));
					case "CustomerLoginPage register" -> bus.publish(new CustomerLoginNavEvent("REGISTER", client));
					case "register back" -> bus.publish(new CustomerLoginNavEvent("BACK", client)); // <-- add this for consistency
					default -> bus.publish(new SendToClientEvent(
							new ErrorResponse("Unknown command: " + s), client));
				}
			} else if (msg instanceof GetStoresRequest rr) {
				bus.publish(new GetStoresRequestedEvent(rr, client));
			} else if (msg instanceof GetReportRequest rr) {
				bus.publish(new GetReportRequestedEvent(rr, client));
			} else if (msg instanceof UpdateComplaintRequest rr) {
				bus.publish(new UpdateComplaintRequestedEvent(rr, client));
			} else if (msg instanceof SubmitComplaintRequest req) {
				bus.publish(new SubmitComplaintRequestEvent(req, client));
			} else if (msg instanceof GetCartRequest req) {
				bus.publish(new GetCartRequestedEvent(req, client));
			} else if (msg instanceof AddToCartRequest req) {
				bus.publish(new AddToCartRequestedEvent(req, client));
			} else if (msg instanceof CartUpdateRequest req) {
				bus.publish(new CartUpdateRequestedEvent(req, client));
			} else if (msg instanceof ContinueShoppingRequest req) {
				bus.publish(new ContinueShoppingRequestedEvent(req, client));
			} else if (msg instanceof CheckoutRequest req) {
				bus.publish(new CheckoutRequestedEvent(req, client));
			} else if (msg instanceof GetCouponsRequest rr) {
				bus.publish(new GetCouponsRequestedEvent(rr, client));
			} else if (msg instanceof GetPaymentsRequest r) {
				bus.publish(new GetPaymentsRequestedEvent(r, client));
			} else if (msg instanceof AddPaymentRequest r) {
				bus.publish(new AddPaymentRequestedEvent(r, client));
			} else if (msg instanceof RemovePaymentRequest r) {
				bus.publish(new RemovePaymentRequestedEvent(r, client));
			} else if (msg instanceof GetOrdersRequest rr) {
				bus.publish(new GetOrdersRequestedEvent(rr, client));
			} else if (msg instanceof AccountOverviewRequest rr) {
				bus.publish(new AccountOverviewRequestedEvent(rr, client));
			} else if (msg instanceof UpdateCustomerProfileRequest rr) {
				bus.publish(new UpdateCustomerProfileRequestedEvent(rr, client));
			} else if (msg instanceof LoginRequest lr) {
				bus.publish(new LoginRequestedEvent(lr, client));
			} else if (msg instanceof RegisterRequest rr) {
				bus.publish(new RegisterRequestedEvent(rr, client));
			} else if (msg instanceof GetEmployeesRequest rr) {
				bus.publish(new EmployeesFetchRequestedEvent(rr, client));
			} else if (msg instanceof CreateEmployeeRequest rr) {
				bus.publish(new EmployeeCreateRequestedEvent(rr, client));
			} else if (msg instanceof UpdateEmployeeRequest rr) {
				bus.publish(new EmployeeUpdateRequestedEvent(rr, client));
			}else if (msg instanceof DeleteEmployeeRequest rr) {
				bus.publish(new EmployeeDeleteRequestedEvent(rr, client));
			}else if (msg instanceof GetCatalogRequest rr) {
				bus.publish(new GetCatalogRequestEvent(rr, client));
			}else if (msg instanceof GetPromotionsRequest rr) {
				bus.publish(new GetPromotionsRequestEvent(rr, client));
			}else if (msg instanceof GetCategoriesRequest rr) {
				bus.publish(new GetCategoriesRequestEvent(rr, client));
			}else if (msg instanceof ConfirmRequest rr) {
				bus.publish(new ConfirmRequestEvent(rr, client));
			}else if (msg instanceof GetFlowersRequest rr) {
				bus.publish(new GetFlowersRequestEvent(rr, client));
			}else if (msg instanceof DeleteFlowerRequest rr) {
				bus.publish(new DeleteFlowerRequestEvent(rr, client));
			}else if (msg instanceof SaveFlowerRequest rr) {
				bus.publish(new SaveFlowerRequestEvent(rr, client));
			}
			else if (msg instanceof GetComplaintsRequest rr) {
				bus.publish(new ComplaintsFetchRequestedEvent(
						rr.getStatus(),
						rr.getType(),
						// You currently pass storeName (string). Later you might resolve this to storeId (Long).
						null,               // storeId (if you only have name, set null for now or add lookup)
						null,               // customerId
						null,               // orderId
						null,               // from
						null,               // to
						null,               // free text search
						"createdAt", true,  // sort by createdAt desc
						0,                  // page
						50,                 // pageSize
						client              // <-- ConnectionToClient
				));
			} else if (msg instanceof String s) {
				switch (s) {

					case "EMPLOYEES_OPEN_EDITOR:NEW" ->
							bus.publish(new EmployeesOpenEditorEvent(
									EmployeesOpenEditorEvent.EditorMode.NEW, null, client));

					default -> {
						if (s.startsWith("EMPLOYEES_OPEN_EDITOR:EDIT:")) {
							var idStr = s.substring("EMPLOYEES_OPEN_EDITOR:EDIT:".length());
							try {
								long id = Long.parseLong(idStr);
								bus.publish(new EmployeesOpenEditorEvent(
										EmployeesOpenEditorEvent.EditorMode.EDIT, id, client));
							} catch (NumberFormatException e) {
								bus.publish(new SendToClientEvent(
										new ErrorResponse("Bad employee id: " + idStr), client));
							}
						}  else {
							bus.publish(new SendToClientEvent(new ErrorResponse("Unknown command: " + s), client));
						}
					}
				}
			}else {
				System.out.printf("[Server] got no handler for msg: %s\n", msg);
			}
		} catch (Exception e) {
			bus.publish(new SendToClientEvent(
					new ErrorResponse("Server error: " + e.getMessage()), client));
		}
	}
}
