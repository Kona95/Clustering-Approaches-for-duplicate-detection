/*
 * This file is licensed to You under the "Simplified BSD License".
 * You may not use this software except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/bsd-license.php
 * 
 * See the COPYRIGHT file distributed with this work for information
 * regarding copyright ownership.
 */
package ch.usi.inf.sape.hac.experiment;


/**
 * Computes the dissimilarity between two observations in an experiment.
 *
 * @author Matthias.Hauswirth@usi.ch
 */
public interface DissimilarityMeasure<T> {

    public double computeDissimilarity(T o1, T o2);

}
