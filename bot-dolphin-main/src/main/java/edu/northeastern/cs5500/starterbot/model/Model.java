package edu.northeastern.cs5500.starterbot.model;

import edu.northeastern.cs5500.starterbot.ExcludeFromJacocoGeneratedReport;
import org.bson.types.ObjectId;

@ExcludeFromJacocoGeneratedReport
public interface Model {
    ObjectId getId();

    void setId(ObjectId id);
}
