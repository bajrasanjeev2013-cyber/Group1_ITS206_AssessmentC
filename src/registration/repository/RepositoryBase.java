package registration.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import registration.exception.NotFoundException;
import registration.exception.RegistrationException;
import registration.exception.ValidationException;


public abstract class RepositoryBase<T> implements IRepository<T> {
    private final Map<String, T> items = new TreeMap<String, T>(String.CASE_INSENSITIVE_ORDER);

    protected abstract String keyOf(T item);
    protected abstract boolean matches(T item, String query);
    protected abstract String entityName();

    @Override
    public void add(T item) {
        String key = keyOf(item);
        if (items.containsKey(key)) {
            throw new RegistrationException(entityName() + " '" + key + "' already exists.");
        }
        items.put(key, item);
    }

    @Override
    public void remove(String key) {
        if (items.remove(clean(key)) == null) {
            throw new NotFoundException(entityName(), clean(key));
        }
    }

    @Override
    public T getByKey(String key) {
        T item = items.get(clean(key));
        if (item == null) {
            throw new NotFoundException(entityName(), clean(key));
        }
        return item;
    }

    @Override
    public boolean exists(String key) {
        return items.containsKey(clean(key));
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<T>(items.values()); // already sorted by key
    }

    @Override
    public List<T> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new ValidationException("Search text cannot be empty.");
        }
        String q = query.trim();
        List<T> result = new ArrayList<T>();
        for (T item : items.values()) {
            if (matches(item, q)) result.add(item);
        }
        return result;
    }

    private static String clean(String key) {
        return key == null ? "" : key.trim();
    }
}
