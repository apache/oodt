// Copyright 2002-2003 California Institute of Technology.  ALL RIGHTS RESERVED.
// U.S. Government Sponsorship acknowledged.
//
// $Id: TestRunner.java,v 1.2 2005-08-03 17:00:50 kelly Exp $

package jpl.eda.product.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import jpl.eda.product.ProductClient;
import jpl.eda.product.ProductException;
import jpl.eda.xmlquery.Result;
import jpl.eda.xmlquery.XMLQuery;
import java.security.MessageDigest;
import java.security.DigestInputStream;
import java.util.Arrays;

/**
 * Test runner.
 *
 * @author Kelly
 * @version $Revision: 1.2 $
 */
public class TestRunner {
	/**
	 * Run tests described in the given configuration.
	 *
	 * @param config a <code>TestConfig</code> value.
	 * @throws ProductException if an error occurs.
	 */
	public static void runQueries(final TestConfig config) throws ProductException {
		final TestRunner runner = new TestRunner(config);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				runner.shutdown();
				System.out.println("        Total queries run: " + runner.totalQueriesRun);
				System.out.println(" Max simultaneous queries: " + config.getConcurrency());
				System.out.println("Average service time (ms): " + runner.getAverageServiceTime());
				System.out.println("                Successes: " + runner.totalSuccesses);
				System.out.println("        Garbled responses: " + runner.totalGarbled);
				System.out.println("                   Errors: " + runner.totalErrors);
				System.out.println("                Time outs: " + runner.totalTimeOuts);
			}
		});
		runner.start();
	}

	/**
	 * Stop testing.
	 */
	public synchronized void shutdown() {
		System.err.print("Shutting down...");
		running = false;
		notifyAll();
		while (current > 0) try {
			System.out.print(".");
			System.out.flush();
			wait();
		} catch (InterruptedException ignore) {}
		System.out.println("done");
	}

	/**
	 * Start testing.
	 */
	public void start() {
		running = true;
		int index = 0;
		while (running)
			launch((Query) queries[index++ % queries.length]);
	}

	/**
	 * Get the average time (in milliseconds) to service a successful transaction.
	 *
	 * @return a <code>double</code> value.
	 */
	public double getAverageServiceTime() {
		return totalSuccesses != 0? ((double) successTime) / totalSuccesses : 0.0;
	}

	/**
	 * Launch the given query as room permits.
	 *
	 * @param query a <code>Query</code> value.
	 */
	private synchronized void launch(final Query query) {
		while (current == concurrency && running) try {
			wait();
		} catch (InterruptedException ignore) {}
		if (!running) return;
		++current;
		new Thread() {
			public void run() {
				++totalQueriesRun;
				long time0 = System.currentTimeMillis();
				switch (executeQuery(query)) {
					case OK:
						successTime += (System.currentTimeMillis() - time0);
						++totalSuccesses;
						break;
					case TIME_OUT:
						++totalTimeOuts;
						break;
					case ERROR:
						++totalErrors;
						break;
					case GARBLED:
						++totalGarbled;
						break;
				}
				synchronized (TestRunner.this) {
					--current;
					TestRunner.this.notifyAll();
				}
			}
		}.start();
	}

	/**
	 * Execute the given query, timing it out if necessary.
	 *
	 * @param query a <code>Query</code> value.
	 * @return an <code>int</code> value.
	 */
	private int executeQuery(Query query) {
		QueryExecuter qe = new QueryExecuter(query);
		qe.start();
		try {
			qe.join(maxWait);
		} catch (InterruptedException ignore) {}
		if (qe.isAlive()) {
			qe.interrupt();
			return TIME_OUT;
		} else if (qe.getError() != null)
			return ERROR;
		else if (qe.wasOK())
			return OK;
		else return GARBLED;
	}

	/**
	 * Creates a new <code>TestRunner</code> instance.
	 *
	 * @param config a <code>TestConfig</code> value.
	 * @throws ProductException if an error occurs.
	 */
	private TestRunner(TestConfig config) throws ProductException {
		concurrency = config.getConcurrency();
		maxWait = config.getMaxWait();
		queries = (Query[]) config.getQueries().toArray(new Query[0]);
		productClient = new ProductClient(config.getObject());
	}

	private ProductClient productClient;
	private Query[] queries;
	private volatile boolean running;
	private int concurrency;
	private int current;
	private long successTime;
	private int totalSuccesses;
	private int totalErrors;
	private long totalBytes;
	private int totalQueriesRun;
	private int maxWait;
	private int totalTimeOuts;
	private int totalGarbled;

	private static final int OK = 0;
	private static final int TIME_OUT = 1;
	private static final int ERROR = 2;
	private static final int GARBLED = 3;

	/**
	 * Thread to execute a query.
	 */
	private class QueryExecuter extends Thread {
		QueryExecuter(Query query) {
			this.query = query;
		}
		public void run() {
			DigestInputStream in = null;
			try {
				XMLQuery q = new XMLQuery(query.getExpr(), /*id*/null, /*title*/null, /*desc*/null, /*ddID*/null,
					/*resultModeID*/null, /*propType*/null, /*propLevels*/null, /*maxResults*/999,
					/*mimeAccept*/null, /*parseQuery*/true);
				XMLQuery response = productClient.query(q);
				List results = response.getResults();
				if (results.isEmpty()) throw new ProductException("always expect results");
				Result r = (Result) results.get(0);
				MessageDigest md = MessageDigest.getInstance("MD5");
				in = new DigestInputStream(r.getInputStream(), md);
				byte[] buf = new byte[512];
				int num;
				long total = 0;
				while ((num = in.read(buf)) != -1)
					total += num;
				totalBytes += total;
				in.close();
				byte[] digest = md.digest();
				ok = query.checkDigest(digest);
			} catch (RuntimeException ex) {
				throw ex;
			} catch (Exception ex) {
				error = ex;
			} finally {
				if (in != null) try {
					in.close();
				} catch (IOException ignore) {}
			}
		}
		Exception getError() {
			return error;
		}
		boolean wasOK() {
			return ok;
		}
		private Exception error;
		private Query query;
		private boolean ok;
	}
}
