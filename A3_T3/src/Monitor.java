/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

enum State{HUNGRY, EATING, THINKING, SLEEPING};

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
	private int[] priority; //this will hold the priority of each of the philosophers
	private boolean[] prioritiesSet;
	boolean someoneIsTalking;

	private void randomizePriority()
	{
		//this method assigns eating priorities randomly.
		boolean done = false;

		while(!done)
		{
			for(int i = 0; i < priority.length; i++)
			{
				if(prioritiesSet())
				{
					done = true;
				}

				int prior = (int)(Math.random()*priority.length);

				if(!priorityContains(prior))
				{
					priority[i] = prior;
					prioritiesSet[i] = true;
				}
			}
		}
	}

	private boolean prioritiesSet()
	{
		for(int i = 0; i<prioritiesSet.length; i++)
		{
			if(!prioritiesSet[i])
				return false;
		}
		return true;
	}

	private boolean priorityContains(int i)
	{
		for(int j = 0; j<priority.length; j++)
		{
			if(priority[j] == i)
				return true;
		}

		return false;
	}

	private boolean hasPriority(int id)
	{
		//to determine if a philosopher has priority, we need to check in the array if the other philosophers
		//have a higher priority and are hungry. If this is the case for any, the we should return false
		//otherwise this philosopher has priority
		for(int i = 0; i<priority.length; i++)
		{
			if(i != id && priority[i] > priority[id] && state[i] == State.HUNGRY)
				return false;
		}

		return true;
	}

	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		state = new State[piNumberOfPhilosophers];
		eating_conditions = new Condition[piNumberOfPhilosophers];
		priority = new int[piNumberOfPhilosophers];
		prioritiesSet = new boolean[piNumberOfPhilosophers];
		someoneIsTalking = false;

		//initially all of the states should be set to thinking
		System.out.println("Initial eating priorities:");
		for(int i = 0; i<state.length; i++)
		{
			state[i] = State.THINKING;
			prioritiesSet[i] = false;
			priority[i] = -1;
			eating_conditions[i] = lock.newCondition();
		}

		randomizePriority();
		for(int i = 0; i<priority.length; i++)
		{
			System.out.println("Philosopher " + (i+1) + " has priority " + priority[i] + ".");
		}
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	private void test(int pID)
	{
		String priorityInfo = "";
		//here we test if the philosopher can eat
		// it is okay for him to eat if his left and right neighbors are not eating. This is legal if his left and right
		//neighbors are not eating. This will guarantee that the chopstick to his left and the chopstick to his right
		//are available

		//the philosopher at the end of the array should be the next one allowed to eat.
		//if the id of the philosopher is equal to the id in the last position of the array (initially 4)
		//then that philosopher is allowed to eat. otherwise we should return
		boolean hasPriority = hasPriority(pID);

		if(state[(pID + 1)%state.length] != State.EATING && state[(pID + state.length - 1)%state.length] != State.EATING
			&& state[pID] == State.HUNGRY && hasPriority)
		{
			state[pID] = State.EATING;
			eating_conditions[pID].signal();
			return;
		}

		if(!hasPriority)
			priorityInfo = "Philosopher " + (pID + 1) + " does not have priority. ";

		System.out.println("Philosopher " + (pID + 1) + " is not allowed to eat. " + priorityInfo
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
	public synchronized void requestTalk(final int piTID)
	{
		//in the request talk method, we should wait for a maximum given amount of time, which is TIME_TO_WASTE
		System.out.println("A philosopher is requesting to speak.");

		//we are allowed to speak if no one else is talking and if no one is sleeping
		boolean allowedToSpeak = false;

		while(someoneIsTalking || someoneIsSleeping())
		{
			allowedToSpeak = !someoneIsSleeping();

			try
			{
				if(!allowedToSpeak || someoneIsTalking)
				{
					System.out.println("Philosopher " + (piTID + 1) + " could not speak.");
					this.wait();
				}
			}

			catch(InterruptedException e)
			{
				System.out.println("Interruption detected.");
			}
		}
		someoneIsTalking = true;
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk(final int piTID)
	{
		//when a philosopher has finished talking, he should signal that he is finished talking so that a
		//philosopher waiting to speak can begin talking.
		someoneIsTalking = false;
		notify();
		System.out.println("Other philosophers are now free to express their thoughts.");
	}

	/**
	 *
	 */
	public synchronized void requestSleep(final int piTID)
	{
		//check if there is someone talking
		//otherwise wait
		System.out.println("Philosopher " + (piTID + 1) + " wants to sleep.");

		while(someoneIsTalking)
		{
			System.out.println("Philosopher " + (piTID+1) + " could not sleep because someone was talking.");
			try
			{
				this.wait();
				state[piTID] = State.SLEEPING;
				System.out.println("Philosopher " + (piTID + 1) + " is now sleeping.");
			}

			catch(InterruptedException e)
			{
				System.out.println("Interruption detected");
			}
		}

		System.out.println("Philosopher " + (piTID + 1) + " is now sleeping.");
	}

	public synchronized void endSleep(final int piTID)
	{
		this.notify();
		state[piTID] = State.THINKING;
		System.out.println("Philosopher " + (piTID + 1) + " has finished sleeping.");
	}

	private synchronized boolean someoneIsSleeping()
	{
		for(int i = 0; i < state.length; i++)
		{
			if(state[i] == State.SLEEPING)
				return true;
		}

		return false;
	}

}

// EOF
