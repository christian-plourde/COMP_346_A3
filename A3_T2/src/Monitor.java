/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

enum State{HUNGRY, EATING, SLEEPING, THINKING, TALKING};

public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */
	private State[] state;
	private final Lock lock = new ReentrantLock();
	private Condition[] eating_conditions;

	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		state = new State[piNumberOfPhilosophers];
		eating_conditions = new Condition[piNumberOfPhilosophers];
		//initially all of the states should be set to thinking
		for(int i = 0; i<state.length; i++)
		{
			state[i] = State.THINKING;
			eating_conditions[i] = lock.newCondition();
		}
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	private void test(int pID)
	{
		//here we test if the philosopher can eat
		// it is okay for him to eat if his left and right neighbors are not eating. This is legal if his left and right
		//neighbors are not eating. This will guarantee that the chopstick to his left and the chopstick to his right
		//are available
		System.out.println("Testing for philosopher " + (pID + 1) + ". The neighbors are: " + ((pID + 1)%state.length + 1) +
				" and " + ((pID + state.length - 1)%state.length + 1) + ".");

		if(state[(pID + 1)%state.length] != State.EATING && state[(pID + state.length - 1)%state.length] != State.EATING
			&& state[pID] == State.HUNGRY)
		{
			state[pID] = State.EATING;
			eating_conditions[pID].signal();
			return;
		}

		System.out.println("Philosopher " + (pID + 1) + " is not allowed to eat. "
				+ "Exiting method for Philosopher " + (pID + 1) + ".");
	}

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public void pickUp(final int piTID)
	{
		lock.lock();
		int id = piTID;
		//before picking up set the state to hungry to signal intention to eat
		state[id] = State.HUNGRY;

		//test to see if the chopsticks are available
		test(id);

		if(state[id] != State.EATING)
		{
			System.out.println("Philosopher " + (id + 1) + " is waiting for chopsticks to be available.");
			//if by this point we are not eating we should wait
			try
			{
				eating_conditions[id].await();
			}

			catch(InterruptedException e)
			{

			}

			System.out.println("Chopsticks have become available for Philosopher " + (id+1) + ".");
		}

		lock.unlock();
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public void putDown(final int piTID)
	{
		lock.lock();

		int id = piTID;
		System.out.println("Philosopher " + (id + 1) + " is getting ready to put down his chopsticks.");
		//we should first set our state to thinking
		state[id] = State.THINKING;

		//then we should test our left and right neighbors to allow them to eat
		test((id + 1)%state.length);
		test((id + state.length - 1)%state.length);

		System.out.println("Philosopher " + (id + 1) + " has put down his chopsticks.");
		lock.unlock();
	}

	/**
	 * Only one philosopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk()
	{
		//in the request talk method, we should wait for a maximum given amount of time, which is TIME_TO_WASTE
		System.out.println("A philosopher is requesting to speak.");
		try
		{
			wait(Philosopher.TIME_TO_WASTE);
		}

		catch(InterruptedException e)
		{
			System.out.println("Interruption detected.");
		}
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk()
	{
		//when a philosopher has finished talking, he should signal that he is finished talking so that a
		//philosopher waiting to speak can begin talking.
		notify();
		System.out.println("Other philosophers are now free to express their thoughts.");
	}
}

// EOF
