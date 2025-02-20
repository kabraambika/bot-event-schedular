package edu.northeastern.cs5500.starterbot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to exclude methods or classes from the JaCoCo generated report. This annotation must
 * be used for marking methods or classes that are not generated by team
 *
 * <p>When applied to a method or class, it indicates that the annotated element should be excluded
 * from the code coverage report generated by JaCoCo.
 *
 * <p>Reference taken by Professor's lecture
 */
@Retention(RetentionPolicy.CLASS)
public @interface ExcludeFromJacocoGeneratedReport {}
