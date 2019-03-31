import common.BaseThread;

/**
 * Class Philosopher.
 * Outlines main subrutines of our virtual philosopher.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Philosopher extends BaseThread
{

	/**
	 * Max time an action can take (in milliseconds)
	 */
	public static final long TIME_TO_WASTE = 1000;

	/**
	 * The act of eating.
	 * - Print the fact that a given phil (their TID) has started eating.
	 * - yield
	 * - Then sleep() for a random interval.
	 * - yield
	 * - The print that they are done eating.
	 */
	public void eat()
	{
		try
		{
			System.out.println("Philosopher " + getTID() + " is now eating.");
			yield();
			sleep((long)(Math.random() * TIME_TO_WASTE));
			yield();
			System.out.println("Philosopher " + getTID() + " has finished eating.");
		}
		catch(InterruptedException e)
		{
			System.err.println("Philosopher.eat():");
			DiningPhilosophers.reportException(e);
			System.exit(1);
		}
	}

	/**
	 * The act of thinking.
	 * - Print the fact that a given phil (their TID) has started thinking.
	 * - yield
	 * - Then sleep() for a random interval.
	 * - yield
	 * - The print that they are done thinking.
	 */
	public void think()
	{
		try
		{
			System.out.println("Philosopher " + getTID() + " is now thinking.");
			yield();
			sleep((long)(Math.random() * TIME_TO_WASTE));
			yield();
			System.out.println("Philosopher " + getTID() + " has finished thinking.");
		}

		catch(InterruptedException e)
		{
			System.err.println("Philosopher.think():");
			DiningPhilosophers.reportException(e);
			System.exit(1);
		}
	}

	/**
	 * The act of talking.
	 * - Print the fact that a given phil (their TID) has started talking.
	 * - yield
	 * - Say something brilliant at random
	 * - yield
	 * - The print that they are done talking.
	 */
	public void talk()
	{
			System.out.println("Philosopher " + getTID() + " is now talking.");
			yield();
			saySomething();
			yield();
			System.out.println("Philosopher " + getTID() + " has finished talking.");
	}

	/**
	 * No, this is not the act of running, just the overridden Thread.run()
	 */
	public void run()
	{
		for(int i = 0; i < DiningPhilosophers.DINING_STEPS; i++)
		{
			DiningPhilosophers.soMonitor.pickUp(getTID() - 1);

			eat();

			DiningPhilosophers.soMonitor.putDown(getTID() - 1);

			think();

			if(allowTalk())
			{
				//if the philosopher is going to say something useful, i.e. he was granted permission to say something
				//then we are here
				//before speaking, the philosopher must first make a request to speak
				DiningPhilosophers.soMonitor.requestTalk();
				talk();
				//when he is finished talking he must end his talking phase to let others know they can now speak
				//the only way this can be bypassed is if a philosopher waiting to speak has waited longer than his
				//maximum allowed waiting time, in which can he can speak even if the philosopher currently speaking
				//has not finished talking
				DiningPhilosophers.soMonitor.endTalk();
			}

			else
			{
				System.out.println("Philosopher " + getTID() + " had nothing interesting to say.");
			}

			yield();
		}
	} // run()

	/**
	 * Prints out a phrase from the array of phrases at random.
	 * Feel free to add your own phrases.
	 */
	public void saySomething()
	{
		String[] astrPhrases =
		{
			"Eh, it's not easy to be a philosopher: eat, think, talk, eat...",
			"You know, true is false and false is true if you think of it",
			"2 + 2 = 5 for extremely large values of 2...",
			"If thee cannot speak, thee must be silent",
			"My number is " + getTID() + ""
		};

		System.out.println
		(
			"Philosopher " + getTID() + " says: " +
			astrPhrases[(int)(Math.random() * astrPhrases.length)]
		);
	}

	private boolean allowTalk()
	{
		//a method to determine if the philosopher is allowed to talk (i.e. going to say something useful
		//if the number generated is a multiple of 2 he is allowed to talk, otherwise he isn't.
		int decision = (int)(Math.random()*100);
		if(decision%2 == 0)
			return true;
		return false;
	}
}

// EOF
