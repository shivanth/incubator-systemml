/**
 * IBM Confidential
 * OCO Source Materials
 * (C) Copyright IBM Corp. 2010, 2013
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */


package com.ibm.bi.dml.runtime.matrix.mapred;

import java.util.ArrayList;

import com.ibm.bi.dml.runtime.matrix.io.MatrixIndexes;
import com.ibm.bi.dml.runtime.matrix.io.MatrixValue;


public class CachedValueMap extends CachedMap<IndexedMatrixValue>
{
	@SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp. 2010, 2013\n" +
                                             "US Government Users Restricted Rights - Use, duplication  disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";
	
	public IndexedMatrixValue set(byte thisMatrix, MatrixIndexes indexes, MatrixValue value) {
		if(numValid<cache.size())	
			cache.get(numValid).set(indexes, value);
		else
			cache.add(new IndexedMatrixValue(indexes, value));
		
		ArrayList<Integer> list=map.get(thisMatrix);
		if(list==null)
		{
			list=new ArrayList<Integer>(4);
			map.put(thisMatrix, list);
		}
		list.add(numValid);
		numValid++;
		return cache.get(numValid-1);
		
	}

	public IndexedMatrixValue holdPlace(byte thisMatrix, Class<? extends MatrixValue> cls)
	{
		if(numValid>=cache.size())	
			cache.add(new IndexedMatrixValue(cls));
		
		ArrayList<Integer> list=map.get(thisMatrix);
		if(list==null)
		{
			list=new ArrayList<Integer>(4);
			map.put(thisMatrix, list);
		}
		list.add(numValid);
		numValid++;
		return cache.get(numValid-1);
	}
}