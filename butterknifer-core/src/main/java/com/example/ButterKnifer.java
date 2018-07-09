package com.example;

/**
 * Created by serenitynanian on 2018/7/9.
 */

public class ButterKnifer {

    public static void bind(Object object) {
        String className = object.getClass().getName() + "$ViewBinder";
        try {
            Class<?> viewBinderClass = Class.forName(className);
            ViewBinder viewBinder = (ViewBinder) viewBinderClass.newInstance();
            viewBinder.bind(object);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }
}
