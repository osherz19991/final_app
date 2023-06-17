package com.example.finalapp.Utilities;

import android.app.Activity;
import android.content.Intent;

import com.example.finalapp.Add_expense_income_Activity;
import com.example.finalapp.CategoryActivity;
import com.example.finalapp.DistributionGraphActivity;
import com.example.finalapp.Income_Expanse_Activity;
import com.example.finalapp.MainActivity;
import com.example.finalapp.R;

public class BottomNavigationManager {

    public static final int MENU_HOME = R.id.menu_home;
    public static final int MENU_ADD = R.id.menu_add;
    public static final int MENU_GRAPH = R.id.menu_distribution;
    public static final int MENU_INCOME_EXPENSE = R.id.menu_list;

    public static final int MENU_CATEGORY = R.id.menu_category;



    public static void navigate(Activity activity, int menuItemId) {
        Intent intent = null;

        if (menuItemId == MENU_HOME) {
            intent = new Intent(activity, MainActivity.class);
        } else if (menuItemId == MENU_ADD) {
            intent = new Intent(activity, Add_expense_income_Activity.class);
        } else if (menuItemId == MENU_GRAPH) {
            intent = new Intent(activity, DistributionGraphActivity.class);
        } else if (menuItemId == MENU_INCOME_EXPENSE) {
            intent = new Intent(activity, Income_Expanse_Activity.class);
        } else if (menuItemId == MENU_CATEGORY) {
            intent = new Intent(activity, CategoryActivity.class);
        }

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        }
    }
}



