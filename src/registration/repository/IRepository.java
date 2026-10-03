package registration.repository;

import java.util.List;

/**
 * Generic storage contract (INTERFACE requirement). Services depend on this
 * abstraction, not on a concrete collection.
 */
public interface IRepository<T> {
    void add(T item);
    void remove(String key);
    T getByKey(String key);
    boolean exists(String key);
    List<T> getAll();
    List<T> search(String query);
}
