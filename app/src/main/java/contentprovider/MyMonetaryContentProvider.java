package contentprovider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import database.CategoryTable;
import database.MonetaryDatabaseHelper;
import database.MonetaryTable;
import model.Balance;
import model.Category;
import model.Money;

public class MyMonetaryContentProvider extends ContentProvider {

    // database
    private static SQLiteDatabase db;

    private service.Service service_ = service.Service.getInstance();

    // UriMatcher
    private static final int MONETARYS = 1;
    private static final int MONETARY_ID = 2;

    private static final int CATEGORYS = 3;
    private static final int CATEGORY_ID = 4;

    private static final String AUTHORITHY = "com.example.contentprovider";

    private static final String BASE_PATH = "money";

    private static final String BASE_PATH_CAT = "category";

    public static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITHY + "/"
            + BASE_PATH);

    public static Uri CONTENT_URI_CAT = Uri.parse("content://" + AUTHORITHY
            + "/" + BASE_PATH_CAT);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/money";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/money";

    public static final String CONTENT_TYPE_CAT = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/category";

    public static final String CONTENT_ITEM_TYPE_CAT = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/category";

    public static final UriMatcher sURI_MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURI_MATCHER.addURI(AUTHORITHY, BASE_PATH, MONETARYS);
        sURI_MATCHER.addURI(AUTHORITHY, BASE_PATH + "/#", MONETARY_ID);
        sURI_MATCHER.addURI(AUTHORITHY, BASE_PATH_CAT, CATEGORYS);
        sURI_MATCHER.addURI(AUTHORITHY, BASE_PATH_CAT + "/#", CATEGORY_ID);

    }


    @Override
    public boolean onCreate() {
        MonetaryDatabaseHelper database = new MonetaryDatabaseHelper(getContext());
        db = database.getWritableDatabase();

        return (db == null) ? false : true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();


        checkColumns(projection);

        int uriType = sURI_MATCHER.match(uri);

        switch (uriType) {

            case MONETARY_ID:
                queryBuilder.appendWhere(MonetaryTable.COLUMN_ID + "="
                        + uri.getLastPathSegment());

                break;
            case MONETARYS:
                queryBuilder.setTables(MonetaryTable.TABLE_MONETARY);

                break;

            case CATEGORY_ID:
                queryBuilder.appendWhere(CategoryTable.COLUMN_ID + "="
                        + uri.getLastPathSegment());

                break;
            case CATEGORYS:
                queryBuilder.setTables(CategoryTable.TABLE_CATEGORY);

                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);

        }


        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);


        return cursor;
    }

    private void checkColumns(String[] projection) {
        String[] available = {MonetaryTable.COLUMN_ID,
                MonetaryTable.COLUMN_CATEGORY, MonetaryTable.COLUMN_AMOUNT,
                MonetaryTable.COLUMN_NOTES, MonetaryTable.COLUMN_DATE,
                MonetaryTable.COLUMN_RULE, MonetaryTable.COLUMN_TYPE};

        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(
                    Arrays.asList(projection));
            HashSet<String> availableCollumns = new HashSet<String>(
                    Arrays.asList(available));
            if (!availableCollumns.contains(requestedColumns)) {
                throw new IllegalArgumentException(
                        "Unknown columns in projection");
            }

        }

    }

    @Override
    public String getType(Uri uri) {

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURI_MATCHER.match(uri);
        Uri doneUri;


        int rowsDeleted = 0;
        long id = 0;

        switch (uriType) {
            case MONETARYS:

                id = db.insert(MonetaryTable.TABLE_MONETARY, null, values);

                break;

            case CATEGORYS:

                id = db.insert(CategoryTable.TABLE_CATEGORY, null, values);
                break;
            case -1:


            default:
                throw new IllegalArgumentException("Unknown URI" + uri);


        }

        getContext().getContentResolver().notifyChange(uri, null);

        if (uriType == MONETARYS) {

            doneUri = Uri.parse(BASE_PATH + "/" + id);
        } else {
            doneUri = Uri.parse(BASE_PATH_CAT + "/" + id);
        }

        return doneUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURI_MATCHER.match(uri);

        int rowsDeleted = 0;
        switch (uriType) {
            case MONETARYS:

                rowsDeleted = db.delete(MonetaryTable.TABLE_MONETARY, selection,
                        selectionArgs);
                break;

            case MONETARY_ID:
                //sqlDB = database.getWritableDatabase();
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(MonetaryTable.TABLE_MONETARY,
                            MonetaryTable.COLUMN_ID + "=" + id, null);

                } else {
                    rowsDeleted = db.delete(MonetaryTable.TABLE_MONETARY,
                            MonetaryTable.COLUMN_ID + "=" + id + "and" + selection,
                            selectionArgs);
                }
                break;

            case CATEGORYS:

                rowsDeleted = db.delete(MonetaryTable.TABLE_MONETARY, selection,
                        selectionArgs);
                break;

            case CATEGORY_ID:

                String id_cat = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(CategoryTable.TABLE_CATEGORY,
                            CategoryTable.COLUMN_ID + "=" + id_cat, null);

                } else {
                    rowsDeleted = db.delete(CategoryTable.TABLE_CATEGORY,
                            CategoryTable.COLUMN_ID + "=" + id_cat + "and"
                                    + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI :" + uri);

        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURI_MATCHER.match(uri);

        int rowsUpdated = 0;
        switch (uriType) {
            case MONETARYS:

                rowsUpdated = db.update(MonetaryTable.TABLE_MONETARY, values,
                        selection, selectionArgs);

                break;
            case MONETARY_ID:

                String id = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(MonetaryTable.TABLE_MONETARY,
                            values, MonetaryTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsUpdated = db.update(MonetaryTable.TABLE_MONETARY,
                            values, MonetaryTable.COLUMN_ID + "=" + id + "and"
                                    + selection, selectionArgs);
                }
                break;
            case CATEGORYS:

                rowsUpdated = db.update(CategoryTable.TABLE_CATEGORY, values,
                        selection, selectionArgs);
                break;

            case CATEGORY_ID:

                String id_cat = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(CategoryTable.TABLE_CATEGORY,
                            values, CategoryTable.COLUMN_ID + "=" + id_cat, null);
                } else {
                    rowsUpdated = db.update(CategoryTable.TABLE_CATEGORY,
                            values, CategoryTable.COLUMN_ID + "=" + id_cat + "and"
                                    + selection, selectionArgs);
                }
                break;

            default:

                throw new IllegalArgumentException("Unknown URI :" + uri);

        }
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }

    public boolean tableExists(String tableName, Context context) {

        Cursor cursor = null;
        boolean created = false;

        try {

            cursor = db.rawQuery("Select * FROM " + tableName, null);

            created = true;
        } catch (Exception e) {

            Toast toast = Toast.makeText(context, e + " ", Toast.LENGTH_LONG);

        }
        cursor.close();

        return created;

    }

    public void dropTable(Context context) {


        String sql = "drop table " + "category";
        try {

            db.execSQL(sql);

        } catch (SQLException e) {
            Toast toast = Toast.makeText(context, e + " ", Toast.LENGTH_LONG);
        }
    }

    public void createCategoriesForFirstTime(Context context, String tablename) {

        if (this.tableExists(tablename, context)) {

            try {

                String[] colorList = {"#00ffff", "#8b008b", "#add8e6", "#90ee90",
                        "#d3d3d3", "#ffb6c1", "#ffffe0", "#0000ff", "#a52a2a", "#00008b",
                        "#008b8b", "#a9a9a9", "#006400", "#bdb76b", "#556b2f", "#ff8c00",
                        "#9932cc", "#8b0000", "#e9967a", "#9400d3", "#ff00ff", "#ffd700",
                        "#008000", "#4b0082", "#f0e68c", "#00ff00", "#800000", "#000080",
                        "#ffa500", "#ffc0cb", "#800080", "#ff0000", "#c0c0c0", "#ffff00"};


                String[] categoryNames = {"Other", "Food and drinks",
                        "Health care", "Leisure", "Transportation", "Fuel",
                        "Hotel", "Household", "Insurance", "Utilities"};

                for (int i = 0; i < categoryNames.length; i++) {

                    ContentValues values = new ContentValues();
                    values.put(CategoryTable.COLUMN_NAME, categoryNames[i]);
                    values.put(CategoryTable.COLUMN_COLOR, colorList[i]);
                    Uri uri = context.getContentResolver().insert(
                            MyMonetaryContentProvider.CONTENT_URI_CAT, values);

                }

            } catch (Exception e) {
                Toast toast = Toast.makeText(context, e + " ", Toast.LENGTH_LONG);
            }
        }

    }

    public ArrayList<Category> getCategories(Context context) {


        ArrayList<Category> listCat = new ArrayList<Category>();

        Cursor cursor = db
                .query("category", null, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            long catId = Long.parseLong(cursor.getString(cursor
                    .getColumnIndex(CategoryTable.COLUMN_ID)));
            String nameCat = cursor.getString(cursor
                    .getColumnIndex(CategoryTable.COLUMN_NAME));
            String colorCat = (cursor.getString(cursor
                    .getColumnIndex(CategoryTable.COLUMN_COLOR)));

            Category category = new Category(catId, nameCat, colorCat);
            cursor.moveToNext();

            listCat.add(category);
        }
        cursor.close();

        return listCat;

    }

    public Category getCategoryForId(long categoryId) {

        Category category = null;


        Cursor cursor = db.query(CategoryTable.TABLE_CATEGORY,
                new String[]{CategoryTable.COLUMN_ID, CategoryTable.COLUMN_COLOR, CategoryTable.COLUMN_NAME},
                CategoryTable.COLUMN_ID + "=?",
                new String[]{String.valueOf(categoryId)}
                , null, null, null);
        if (cursor.moveToFirst()) {
            do {

                category = new Category();

                category.setCategory_id(cursor.getLong(cursor.getColumnIndex(CategoryTable.COLUMN_ID)));

                category.setColor(cursor.getString(cursor.getColumnIndex(CategoryTable.COLUMN_COLOR)));

                category.setName(cursor.getString(cursor.getColumnIndex(CategoryTable.COLUMN_NAME)));
            } while (cursor.moveToNext());

        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return category;
    }

    public boolean createNewCategory(String categoryName, String color, Context context) {

        try {
            ContentValues values = new ContentValues();
            values.put(CategoryTable.COLUMN_NAME, categoryName);
            values.put(CategoryTable.COLUMN_COLOR, color);
            Uri uri = context.getContentResolver().insert(
                    MyMonetaryContentProvider.CONTENT_URI_CAT, values);
            return true;
        } catch (Exception ex) {
            return false;
        }


    }

    public void createIncomeExpense(Money money, Context context) {

        String moneyAmount = String.valueOf(money.getAmount());
        String moneyDate = service_.getStringFromDate(money.getDate());

        ContentValues values = new ContentValues();
        values.put(MonetaryTable.COLUMN_AMOUNT, moneyAmount);
        values.put(MonetaryTable.COLUMN_NOTES, money.getNotes());
        values.put(MonetaryTable.COLUMN_DATE, moneyDate);
        values.put(MonetaryTable.COLUMN_CATEGORY, money.getCategory());
        values.put(MonetaryTable.COLUMN_RULE, money.getRule());
        values.put(MonetaryTable.COLUMN_TYPE, money.getType());
        values.put(MonetaryTable.COLUMN_STATUS, money.getStatus());


        Uri uri = context.getContentResolver().insert(
                MyMonetaryContentProvider.CONTENT_URI, values);


    }

    public void getExpenses() {
        ArrayList<Money> expenses = new ArrayList<Money>();

        Cursor cursor = db
                .query("monetary", null, null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            long moneyId = Long.parseLong(cursor.getString(cursor
                    .getColumnIndex(MonetaryTable.COLUMN_ID)));
            String catMoney = cursor.getString(cursor
                    .getColumnIndex(MonetaryTable.COLUMN_CATEGORY));
            Double amount = (cursor.getDouble(cursor
                    .getColumnIndex(MonetaryTable.COLUMN_AMOUNT)));
            String date = (cursor.getString(cursor
                    .getColumnIndex(MonetaryTable.COLUMN_DATE)));
            String rule = cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_RULE));
            String type = cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_TYPE));
            int status = cursor.getInt(cursor.getColumnIndex(MonetaryTable.COLUMN_STATUS));


            cursor.moveToNext();


        }
        cursor.close();


    }

    public ArrayList<Money> getMonthIncomesExpenses(int month) {
        getExpenses();
        String monthValue;
        if (month < 10) {
            monthValue = "0" + month;
        } else {
            monthValue = String.valueOf(month);
        }

        ArrayList<Money> expenses = new ArrayList<>();
        Cursor cursor = db.query("monetary",
                new String[]{MonetaryTable.COLUMN_ID, MonetaryTable.COLUMN_CATEGORY,
                        MonetaryTable.COLUMN_AMOUNT, MonetaryTable.COLUMN_NOTES,
                        MonetaryTable.COLUMN_DATE, MonetaryTable.COLUMN_RULE,
                        MonetaryTable.COLUMN_TYPE, MonetaryTable.COLUMN_STATUS},
                "strftime('%m',date)=? AND type!='Balance'",
                new String[]{monthValue}
                , MonetaryTable.COLUMN_ID, null, MonetaryTable.COLUMN_DATE + " DESC ", null);

        if (cursor.moveToFirst()) {

            do {

                Money money = new Money();
                money.setMoney_id(cursor.getLong(cursor.getColumnIndex(MonetaryTable.COLUMN_ID)));

                money.setCategory(cursor.getLong(cursor.getColumnIndex(MonetaryTable.COLUMN_CATEGORY)));

                money.setAmount(cursor.getDouble(cursor.getColumnIndex(MonetaryTable.COLUMN_AMOUNT)));

                money.setNotes(cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_NOTES)));

                String date = cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_DATE));
                money.setDate(service_.getDateFromString(date));

                money.setRule(cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_RULE)));
                money.setType(cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_TYPE)));
                money.setStatus(cursor.getInt(cursor.getColumnIndex(MonetaryTable.COLUMN_STATUS)));

                expenses.add(money);
            } while (cursor.moveToNext());

        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return expenses;

    }

    public Balance getSavedBalance(Context context) {

        Balance balance = new Balance();
        Cursor cursor = db.query(MonetaryTable.TABLE_MONETARY,
                new String[]{MonetaryTable.COLUMN_AMOUNT, MonetaryTable.COLUMN_DATE},
                "type='Balance' ", null
                , null, null, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {

            cursor.moveToFirst();
            double amount = cursor.getDouble(cursor.getColumnIndex(MonetaryTable.COLUMN_AMOUNT));
            String datebalance = cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_DATE));
            Date balanceDate = service_.getDateFromString(datebalance);

            balance.setAmount(amount);
            balance.setBalanceDate(balanceDate);
        } else {

            balance.setAmount(0);
            balance.setBalanceDate(new Date());

        }
        cursor.close();

        return balance;

    }

    public boolean updateBalance(double amount, Date date) {


        String updateDate = service_.getStringFromDate(service_.getUpdateDate(date));
        ContentValues cv = new ContentValues();
        cv.put(MonetaryTable.COLUMN_AMOUNT, amount);
        cv.put(MonetaryTable.COLUMN_DATE, updateDate);
        return db.update(MonetaryTable.TABLE_MONETARY, cv, "type='Balance'", null) > 0;

    }

    public ArrayList<Money> getWeekIncomesExpenses(Date date) {
        String week;
        int weekOfYear = service_.getWeekOfYear(date);
        if (weekOfYear < 10) {
            week = "0" + String.valueOf(weekOfYear);
        } else {
            week = String.valueOf(weekOfYear);
        }
        ArrayList<Money> weekIncExp = new ArrayList<>();
        Cursor cursor = db.query(MonetaryTable.TABLE_MONETARY,
                new String[]{MonetaryTable.COLUMN_ID, MonetaryTable.COLUMN_CATEGORY,
                        MonetaryTable.COLUMN_AMOUNT, MonetaryTable.COLUMN_NOTES,
                        MonetaryTable.COLUMN_DATE, MonetaryTable.COLUMN_RULE,
                        MonetaryTable.COLUMN_TYPE, MonetaryTable.COLUMN_STATUS},
                "strftime('%W',date)" + "=? AND type!='Balance' ",
                new String[]{week}, MonetaryTable.COLUMN_ID
                , null, MonetaryTable.COLUMN_DATE + " DESC ", null);
        if (cursor.moveToFirst()) {
            do {

                Money money = new Money();
                money.setMoney_id(cursor.getLong(cursor.getColumnIndex(MonetaryTable.COLUMN_ID)));

                money.setCategory(cursor.getLong(cursor.getColumnIndex(MonetaryTable.COLUMN_CATEGORY)));

                money.setAmount(cursor.getDouble(cursor.getColumnIndex(MonetaryTable.COLUMN_AMOUNT)));

                money.setNotes(cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_NOTES)));

                String stringDate = cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_DATE));
                money.setDate(service_.getDateFromString(stringDate));

                money.setRule(cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_RULE)));
                money.setType(cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_TYPE)));
                money.setStatus(cursor.getInt(cursor.getColumnIndex(MonetaryTable.COLUMN_STATUS)));

                weekIncExp.add(money);

            } while (cursor.moveToNext());

        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return weekIncExp;

    }

    public ArrayList<Money> getIncExpWithStatus() {
        ArrayList<Money> moneyList = new ArrayList<>();
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        Cursor cursor = db.query(MonetaryTable.TABLE_MONETARY,
                new String[]{MonetaryTable.COLUMN_ID, MonetaryTable.COLUMN_CATEGORY,
                        MonetaryTable.COLUMN_AMOUNT, MonetaryTable.COLUMN_NOTES,
                        MonetaryTable.COLUMN_DATE, MonetaryTable.COLUMN_RULE,
                        MonetaryTable.COLUMN_TYPE, MonetaryTable.COLUMN_STATUS},
                "strftime('%j',date)" + "=? AND strftime('%Y',date)" + "=?" + "AND status='0'",
                new String[]{String.valueOf(dayNumber), String.valueOf(year)}, MonetaryTable.COLUMN_ID
                , null, MonetaryTable.COLUMN_DATE + " DESC ", null);

        if (cursor.moveToFirst()) {
            do {
                Money money = new Money();
                money.setMoney_id(cursor.getLong(cursor.getColumnIndex(MonetaryTable.COLUMN_ID)));

                money.setCategory(cursor.getLong(cursor.getColumnIndex(MonetaryTable.COLUMN_CATEGORY)));

                money.setAmount(cursor.getDouble(cursor.getColumnIndex(MonetaryTable.COLUMN_AMOUNT)));

                money.setNotes(cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_NOTES)));

                String stringDate = cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_DATE));
                money.setDate(service_.getDateFromString(stringDate));

                money.setRule(cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_RULE)));
                money.setType(cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_TYPE)));
                money.setStatus(cursor.getInt(cursor.getColumnIndex(MonetaryTable.COLUMN_STATUS)));
                moneyList.add(money);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return moneyList;
    }

    public boolean updateStatus(Money money, Context context) {
        double amount;
        Balance balance;

        ContentValues cv = new ContentValues();
        cv.put(MonetaryTable.COLUMN_STATUS, "1");
        String where = "_id=" + money.getMoney_id();
        db.update(MonetaryTable.TABLE_MONETARY, cv, where, null);


        balance = getSavedBalance(context);
        if (money.getType().equals("Income")) {
            balance.setAmount(balance.getAmount() + money.getAmount());

        } else if (money.getType().equals("Expense")) {
            balance.setAmount(balance.getAmount() - money.getAmount());
        }
        return updateBalance(balance.getAmount(), money.getDate());


    }

    public ArrayList<Money> getExpensesForPeriod(Date date1, Date date2) {
        ArrayList<Money> expenses = new ArrayList<>();
        Cursor cursor = db.query(MonetaryTable.TABLE_MONETARY,
                new String[]{MonetaryTable.COLUMN_ID, MonetaryTable.COLUMN_CATEGORY,
                        MonetaryTable.COLUMN_AMOUNT, MonetaryTable.COLUMN_NOTES,
                        MonetaryTable.COLUMN_DATE, MonetaryTable.COLUMN_RULE,
                        MonetaryTable.COLUMN_TYPE, MonetaryTable.COLUMN_STATUS}, MonetaryTable.COLUMN_DATE + " BETWEEN ? AND ? AND type='Expense' ",
                new String[]{service_.getStringFromDate(date1), service_.getStringFromDate(date2)}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Money money = new Money();
                money.setMoney_id(cursor.getLong(cursor.getColumnIndex(MonetaryTable.COLUMN_ID)));

                money.setCategory(cursor.getLong(cursor.getColumnIndex(MonetaryTable.COLUMN_CATEGORY)));

                money.setAmount(cursor.getDouble(cursor.getColumnIndex(MonetaryTable.COLUMN_AMOUNT)));

                money.setNotes(cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_NOTES)));

                String stringDate = cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_DATE));
                money.setDate(service_.getDateFromString(stringDate));

                money.setRule(cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_RULE)));
                money.setType(cursor.getString(cursor.getColumnIndex(MonetaryTable.COLUMN_TYPE)));
                money.setStatus(cursor.getInt(cursor.getColumnIndex(MonetaryTable.COLUMN_STATUS)));
                expenses.add(money);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }


        return expenses;
    }

    public double getAmountMonth(int month, int year, Category category) {
        String monthS;
        double amount = 0;
        if (month < 10) {
            monthS = "0" + month;
        } else {
            monthS = String.valueOf(month);
        }

        Cursor cursor = db.query(MonetaryTable.TABLE_MONETARY, new String[]{"sum(amount)"},
                "type='Expense' AND strftime('%m',date)=? AND strftime('%Y',date)=? AND category=?", new String[]{monthS, String.valueOf(year), String.valueOf(category.getCategory_id())}, null, null, null);


        if (cursor.moveToFirst()) {
            do {
                amount = cursor.getDouble(0);

            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return amount;
    }

    public boolean updateOnPostpone(Money money) {

        try {
            long id = money.getMoney_id();
            String updateDate = service_.getStringFromDate(service_.getUpdateDate(money.getDate()));
            ContentValues cv = new ContentValues();
            cv.put(MonetaryTable.COLUMN_AMOUNT, money.getAmount());
            cv.put(MonetaryTable.COLUMN_DATE, updateDate);
            cv.put(MonetaryTable.COLUMN_NOTES, money.getNotes());
            return db.update(MonetaryTable.TABLE_MONETARY, cv, "_id=" + id, null) > 0;
        } catch (Exception ex) {
            return false;
        }


    }

    public boolean deleteIncExp(Money money) {


        long delId = money.getMoney_id();
        return db.delete(MonetaryTable.TABLE_MONETARY, "_id=" + delId, null) > 0;
    }

    public long getMoneyIdFromDB(Money money) {

        long retrunId = 0;
        Cursor cursor = db.query(MonetaryTable.TABLE_MONETARY,
                new String[]{MonetaryTable.COLUMN_ID, MonetaryTable.COLUMN_CATEGORY,
                        MonetaryTable.COLUMN_AMOUNT, MonetaryTable.COLUMN_NOTES,
                        MonetaryTable.COLUMN_DATE, MonetaryTable.COLUMN_RULE,
                        MonetaryTable.COLUMN_TYPE, MonetaryTable.COLUMN_STATUS}, MonetaryTable.COLUMN_DATE + "=? AND status=0 AND type=? AND amount=? AND category=?",
                new String[]{service_.getStringFromDate(money.getDate()), money.getType(), String.valueOf(money.getAmount()), String.valueOf(money.getCategory())}, null, null, null);

        if (cursor.moveToFirst()) {
            do {

                retrunId = cursor.getLong(cursor.getColumnIndex(MonetaryTable.COLUMN_ID));

            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return retrunId;

    }

}
