package com.iproject.tapstor.objects;

public class Cat implements Cloneable {
    public int id;
    public String title;

    public Cat(int id, String title) {

        this.id = id;
        this.title = title;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        return new Cat(this.id, this.title);
    }

}
