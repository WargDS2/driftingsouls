ALTER TABLE `ship_script_data` ADD CONSTRAINT `script_data_fk_ship` FOREIGN KEY (`shipid`) REFERENCES `ships` (`id`);