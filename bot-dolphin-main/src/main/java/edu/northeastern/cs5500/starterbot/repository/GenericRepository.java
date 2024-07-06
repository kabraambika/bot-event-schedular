package edu.northeastern.cs5500.starterbot.repository;

import edu.northeastern.cs5500.starterbot.ExcludeFromJacocoGeneratedReport;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.bson.types.ObjectId;

@ExcludeFromJacocoGeneratedReport
public interface GenericRepository<T> {
    public T get(@Nonnull ObjectId id);

    public T add(@Nonnull T item);

    public T update(@Nonnull T item);

    public void delete(@Nonnull ObjectId id);

    public Collection<T> getAll();

    public long count();
}
