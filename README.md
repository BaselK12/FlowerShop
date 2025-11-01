FlowerShop

FlowerShop is a full client–server e-commerce application for managing flower store operations.  
It was built as part of my university coursework to simulate a real multi-branch shop system.

Overview
The project includes a JavaFX client and a Java server communicating through the OCSF framework.  
It supports multiple stores, product catalogs, user accounts, shopping carts, and checkout flows,  
with a MySQL database managed by Hibernate and Flyway.

Main Features
- Multi-store support (Haifa, Tel Aviv, Jerusalem, Beersheba)
- Catalog with flowers, promotions, and categories
- Shopping cart with live quantity and price updates
- Checkout with delivery or pickup options, full validation, and order review
- User account with order history, coupons, and premium discounts
- Event-driven updates between client and server using EventBus

What I Implemented
- EventBus-based communication between JavaFX and the server  
- Complete Cart and Checkout flows (including validation and order confirmation)  
- Flyway migrations for stores, promotions, and flowers  
- Premium account logic and coupon validation  
- Central navigation system (`Nav.go`) for FXML screens  
- Fixes for JavaFX threading and UI refresh issues  

Technologies
JavaFX, OCSF, EventBus, Hibernate, MySQL, Flyway, Maven

This was a group project made by me and friends from Uni
