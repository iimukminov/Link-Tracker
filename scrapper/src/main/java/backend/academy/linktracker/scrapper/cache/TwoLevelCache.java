package backend.academy.linktracker.scrapper.cache;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;

@RequiredArgsConstructor
public class TwoLevelCache implements Cache {

    private final String name;
    private final Cache l1Cache;
    private final Cache l2Cache;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {
        ValueWrapper wrapper = l1Cache.get(key);
        if (wrapper != null) {
            return wrapper;
        }

        wrapper = l2Cache.get(key);
        if (wrapper != null) {
            l1Cache.put(key, wrapper.get());
        }
        return wrapper;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        ValueWrapper wrapper = get(key);
        return wrapper != null ? (T) wrapper.get() : null;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        ValueWrapper wrapper = get(key);
        if (wrapper != null) {
            return (T) wrapper.get();
        }
        try {
            T value = valueLoader.call();
            put(key, value);
            return value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        l1Cache.put(key, value);
        l2Cache.put(key, value);
    }

    @Override
    public void evict(Object key) {
        l1Cache.evict(key);
        l2Cache.evict(key);
    }

    @Override
    public void clear() {
        l1Cache.clear();
        l2Cache.clear();
    }
}
