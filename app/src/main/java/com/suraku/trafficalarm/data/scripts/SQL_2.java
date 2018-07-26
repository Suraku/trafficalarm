package com.suraku.trafficalarm.data.scripts;

/**
 * Script file
 */

public class SQL_2
{
    public String[] getScripts()
    {
        return new String[] {
                createTable_event
        };
    }

    private final String createTable_event =
            "CREATE TABLE IF NOT EXISTS `event` (\n" +
                    " `EventPK` varchar(36) NOT NULL DEFAULT 'UUID()',\n" +
                    " `CreatedDate` int(16) NOT NULL,\n" +
                    " `UserFK` varchar(36) NOT NULL,\n" +
                    " `IsVisible` int(1) NOT NULL,\n" +
                    " `EventLevel` int(12) NOT NULL,\n" +
                    " `IsReported` int(1) NOT NULL,\n" +
                    " `DisplayMessage` varchar(10000) NOT NULL,\n" +
                    " `ErrorMessage` varchar(10000) NOT NULL,\n" +
                    " PRIMARY KEY (`EventPK`)\n" +
                    " FOREIGN KEY (`UserFK`) REFERENCES `user` (`UserPK`) ON DELETE CASCADE ON UPDATE CASCADE\n" +
                    ");";
}
