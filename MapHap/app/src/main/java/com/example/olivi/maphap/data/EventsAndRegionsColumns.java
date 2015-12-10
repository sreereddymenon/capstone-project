package com.example.olivi.maphap.data;

import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.References;

/**
 * Created by olivi on 11/30/2015.
 */
public interface EventsAndRegionsColumns {
    @DataType(DataType.Type.TEXT)
    @PrimaryKey
    @References(table = EventDatabase.EVENTS,
            column = EventsColumns.EB_ID)
    String EVENT_ID =
            "event_id";


    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @References(table = EventDatabase.REGIONS,
            column = RegionsColumns._ID)
    String REGION_ID =
            "region_id";
}
