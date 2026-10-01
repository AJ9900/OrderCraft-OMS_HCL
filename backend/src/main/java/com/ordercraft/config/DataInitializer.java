package com.ordercraft.config;

import com.ordercraft.entity.*;
import com.ordercraft.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final BomRepository bomRepository;
    private final BomItemRepository bomItemRepository;
    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerOrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ordercraft.admin.username:admin}")
    private String adminUsername;

    @Value("${ordercraft.admin.password}")
    private String adminPassword;

    @Value("${ordercraft.admin.email:admin@ordercraft.com}")
    private String adminEmail;

    @Value("${ordercraft.admin.reset-password:false}")
    private boolean resetAdminPassword;

    public DataInitializer(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           ProductRepository productRepository,
                           BomRepository bomRepository,
                           BomItemRepository bomItemRepository,
                           InventoryRepository inventoryRepository,
                           SupplierRepository supplierRepository,
                           CustomerOrderRepository orderRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.bomRepository = bomRepository;
        this.bomItemRepository = bomItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.supplierRepository = supplierRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        seedInitialManufacturingData();
    }

    // ─── Users ────────────────────────────────────────────────────────────────
    private void seedUsers() {
        // Admin (configurable via env)
        if (!userRepository.existsByUsername(adminUsername)) {
            User admin = new User(adminUsername, passwordEncoder.encode(adminPassword),
                    adminEmail, "System Administrator", Role.ADMIN);
            userRepository.save(admin);
            log.info("Initialized default ADMIN user: {}", adminUsername);
        } else if (resetAdminPassword) {
            User admin = userRepository.findByUsername(adminUsername).orElseThrow();
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setActive(true);
            userRepository.save(admin);
            log.info("Reset ADMIN password for: {}", adminUsername);
        }

        // Demo role accounts — password is same as adminPassword for simplicity
        createUserIfAbsent("sales",       "sales@ordercraft.com",       "Priya Sharma",    Role.SALES_MANAGER);
        createUserIfAbsent("production",  "production@ordercraft.com",  "Raj Mehta",       Role.PRODUCTION_MANAGER);
        createUserIfAbsent("procurement", "procurement@ordercraft.com", "Ananya Gupta",    Role.PROCUREMENT_MANAGER);
        createUserIfAbsent("warehouse",   "warehouse@ordercraft.com",   "Suresh Kumar",    Role.WAREHOUSE_MANAGER);
        createUserIfAbsent("finance",     "finance@ordercraft.com",     "Kavita Reddy",    Role.FINANCE_MANAGER);
    }

    private void createUserIfAbsent(String username, String email, String fullName, Role role) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User(username, passwordEncoder.encode(adminPassword), email, fullName, role);
            userRepository.save(user);
            log.info("Created demo user: {} ({})", username, role);
        }
    }

    // ─── Manufacturing Data ───────────────────────────────────────────────────
    private void seedInitialManufacturingData() {
        if (productRepository.count() > 0) {
            log.info("Products already seeded — skipping demo data.");
            return;
        }

        log.info("Seeding realistic manufacturing demo data …");

        // ── Raw materials ────────────────────────────────────────────────────
        Product seat     = makeProduct("RM-SEAT-001", "Ergonomic Seat Pan",        "Moulded polyurethane ergonomic seat pan, black finish",         "Components",  "RAW_MATERIAL", bd(0), bd(18.50),  "PCS");
        Product frame    = makeProduct("RM-FRAME-001","Heavy-Duty Metal Frame",    "Steel reinforced swivel base with gas-lift socket",             "Metals",      "RAW_MATERIAL", bd(0), bd(35.00),  "PCS");
        Product wheel    = makeProduct("RM-WHEEL-001","Nylon Caster Wheel 50mm",   "Dual-wheel PU-coated caster, floor-friendly",                   "Hardware",    "RAW_MATERIAL", bd(0), bd(3.20),   "PCS");
        Product screw    = makeProduct("RM-SCREW-001","M6 Hex Bolt Set",           "High-tensile steel hex bolt & nut assortment",                  "Fasteners",   "RAW_MATERIAL", bd(0), bd(0.45),   "PCS");
        Product cushion  = makeProduct("RM-CUSH-001", "Breathable Mesh Cushion",   "High-density memory foam with breathable mesh fabric, lumbar",  "Fabrics",     "RAW_MATERIAL", bd(0), bd(12.00),  "PCS");
        Product armrest  = makeProduct("RM-ARM-001",  "Adjustable Armrest Pair",   "Height & width adjustable PP armrests with PU pad",             "Components",  "RAW_MATERIAL", bd(0), bd(9.80),   "SET");
        Product deskTop  = makeProduct("RM-TOP-001",  "MDF Desktop Panel 60x30\"", "18mm MDF with scratch-resistant laminate, walnut veneer",       "Wood & Board","RAW_MATERIAL", bd(0), bd(55.00),  "PCS");
        Product deskLeg  = makeProduct("RM-LEG-001",  "Steel Adjustable Leg",      "Square steel tube leg with levelling foot, powder-coated grey", "Metals",      "RAW_MATERIAL", bd(0), bd(22.00),  "PCS");
        Product deskMotor= makeProduct("RM-MOTOR-001","Dual Lift Motor Kit",        "Synchronised dual-motor actuator set for sit-stand desks",      "Electronics", "RAW_MATERIAL", bd(0), bd(48.00),  "SET");
        Product wireMgmt = makeProduct("RM-WIRE-001", "Cable Management Tray",     "Steel cable tray & plastic grommet set, desk-mount",            "Hardware",    "RAW_MATERIAL", bd(0), bd(6.50),   "SET");

        // ── Finished goods ───────────────────────────────────────────────────
        Product chair    = makeProduct("FG-CHAIR-001","Ergonomic Office Chair Deluxe","Premium swivel desk chair with lumbar support & mesh back",  "Furniture",   "FINISHED_GOOD", bd(149.99), bd(75.00), "PCS");
        Product desk     = makeProduct("FG-DESK-001", "Motorised Standing Desk 60×30","Dual-motor height-adjustable sit-stand desk, walnut",        "Furniture",   "FINISHED_GOOD", bd(399.00), bd(210.00),"PCS");

        // ── Inventory ────────────────────────────────────────────────────────
        //                  product   current  minStock  reorder  unitCost
        makeInventory(seat,     bd(80),  bd(20), bd(40),  bd(18.50));
        makeInventory(frame,    bd(45),  bd(15), bd(30),  bd(35.00)); // shortage scenario for 50-chair order
        makeInventory(wheel,    bd(350), bd(50), bd(100), bd(3.20));
        makeInventory(screw,    bd(800), bd(100),bd(200), bd(0.45));
        makeInventory(cushion,  bd(90),  bd(20), bd(35),  bd(12.00));
        makeInventory(armrest,  bd(60),  bd(10), bd(25),  bd(9.80));
        makeInventory(deskTop,  bd(30),  bd(5),  bd(15),  bd(55.00));
        makeInventory(deskLeg,  bd(55),  bd(8),  bd(20),  bd(22.00));
        makeInventory(deskMotor,bd(18),  bd(5),  bd(10),  bd(48.00));
        makeInventory(wireMgmt, bd(40),  bd(10), bd(20),  bd(6.50));
        makeInventory(chair,    bd(15),  bd(5),  bd(10),  bd(75.00));
        makeInventory(desk,     bd(8),   bd(2),  bd(5),   bd(210.00));

        // ── BOM: Office Chair ────────────────────────────────────────────────
        Bom chairBom = makeBom("BOM-CHAIR-V1", chair, "1.0", "ACTIVE", "Standard build — Ergonomic Office Chair Deluxe");
        addBomLine(chairBom, seat,    bd(1),  "PCS", bd(0));
        addBomLine(chairBom, frame,   bd(1),  "PCS", bd(0));
        addBomLine(chairBom, wheel,   bd(5),  "PCS", bd(0));
        addBomLine(chairBom, screw,   bd(10), "PCS", bd(2));
        addBomLine(chairBom, cushion, bd(1),  "PCS", bd(0));
        addBomLine(chairBom, armrest, bd(1),  "SET", bd(0));

        // ── BOM: Standing Desk ───────────────────────────────────────────────
        Bom deskBom = makeBom("BOM-DESK-V1", desk, "1.0", "ACTIVE", "Standard build — Motorised Standing Desk 60×30");
        addBomLine(deskBom, deskTop,   bd(1), "PCS", bd(0));
        addBomLine(deskBom, deskLeg,   bd(4), "PCS", bd(0));
        addBomLine(deskBom, deskMotor, bd(1), "SET", bd(0));
        addBomLine(deskBom, wireMgmt,  bd(1), "SET", bd(0));
        addBomLine(deskBom, screw,     bd(24),"PCS", bd(2));

        // ── Customers ────────────────────────────────────────────────────────
        Customer acme   = makeCustomer("CUST-1001","Acme Enterprise Corp",   "procurement@acme.com",    "+1-555-0199","100 Innovation Way",     "San Jose",    "CA","USA","95112");
        Customer zenith = makeCustomer("CUST-1002","Zenith Tech Hub",         "admin@zenithtech.io",     "+1-555-0245","450 Market St Suite 800", "Austin",      "TX","USA","78701");
        Customer nova   = makeCustomer("CUST-1003","Nova Office Solutions",   "orders@novaoffice.co.uk", "+44-20-7946-0958","22 Baker Street",   "London",      "London","UK","W1U 3BW");
        Customer vanta  = makeCustomer("CUST-1004","Vanta Workspaces Pvt Ltd","purchase@vantaws.in",     "+91-80-4567-8901","Plot 14 Electronics City","Bengaluru","KA","India","560100");
        Customer metro  = makeCustomer("CUST-1005","Metro Corporate Interiors","hello@metroci.com",      "+1-312-555-0177","230 S Wacker Dr Fl 22","Chicago",    "IL","USA","60606");

        // ── Suppliers ────────────────────────────────────────────────────────
        Supplier apex  = makeSupplier("SUPP-2001","Apex Steel & Frame Fabricators","sales@apexsteel.com",     "+1-555-3344","88 Industrial Blvd, Detroit MI", "US-EIN-9923841");
        Supplier poly  = makeSupplier("SUPP-2002","Polymatrix Plastics Ltd",       "orders@polymatrix.com",   "+1-555-8821","12 Polymer Park, Houston TX",     "US-EIN-4412903");
        Supplier fastco= makeSupplier("SUPP-2003","FastCo Hardware & Fasteners",   "supply@fastco.com",       "+1-555-6600","55 Bolt Way, Cleveland OH",        "US-EIN-7731042");

        // ── Customer Orders ──────────────────────────────────────────────────
        // Order 1: 50 chairs confirmed (has metal-frame shortage)
        CustomerOrder ord1 = makeOrder("ORD-2026-001", acme,   LocalDate.now(),              LocalDate.now().plusDays(14), OrderStatus.CONFIRMED,
                "Urgent batch for new Austin development campus");
        addOrderLine(ord1, chair, bd(50), bd(149.99), bd(0), bd(599.96));
        orderRepository.save(ord1);

        // Order 2: 10 desks confirmed
        CustomerOrder ord2 = makeOrder("ORD-2026-002", zenith, LocalDate.now().minusDays(3), LocalDate.now().plusDays(21), OrderStatus.CONFIRMED,
                "Q4 fit-out for new Austin office");
        addOrderLine(ord2, desk, bd(10), bd(399.00), bd(0), bd(319.20));
        orderRepository.save(ord2);

        // Order 3: 30 chairs for London (draft)
        CustomerOrder ord3 = makeOrder("ORD-2026-003", nova,   LocalDate.now().minusDays(1), LocalDate.now().plusDays(30), OrderStatus.DRAFT,
                "Baker Street corporate refurbishment");
        addOrderLine(ord3, chair, bd(30), bd(149.99), bd(150.00), bd(359.97));
        orderRepository.save(ord3);

        // Order 4: 5 desks (draft)
        CustomerOrder ord4 = makeOrder("ORD-2026-004", vanta,  LocalDate.now(),              LocalDate.now().plusDays(45), OrderStatus.DRAFT,
                "Bangalore electronics city showroom");
        addOrderLine(ord4, desk, bd(5), bd(399.00), bd(0), bd(159.60));
        addOrderLine(ord4, chair, bd(20), bd(149.99), bd(0), bd(239.98));
        orderRepository.save(ord4);

        // Order 5: 100 chairs completed (for revenue on dashboard)
        CustomerOrder ord5 = makeOrder("ORD-2025-088", metro, LocalDate.now().minusDays(60), LocalDate.now().minusDays(30), OrderStatus.COMPLETED,
                "Annual furniture procurement — completed successfully");
        addOrderLine(ord5, chair, bd(100), bd(149.99), bd(1000.00), bd(1199.92));
        orderRepository.save(ord5);

        log.info("Demo manufacturing data seeded successfully — products, BOMs, customers, suppliers, orders.");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private BigDecimal bd(double val) { return BigDecimal.valueOf(val); }

    private Product makeProduct(String code, String name, String desc, String cat, String type,
                                BigDecimal selling, BigDecimal cost, String unit) {
        Product p = new Product();
        p.setProductCode(code); p.setProductName(name); p.setDescription(desc);
        p.setCategory(cat); p.setType(type);
        p.setSellingPrice(selling); p.setCostPrice(cost);
        p.setUnit(unit); p.setStatus("ACTIVE");
        return productRepository.save(p);
    }

    private void makeInventory(Product product, BigDecimal current, BigDecimal min, BigDecimal reorder, BigDecimal cost) {
        inventoryRepository.save(new Inventory(product, current, min, reorder, cost));
    }

    private Bom makeBom(String code, Product product, String version, String status, String notes) {
        Bom b = new Bom();
        b.setBomCode(code); b.setProduct(product); b.setVersion(version);
        b.setStatus(status); b.setEffectiveDate(LocalDate.now()); b.setNotes(notes);
        return bomRepository.save(b);
    }

    private void addBomLine(Bom bom, Product material, BigDecimal qty, String unit, BigDecimal wastage) {
        BomItem bi = new BomItem();
        bi.setBom(bom); bi.setMaterial(material); bi.setQuantity(qty);
        bi.setUnit(unit); bi.setWastagePercentage(wastage);
        bomItemRepository.save(bi);
    }

    private Customer makeCustomer(String code, String name, String email, String phone,
                                   String address, String city, String state, String country, String postal) {
        Customer c = new Customer();
        c.setCustomerCode(code); c.setName(name); c.setEmail(email); c.setPhone(phone);
        c.setAddress(address); c.setCity(city); c.setState(state); c.setCountry(country);
        c.setPostalCode(postal); c.setStatus("ACTIVE");
        return customerRepository.save(c);
    }

    private Supplier makeSupplier(String code, String name, String email, String phone, String address, String tax) {
        Supplier s = new Supplier();
        s.setSupplierCode(code); s.setSupplierName(name); s.setEmail(email);
        s.setPhone(phone); s.setAddress(address); s.setTaxIdentifier(tax); s.setStatus("ACTIVE");
        return supplierRepository.save(s);
    }

    private CustomerOrder makeOrder(String number, Customer customer, LocalDate orderDate,
                                     LocalDate deliveryDate, OrderStatus status, String notes) {
        CustomerOrder o = new CustomerOrder();
        o.setOrderNumber(number); o.setCustomer(customer);
        o.setOrderDate(orderDate); o.setExpectedDeliveryDate(deliveryDate);
        o.setStatus(status); o.setNotes(notes);
        o.setTotalAmount(BigDecimal.ZERO); o.setTaxAmount(BigDecimal.ZERO); o.setGrandTotal(BigDecimal.ZERO);
        return o;
    }

    private void addOrderLine(CustomerOrder order, Product product, BigDecimal qty,
                               BigDecimal unitPrice, BigDecimal discount, BigDecimal tax) {
        OrderItem item = new OrderItem();
        item.setProduct(product); item.setQuantity(qty); item.setUnitPrice(unitPrice);
        item.setDiscount(discount); item.setTax(tax);
        BigDecimal lineTotal = qty.multiply(unitPrice).subtract(discount).add(tax);
        item.setTotal(lineTotal);
        order.addItem(item);
        order.setTotalAmount(order.getTotalAmount().add(qty.multiply(unitPrice)));
        order.setTaxAmount(order.getTaxAmount().add(tax));
        order.setGrandTotal(order.getGrandTotal().add(lineTotal));
    }
}
