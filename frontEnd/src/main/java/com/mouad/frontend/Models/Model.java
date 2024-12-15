package com.mouad.frontend.Models;

import com.mouad.frontend.Views.ViewsFactory;

public class Model {
    private static Model model;
    private final ViewsFactory viewsFactory;

    public Model() {
        this.viewsFactory = new ViewsFactory();
    }

    public static synchronized Model getInstance(){
        if(model == null){
            model = new Model();
        }
        return model;
    }

    public ViewsFactory getViewsFactory(){
        return  viewsFactory;
    }
}
