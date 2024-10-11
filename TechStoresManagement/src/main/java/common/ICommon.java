package common;

import java.util.ArrayList;

public interface ICommon<T> {
    ArrayList<T> getAll(int idStore );
    T getOne(int idStore ,long id);
    boolean add(T obj);
    boolean update(T obj, int id);
    boolean delete(int id);
}
