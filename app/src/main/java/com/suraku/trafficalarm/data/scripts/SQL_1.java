package com.suraku.trafficalarm.data.scripts;

/**
 * Script file
 */

public class SQL_1
{
    public String[] getScripts()
    {
        return new String[] {
                createTable_user,
                createTable_address,
                createTable_timeRequest,
                createTable_alarm,
                "PRAGMA foreign_keys = ON;"
        };
    }

    private final String createTable_user =
            "CREATE TABLE IF NOT EXISTS `user` (\n" +
                    " `UserPK` varchar(36) NOT NULL DEFAULT 'UUID()',\n" +
                    " `Username` varchar(1000) NOT NULL,\n" +
                    " `Password` varchar(1000) NOT NULL,\n" +
                    " `DisplayName` varchar(1000) NOT NULL,\n" +
                    " `CreatedDate` int(16) NOT NULL,\n" +
                    " PRIMARY KEY (`UserPK`)\n" +
                    ");";

    private final String createTable_address =
            "CREATE TABLE IF NOT EXISTS `address` (\n" +
                    " `AddressPK` varchar(36) NOT NULL DEFAULT 'UUID()',\n" +
                    " `AddressLineOne` varchar(1000) NOT NULL,\n" +
                    " `AddressLineTwo` varchar(1000) NOT NULL,\n" +
                    " `City` varchar(1000) NOT NULL,\n" +
                    " `County` varchar(1000) NOT NULL,\n" +
                    " `Postcode` varchar(1000) NOT NULL,\n" +
                    " `CreatedDate` int(16) NOT NULL,\n" +
                    " `UserFK` varchar(36) NOT NULL,\n" +
                    " PRIMARY KEY (`AddressPK`)\n" +
                    " FOREIGN KEY (`UserFK`) REFERENCES `user` (`UserPK`)  ON DELETE CASCADE ON UPDATE CASCADE\n" +
                    ");";

    private final String createTable_timeRequest =
            "CREATE TABLE IF NOT EXISTS `timerequest` (\n" +
                    " `TimeRequestPK` varchar(36) NOT NULL DEFAULT 'UUID()',\n" +
                    " `JsonResponse` varchar(1000) NOT NULL,\n" +
                    " `DurationInTraffic` int(11) NOT NULL,\n" +
                    " `CreatedDate` int(16) NOT NULL,\n" +
                    " `OriginAddressFK` varchar(36) NOT NULL,\n" +
                    " `DestinationAddressFK` varchar(36) NOT NULL,\n" +
                    " PRIMARY KEY (`TimeRequestPK`),\n" +
                    " FOREIGN KEY (`OriginAddressFK`) REFERENCES `address` (`AddressPK`) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                    " FOREIGN KEY (`DestinationAddressFK`) REFERENCES `address` (`AddressPK`) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                    ");";

    private final String createTable_alarm =
            "CREATE TABLE IF NOT EXISTS `alarm` (\n" +
                    " `AlarmPK` varchar(36) NOT NULL DEFAULT 'UUID()',\n" +
                    " `Hour` int(11) NOT NULL,\n" +
                    " `Minute` int(11) NOT NULL,\n" +
                    " `IsActive` int(11) NOT NULL,\n" +
                    " `CreatedDate` int(16) NOT NULL,\n" +
                    " `UserFK` varchar(36) NOT NULL,\n" +
                    " `OriginAddressFK` varchar(36),\n" +
                    " `DestinationAddressFK` varchar(36),\n" +
                    " PRIMARY KEY (`AlarmPK`)\n" +
                    " FOREIGN KEY (`UserFK`) REFERENCES `user` (`UserPK`) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                    " FOREIGN KEY (`OriginAddressFK`) REFERENCES `address` (`AddressPK`) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                    " FOREIGN KEY (`DestinationAddressFK`) REFERENCES `address` (`AddressPK`) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                    ");";
}
