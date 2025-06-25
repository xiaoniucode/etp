package cn.xilio.vine.core.command.model;

public abstract class ResultView<T> {
    public abstract void draw(T result);

    protected void write(String data) {
        System.out.println(data);
    }
}
