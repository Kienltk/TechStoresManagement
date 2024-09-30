package common;

import java.util.ArrayList;

public interface ICommon<T> {
    ArrayList<T> getAll();
    T getOne(long id);
    boolean add(T obj);
    boolean update(T obj, int id);
    boolean delete(int id);
}
