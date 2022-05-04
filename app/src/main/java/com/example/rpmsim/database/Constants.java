package com.example.rpmsim.database;

public class Constants {
    // название таблицы в бд
    public static final String TABLE_DETECTOR = "detector";
    public static final String COLUMN_ID_DETECTOR = "_id";
    public static final String COLUMN_NAME_DETECTOR = "name_detector";
    public static final String COLUMN_GEOMETRICAL_SIZES = "geometrical_sizes";
    public static final String COLUMN_BACKGROUND = "background";
    // название таблицы в бд
    public static final String TABLE_SOURCE = "source";
    public static final String COLUMN_ID_SOURCE = "_id";
    public static final String COLUMN_NAME_SOURCE = "name_source";
    public static final String COLUMN_SOURCE_DIMENSION = "dimension";

    //таблица чувствительности
    public static final String TABLE_DETECTOR_SOURCE = "detector_source";
    public static final String COLUMN_DETECTOR_SOURCE_ID = "_id";
    public static final String COLUMN_SOURCE_DETECTOR_ID = "source_id";
    public static final String COLUMN_VALUE = "value";
    //таблица коэффициентов
    public static final String TABLE_SOURCE_FACTOR = "source_factor";
    public static final String COLUMN_SOURCE_FACTOR_ID = "_id";
    public static final String COLUMN_SOURCE_FACTOR_VALUE = "source_factor_value";
    public static final String COLUMN_SOURCE_FACTOR_DIMENSION = "dimension_factor";
    //таблица защиты
    public static final String TABLE_SHIELD = "shield";
    public static final String COLUMN_ID_SHIELD = "_id";
    public static final String COLUMN_NAME_MATERIAL = "name_material";
    //таблица коэф для различных источников и защиты
    public static final String TABLE_SHIELD_SOURCE = "shield_source";
    public static final String COLUMN_ID_SHIELD_SOURCE = "_id";
    public static final String COLUMN_ID_SOURCE_SHIELD = "source_id";
    public static final String COLUMN_SOURCE_SHIELD_VALUE = "value";
}
