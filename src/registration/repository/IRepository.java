package registration.repository;

import java.util.List;


public interface IRepository<T> {
    void add(T item);
    void remove(String key);
    T getByKey(String key);
    boolean exists(String key);
    List<T> getAll();
    List<T> search(String query);
}
