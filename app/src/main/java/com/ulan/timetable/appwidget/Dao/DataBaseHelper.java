package com.ulan.timetable.appwidget.Dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

/**
 * From https://github.com/SubhamTyagi/TimeTable
 */
class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "class_table.db";
    private static final int DB_VERSION = 4;

    public DataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int version = oldVersion + 1; version <= newVersion; version++) {
            upgradeTo(db, version);
        }
    }

    private void upgradeTo(@NonNull SQLiteDatabase db, int version) {
        switch (version) {
            case 1:
                createTables(db);
                break;
            case 2:
                upgradeFrom1To2(db);
                break;
            case 3:
                upgradeFrom2To3(db);
                break;
            case 4:
                upgradeFrom3To4(db);
                break;
            default:
                throw new IllegalStateException("Don't know how to upgrade to " + version);
        }
    }

    private void upgradeFrom3To4(@NonNull SQLiteDatabase db) {
        db.execSQL("ALTER TABLE app_widget ADD profilePosition INTEGER DEFAULT 0;");
    }

    private void upgradeFrom2To3(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE app_widget(_id INTEGER PRIMARY KEY AUTOINCREMENT , appWidgetId INTEGER , currentTime INTEGER , backgroundColor INTEGER DEFAULT -1 , timeStyle INTEGER DEFAULT -1 , weekStyle INTEGER DEFAULT -1 , UNIQUE(appWidgetId))");
    }

    private void upgradeFrom1To2(@NonNull SQLiteDatabase db) {
        // table_1 表主键添加自增长
        db.execSQL("CREATE TEMPORARY TABLE table_1_backup(week INTEGER , section INTEGER , time INTEGER , startWeek INTEGER , endWeek INTEGER , doubleWeek INTEGER , course CHAR , classroom CHAR)");
        db.execSQL("INSERT INTO table_1_backup SELECT week , section , time , startWeek , endWeek , doubleWeek , course , classroom FROM table_1");
        db.execSQL("DROP TABLE table_1");
        db.execSQL("CREATE TABLE table_1(_id INTEGER PRIMARY KEY AUTOINCREMENT , week INTEGER , section INTEGER , time INTEGER , startWeek INTEGER , endWeek INTEGER ,doubleWeek INTEGER , course CHAR , classroom CHAR)");
        db.execSQL("INSERT INTO table_1 (week , section , time , startWeek , endWeek , doubleWeek , course , classroom) SELECT week , section , time , startWeek , endWeek , doubleWeek , course , classroom FROM table_1_backup");
        db.execSQL("DROP TABLE table_1_backup");

        // 创建 course_classroom 表
        db.execSQL("CREATE TABLE course_classroom(_id INTEGER PRIMARY KEY AUTOINCREMENT , course CHAR , classroom CHAR)");
        // 初始化 course_classroom 表数据
        db.execSQL("INSERT OR IGNORE INTO course_classroom (course , classroom) SELECT course , classroom FROM table_1");

        // 删除 table_2 表
        db.execSQL("DROP TABLE IF EXISTS table_2");
    }

    private void createTables(@NonNull SQLiteDatabase db) {
        db.execSQL("CREATE TABLE app_widget(_id INTEGER PRIMARY KEY AUTOINCREMENT , appWidgetId INTEGER , currentTime INTEGER , backgroundColor INTEGER DEFAULT -1 , timeStyle INTEGER DEFAULT -1 , weekStyle INTEGER DEFAULT -1 , profilePosition INTEGER DEFAULT 0 , UNIQUE(appWidgetId))");
    }
}
