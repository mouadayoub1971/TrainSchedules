package com.mouad.frontend.Models;

import com.mouad.frontend.Views.ViewsFactory;
import com.mouad.frontend.Views.ViewsFactory1;

public class Model {
    private static Model model;
    private final ViewsFactory1 viewsFactory;

    public Model() {
        this.viewsFactory = new ViewsFactory1();
    }

    public static synchronized Model getInstance(){
        if(model == null){
            model = new Model();
        }
        return model;
    }

    public ViewsFactory1 getViewsFactory(){
        return  viewsFactory;
    }
}
