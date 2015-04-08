package pku.ss.cyy.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pku.ss.cyy.bean.City;
import pku.ss.cyy.db.CityDB;

public class MyApplication extends Application {
    private static final String TAG = "MyAPP";
    private static Application mApplication;
    private CityDB mCityDB;
    private List<City> mCityList;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"MyApplication->OnCreate");
        mApplication = this;
    }

    public static Application getInstance() {
        return mApplication;
    }
}
