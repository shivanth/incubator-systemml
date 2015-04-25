/**
 * IBM Confidential
 * OCO Source Materials
 * (C) Copyright IBM Corp. 2010, 2015
 * The source code for this program is not published or otherwise divested of its trade secrets, irrespective of what has been deposited with the U.S. Copyright Office.
 */

package com.ibm.bi.dml.test.integration.functions.gdfo;

import java.util.HashMap;
import org.junit.Test;

import com.ibm.bi.dml.api.DMLScript.RUNTIME_PLATFORM;
import com.ibm.bi.dml.lops.LopProperties.ExecType;
import com.ibm.bi.dml.runtime.matrix.data.MatrixValue.CellIndex;
import com.ibm.bi.dml.test.integration.AutomatedTestBase;
import com.ibm.bi.dml.test.integration.TestConfiguration;
import com.ibm.bi.dml.test.utils.TestUtils;

/**
 * 
 */
public class GDFOMMChainLoop extends AutomatedTestBase 
{
	@SuppressWarnings("unused")
	private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp. 2010, 2015\n" +
                                             "US Government Users Restricted Rights - Use, duplication  disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";
	
	private final static String TEST_NAME1 = "MMChainLoop";
	private final static String TEST_DIR = "functions/gdfo/";
	private final static String TEST_CONF = "SystemML-config-globalopt.xml";
	
	private final static double eps = 1e-10;
	
	private final static int rows = 1468;
	private final static int cols = 1007;
		
	private final static double sparsity1 = 0.7; //dense
	private final static double sparsity2 = 0.1; //sparse
	
	private final static double maxiter = 10;
	
	@Override
	public void setUp() 
	{
		TestUtils.clearAssertionInformation();
		addTestConfiguration(TEST_NAME1, new TestConfiguration(TEST_DIR, TEST_NAME1, new String[] { "w" })); 
	}

	@Test
	public void testMMChainLoopDenseCP() 
	{
		runGDFOTest(TEST_NAME1, false, ExecType.CP);
	}
	
	@Test
	public void testMMChainLoopSparseCP() 
	{
		runGDFOTest(TEST_NAME1, true, ExecType.CP);
	}
	
	/**
	 * 
	 * @param sparseM1
	 * @param sparseM2
	 * @param instType
	 */
	private void runGDFOTest( String testname,boolean sparse, ExecType instType)
	{
		//rtplatform for MR
		RUNTIME_PLATFORM platformOld = rtplatform;
		rtplatform = (instType==ExecType.MR) ? RUNTIME_PLATFORM.HADOOP : RUNTIME_PLATFORM.HYBRID;

		try
		{
			String TEST_NAME = testname;
			TestConfiguration config = getTestConfiguration(TEST_NAME);
			
			/* This is for running the junit test the new way, i.e., construct the arguments directly */
			String HOME = SCRIPT_DIR + TEST_DIR;
			fullDMLScriptName = HOME + TEST_NAME + ".dml";
			programArgs = new String[]{ "-explain",//"hops",
					                    "-config="+HOME+TEST_CONF,
					                    "-args", HOME + INPUT_DIR + "X",
					                             HOME + INPUT_DIR + "v",
					                             String.valueOf(maxiter),
					                            HOME + OUTPUT_DIR + "w"};
			fullRScriptName = HOME + TEST_NAME + ".R";
			rCmd = "Rscript" + " " + fullRScriptName + " " + 
			       HOME + INPUT_DIR + " " + String.valueOf(maxiter) + " " + HOME + EXPECTED_DIR;
			
			loadTestConfiguration(config);
	
			//generate actual datasets
			double[][] X = getRandomMatrix(rows, cols, 0, 1, sparse?sparsity2:sparsity1, 7);
			writeInputMatrixWithMTD("X", X, true);
			double[][] y = getRandomMatrix(cols, 1, 0, 1, 1.0, 3);
			writeInputMatrixWithMTD("v", y, true);
			
			runTest(true, false, null, -1); 
			runRScript(true); 
			
			//compare matrices
			HashMap<CellIndex, Double> dmlfile = readDMLMatrixFromHDFS("w");
			HashMap<CellIndex, Double> rfile  = readRMatrixFromFS("w");
			double absEps = rfile.get(new CellIndex(1,1)).doubleValue() * eps;
			TestUtils.compareMatrices(dmlfile, rfile, absEps, "Stat-DML", "Stat-R");
		}
		finally
		{
			rtplatform = platformOld;
		}
	}

}