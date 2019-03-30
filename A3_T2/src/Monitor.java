/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */
	private int nb_chopsticks; //the total number of chopsticks available
	private boolean[] chopsticks; //tracks the use of each of the chopsticks. True means the chopstick is in use.

	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		nb_chopsticks = 2*piNumberOfPhilosophers;
		for(int i = 0; i<chopsticks.length; i++)
		{
			chopsticks[i] = false;
		}
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	private synchronized void test(int pID)
	{
		//tests if the chopsticks for that philosopher are available or not
		//if it is, then we pick it up and so the chopstick at that position becomes unavailable
		if(chopsticks[pID%(nb_chopsticks/2)] && chopsticks[(pID+1)%(nb_chopsticks/2)])
		{
			chopsticks[pID%(nb_chopsticks/2)] = false;
			chopsticks[(pID+1)%(nb_chopsticks/2)] = false;
		}

		else
		{
			try
			{
				wait();
			}
			catch (InterruptedException e)
			{

			}

		}
	}

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public synchronized void pickUp(final int piTID)
	{
		//we perform a test to see if the two chopsticks are available. If not, then we wait. If they are available
		//then we are giving access to the end of this method
		test(piTID);
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		//
	}

	/**
	 * Only one philopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk()
	{
		// ...
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk()
	{
		// ...
	}
}

// EOF
