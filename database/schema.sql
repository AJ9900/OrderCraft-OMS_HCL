-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: ordercraft
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(100) NOT NULL,
  `entity_id` varchar(100) DEFAULT NULL,
  `entity_name` varchar(100) NOT NULL,
  `module` varchar(100) NOT NULL,
  `new_value` text,
  `old_value` text,
  `timestamp` datetime(6) NOT NULL,
  `username` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `bom_items`
--

DROP TABLE IF EXISTS `bom_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bom_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` decimal(12,4) NOT NULL,
  `unit` varchar(20) NOT NULL,
  `wastage_percentage` decimal(5,2) NOT NULL,
  `bom_id` bigint NOT NULL,
  `material_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK61p0elxqsv3ek2hyxoqknoj4q` (`bom_id`),
  KEY `FKebp79i2bnu3b5lia090r33ps0` (`material_id`),
  CONSTRAINT `FK61p0elxqsv3ek2hyxoqknoj4q` FOREIGN KEY (`bom_id`) REFERENCES `boms` (`id`),
  CONSTRAINT `FKebp79i2bnu3b5lia090r33ps0` FOREIGN KEY (`material_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `boms`
--

DROP TABLE IF EXISTS `boms`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `boms` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bom_code` varchar(50) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `effective_date` date NOT NULL,
  `notes` text,
  `status` varchar(30) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `version` varchar(20) NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3ubir9h76bmsu34nyw0w5syb6` (`bom_code`),
  KEY `FKmhxh3n0l9b3wtu7mdi0iwt5sa` (`product_id`),
  CONSTRAINT `FKmhxh3n0l9b3wtu7mdi0iwt5sa` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customer_orders`
--

DROP TABLE IF EXISTS `customer_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `expected_delivery_date` date NOT NULL,
  `grand_total` decimal(14,2) NOT NULL,
  `notes` text,
  `order_date` date NOT NULL,
  `order_number` varchar(50) NOT NULL,
  `status` enum('CANCELLED','COMPLETED','CONFIRMED','DRAFT','IN_PRODUCTION','MATERIAL_CHECK','MATERIAL_SHORTAGE','QUALITY_CHECK','READY_FOR_PRODUCTION') NOT NULL,
  `tax_amount` decimal(14,2) NOT NULL,
  `total_amount` decimal(14,2) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKs4wt1sgd48rj6cgahwlksogx` (`order_number`),
  KEY `FKiwewyud8svmsotwucvm94aaw8` (`customer_id`),
  CONSTRAINT `FKiwewyud8svmsotwucvm94aaw8` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `city` varchar(50) NOT NULL,
  `country` varchar(50) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `customer_code` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `name` varchar(100) NOT NULL,
  `phone` varchar(30) NOT NULL,
  `postal_code` varchar(20) NOT NULL,
  `state` varchar(50) NOT NULL,
  `status` varchar(30) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKiqv746oh5t5is1vr4p2nl79r6` (`customer_code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `goods_receipt_items`
--

DROP TABLE IF EXISTS `goods_receipt_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_receipt_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `received_quantity` decimal(12,2) NOT NULL,
  `goods_receipt_id` bigint NOT NULL,
  `po_item_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKel2i3xb291r13xh5o28hwhqc6` (`goods_receipt_id`),
  KEY `FK8mgu4uohgx4yqaknj9a7osshp` (`po_item_id`),
  CONSTRAINT `FK8mgu4uohgx4yqaknj9a7osshp` FOREIGN KEY (`po_item_id`) REFERENCES `po_items` (`id`),
  CONSTRAINT `FKel2i3xb291r13xh5o28hwhqc6` FOREIGN KEY (`goods_receipt_id`) REFERENCES `goods_receipts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `goods_receipts`
--

DROP TABLE IF EXISTS `goods_receipts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_receipts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `notes` text,
  `receipt_date` date NOT NULL,
  `receipt_number` varchar(50) NOT NULL,
  `received_by` varchar(50) NOT NULL,
  `po_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmfasptxi0jgu968w81s5f89mg` (`receipt_number`),
  KEY `FKilcn31flt39e8o7xb397pxfcl` (`po_id`),
  CONSTRAINT `FKilcn31flt39e8o7xb397pxfcl` FOREIGN KEY (`po_id`) REFERENCES `purchase_orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inventories`
--

DROP TABLE IF EXISTS `inventories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `available_quantity` decimal(12,2) NOT NULL,
  `current_quantity` decimal(12,2) NOT NULL,
  `minimum_stock` decimal(12,2) NOT NULL,
  `reorder_level` decimal(12,2) NOT NULL,
  `reserved_quantity` decimal(12,2) NOT NULL,
  `unit_cost` decimal(12,2) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK3bms153u88bt0daekfy4qpj6k` (`product_id`),
  CONSTRAINT `FK8drmqyx629j3oo8ct9jnc5y3y` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `invoices`
--

DROP TABLE IF EXISTS `invoices`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `invoices` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `discount` decimal(14,2) NOT NULL,
  `due_date` date NOT NULL,
  `grand_total` decimal(14,2) NOT NULL,
  `invoice_date` date NOT NULL,
  `invoice_number` varchar(50) NOT NULL,
  `paid_amount` decimal(14,2) NOT NULL,
  `status` enum('CANCELLED','DRAFT','ISSUED','OVERDUE','PAID','PARTIALLY_PAID') NOT NULL,
  `subtotal` decimal(14,2) NOT NULL,
  `tax` decimal(14,2) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKl1x55mfsay7co0r3m9ynvipd5` (`invoice_number`),
  KEY `FKq2w4hmh6l9othnp6cepp0cfe2` (`customer_id`),
  KEY `FKikmjf7v4ev27fg39yqxmaosyc` (`order_id`),
  CONSTRAINT `FKikmjf7v4ev27fg39yqxmaosyc` FOREIGN KEY (`order_id`) REFERENCES `customer_orders` (`id`),
  CONSTRAINT `FKq2w4hmh6l9othnp6cepp0cfe2` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `discount` decimal(12,2) NOT NULL,
  `quantity` decimal(12,2) NOT NULL,
  `tax` decimal(12,2) NOT NULL,
  `total` decimal(14,2) NOT NULL,
  `unit_price` decimal(12,2) NOT NULL,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb2vrrqy10nnyqhb5ergl5498r` (`order_id`),
  KEY `FKocimc7dtr037rh4ls4l95nlfi` (`product_id`),
  CONSTRAINT `FKb2vrrqy10nnyqhb5ergl5498r` FOREIGN KEY (`order_id`) REFERENCES `customer_orders` (`id`),
  CONSTRAINT `FKocimc7dtr037rh4ls4l95nlfi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(14,2) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `notes` text,
  `payment_date` date NOT NULL,
  `payment_method` enum('BANK_TRANSFER','CARD','CASH','OTHER','UPI') NOT NULL,
  `payment_number` varchar(50) NOT NULL,
  `status` varchar(30) NOT NULL,
  `transaction_reference` varchar(100) DEFAULT NULL,
  `invoice_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKc6nxg52ow66u8ut91bytspy64` (`payment_number`),
  KEY `FKrbqec6be74wab8iifh8g3i50i` (`invoice_id`),
  CONSTRAINT `FKrbqec6be74wab8iifh8g3i50i` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `po_items`
--

DROP TABLE IF EXISTS `po_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `po_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` decimal(12,2) NOT NULL,
  `received_quantity` decimal(12,2) NOT NULL,
  `tax` decimal(12,2) NOT NULL,
  `total` decimal(14,2) NOT NULL,
  `unit_price` decimal(12,2) NOT NULL,
  `material_id` bigint NOT NULL,
  `po_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk67opso97mfqdwchx2eyxyrx8` (`material_id`),
  KEY `FK37315ux53889ffx11tm5it1hm` (`po_id`),
  CONSTRAINT `FK37315ux53889ffx11tm5it1hm` FOREIGN KEY (`po_id`) REFERENCES `purchase_orders` (`id`),
  CONSTRAINT `FKk67opso97mfqdwchx2eyxyrx8` FOREIGN KEY (`material_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `production_orders`
--

DROP TABLE IF EXISTS `production_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `production_orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `notes` text,
  `planned_quantity` decimal(12,2) NOT NULL,
  `produced_quantity` decimal(12,2) NOT NULL,
  `production_order_number` varchar(50) NOT NULL,
  `start_date` date DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','IN_PROGRESS','MATERIAL_READY','PLANNED','QUALITY_CHECK') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `customer_order_id` bigint DEFAULT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKi1xf50835asf70j3igna17qq6` (`production_order_number`),
  KEY `FKqteby8dw39stdwyqkq54acamp` (`customer_order_id`),
  KEY `FKj0vvl01mwu7lukvk8glrii11g` (`product_id`),
  CONSTRAINT `FKj0vvl01mwu7lukvk8glrii11g` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKqteby8dw39stdwyqkq54acamp` FOREIGN KEY (`customer_order_id`) REFERENCES `customer_orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` varchar(50) NOT NULL,
  `cost_price` decimal(12,2) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text,
  `product_code` varchar(50) NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `selling_price` decimal(12,2) NOT NULL,
  `status` varchar(30) NOT NULL,
  `type` varchar(30) NOT NULL,
  `unit` varchar(20) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK922x4t23nx64422orei4meb2y` (`product_code`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `purchase_orders`
--

DROP TABLE IF EXISTS `purchase_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `purchase_orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `expected_delivery_date` date NOT NULL,
  `notes` text,
  `po_date` date NOT NULL,
  `po_number` varchar(50) NOT NULL,
  `status` enum('CANCELLED','DRAFT','PARTIALLY_RECEIVED','RECEIVED','SENT') NOT NULL,
  `total_amount` decimal(14,2) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `supplier_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpbiykvcpyg0jslne4gviyeuc2` (`po_number`),
  KEY `FKrpdasmb8y8xs5tiy4369xpinq` (`supplier_id`),
  CONSTRAINT `FKrpdasmb8y8xs5tiy4369xpinq` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_movements`
--

DROP TABLE IF EXISTS `stock_movements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_movements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(50) DEFAULT NULL,
  `movement_type` enum('ADJUSTMENT','IN','OUT','RELEASED','RESERVED') NOT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `quantity` decimal(12,2) NOT NULL,
  `reference_id` varchar(100) DEFAULT NULL,
  `reference_type` varchar(50) DEFAULT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjcaag8ogfjxpwmqypi1wfdaog` (`product_id`),
  CONSTRAINT `FKjcaag8ogfjxpwmqypi1wfdaog` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `suppliers`
--

DROP TABLE IF EXISTS `suppliers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `suppliers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(30) NOT NULL,
  `status` varchar(30) NOT NULL,
  `supplier_code` varchar(50) NOT NULL,
  `supplier_name` varchar(100) NOT NULL,
  `tax_identifier` varchar(50) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqlclyj0vn5vwtb86objyhmlkx` (`supplier_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','FINANCE_MANAGER','PROCUREMENT_MANAGER','PRODUCTION_MANAGER','SALES_MANAGER','WAREHOUSE_MANAGER') NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `username` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'ordercraft'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-01 10:07:50
